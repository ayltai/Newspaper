package com.github.ayltai.newspaper.media;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Optional;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;

import io.reactivex.Maybe;
import io.reactivex.Single;

@Singleton
public final class FrescoImageLoader implements ImageLoader, Closeable, LifecycleObserver {
    private static final Handler                  HANDLER             = new Handler(Looper.getMainLooper());
    private static final List<DataSource>         SOURCES             = Collections.synchronizedList(new ArrayList<>());
    private static final Map<Integer, DataSource> CANCELLABLE_SOURCES = Collections.synchronizedMap(new ArrayMap<>());

    protected static FrescoImageLoader instance;

    private static ExecutorSupplier executorSupplier;

    @NonNull
    public static FrescoImageLoader getInstance(@NonNull final Context context) {
        if (FrescoImageLoader.instance == null) {
            FrescoImageLoader.executorSupplier = new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors());
            FrescoImageLoader.instance         = new FrescoImageLoader(context);
        }

        return FrescoImageLoader.instance;
    }

    protected final Context context;

    protected FrescoImageLoader(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @SuppressWarnings("IllegalCatch")
    @NonNull
    public static Maybe<Bitmap> loadImage(@NonNull final String uri) {
        return Single.<CloseableReference<CloseableImage>>create(
            emitter -> {
                final DataSource<CloseableReference<CloseableImage>> source = Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(uri), false);

                try {
                    if (!emitter.isDisposed()) emitter.onSuccess(DataSources.waitForFinalResult(source));
                } catch (final Throwable error) {
                    if (!emitter.isDisposed()) emitter.onError(error);
                } finally {
                    source.close();
                }
            })
            .compose(RxUtils.applySingleBackgroundSchedulers())
            .map(reference -> {
                if (reference.isValid()) {
                    final CloseableImage image = reference.get();

                    // TODO: Checks image size to avoid out-of-memory error

                    if (image instanceof CloseableBitmap) return Optional.of(((CloseableBitmap)image).getUnderlyingBitmap());
                }

                return Optional.<Bitmap>empty();
            })
            .compose(RxUtils.applySingleBackgroundSchedulers())
            .flatMapMaybe(optional -> {
                if (optional.isPresent()) return Maybe.just(optional.get());

                return Maybe.empty();
            });
    }

    @Override
    public void loadImage(final int requestId, final Uri uri, final ImageLoader.Callback callback) {
        final ImageRequest request = ImageRequest.fromUri(uri);
        final File         file    = FrescoImageLoader.getFileCache(request);

        if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "Load image = " + uri.toString());
        if (DevUtils.isLoggable()) Log.d(this.getClass().getSimpleName(), "File cache = " + file.getAbsolutePath());

        if (file.exists()) {
            if (callback != null) {
                callback.onCacheHit(file);
                callback.onSuccess(file);
            }
        } else {
            if (callback != null) {
                callback.onStart();
                callback.onProgress(0);
            }

            final ImagePipeline                                    pipeline = Fresco.getImagePipeline();
            final DataSource<CloseableReference<PooledByteBuffer>> source   = pipeline.fetchEncodedImage(request, true);

            source.subscribe(new FileDataSubscriber(this.context) {
                @WorkerThread
                @Override
                protected void onProgress(final int progress) {
                    FrescoImageLoader.HANDLER.post(() -> {
                        if (callback != null) callback.onProgress(progress);
                    });
                }

                @WorkerThread
                @Override
                protected void onSuccess(@NonNull final File image) {
                    FrescoImageLoader.CANCELLABLE_SOURCES.remove(requestId);

                    FrescoImageLoader.HANDLER.post(() -> {
                        if (callback != null) {
                            callback.onFinish();
                            callback.onCacheMiss(image);
                            callback.onSuccess(image);
                        }
                    });
                }

                @WorkerThread
                @Override
                protected void onFailure(@NonNull final Throwable error) {
                    FrescoImageLoader.CANCELLABLE_SOURCES.remove(requestId);

                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));

                    FrescoImageLoader.HANDLER.post(() -> {
                        if (callback != null) callback.onFail(new RuntimeException(error));
                    });
                }
            }, FrescoImageLoader.executorSupplier.forBackgroundTasks());

            FrescoImageLoader.CANCELLABLE_SOURCES.put(requestId, source);
        }
    }

    @Override
    public View showThumbnail(@NonNull final BigImageView parent, @NonNull final Uri thumbnailUri, final int scaleType) {
        final SimpleDraweeView view = (SimpleDraweeView)LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_thumbnail, parent, false);

        if (scaleType == BigImageView.INIT_SCALE_TYPE_CENTER_CROP) {
            view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        } else if (scaleType == BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE) {
            view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
        }

        view.setController(Fresco.newDraweeControllerBuilder()
            .setUri(thumbnailUri)
            .build());

        return view;
    }

    @Override
    public void prefetch(@NonNull final Uri uri) {
        FrescoImageLoader.SOURCES.add(Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(uri), false));
    }

    @Override
    public void cancel(final int requestId) {
        if (FrescoImageLoader.CANCELLABLE_SOURCES.containsKey(requestId)) FrescoImageLoader.CANCELLABLE_SOURCES.remove(requestId).close();
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void close() {
        for (final DataSource source : FrescoImageLoader.SOURCES) source.close();
        FrescoImageLoader.SOURCES.clear();

        for (final DataSource source : FrescoImageLoader.CANCELLABLE_SOURCES.values()) source.close();
        FrescoImageLoader.CANCELLABLE_SOURCES.clear();
    }

    @NonNull
    private static File getFileCache(@NonNull final ImageRequest request) {
        final FileCache      mainFileCache = ImagePipelineFactory.getInstance().getMainFileCache();
        final CacheKey       cacheKey      = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(request, false);
        final BinaryResource resource      = mainFileCache.hasKey(cacheKey) ? mainFileCache.getResource(cacheKey) : null;

        if (resource == null) return request.getSourceFile();

        return ((FileBinaryResource)resource).getFile();
    }
}
