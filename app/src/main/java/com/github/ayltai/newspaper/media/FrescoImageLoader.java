package com.github.ayltai.newspaper.media;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.metadata.ImageInfoExtractor;

import io.reactivex.Maybe;

@Singleton
public final class FrescoImageLoader implements ImageLoader, Closeable, LifecycleObserver {
    private static final Handler                  HANDLER             = new Handler(Looper.getMainLooper());
    private static final List<DataSource>         SOURCES             = Collections.synchronizedList(new ArrayList<>());
    private static final Map<Integer, DataSource> CANCELLABLE_SOURCES = Collections.synchronizedMap(new ArrayMap<>());

    private static FrescoImageLoader instance;
    private static ExecutorSupplier  executorSupplier;

    private final Context context;

    public static void init(@Nonnull @NonNull @lombok.NonNull final Context context) {
        if (FrescoImageLoader.instance == null) {
            FrescoImageLoader.executorSupplier = new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors());
            FrescoImageLoader.instance         = new FrescoImageLoader(context);
        }
    }

    @Nonnull
    @NonNull
    public static FrescoImageLoader getInstance() {
        if (FrescoImageLoader.instance == null) throw new NullPointerException();

        return FrescoImageLoader.instance;
    }

    private FrescoImageLoader(@Nonnull @NonNull @lombok.NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @Nonnull
    @NonNull
    public static Maybe<Bitmap> loadImage(@Nonnull @NonNull @lombok.NonNull final String uri) {
        return Maybe.<CloseableReference<CloseableImage>>create(
            emitter -> {
                final DataSource<CloseableReference<CloseableImage>> source = Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(uri), Boolean.FALSE);

                try {
                    if (!emitter.isDisposed()) emitter.onSuccess(DataSources.waitForFinalResult(source));
                } catch (final Throwable error) {
                    if (!emitter.isDisposed()) emitter.onError(error);
                } finally {
                    source.close();
                }
            })
            .compose(RxUtils.applyMaybeBackgroundSchedulers())
            .flatMap(reference -> {
                if (reference.isValid()) {
                    final CloseableImage image = reference.get();

                    if (image instanceof CloseableBitmap) return Maybe.just(((CloseableBitmap)image).getUnderlyingBitmap());
                }

                return Maybe.empty();
            });
    }

    @Override
    public void loadImage(final int requestId, final Uri uri, @Nullable final ImageLoader.Callback callback) {
        final ImageRequest request = ImageRequest.fromUri(uri);
        final File         file    = FrescoImageLoader.getCacheFile(request);

        if (file.exists()) {
            if (callback != null) {
                callback.onCacheHit(ImageInfoExtractor.TYPE_STILL_IMAGE, file);
                callback.onSuccess(file);
            }
        } else {
            if (callback != null) {
                callback.onStart();
                callback.onProgress(0);
            }

            final ImagePipeline pipeline = Fresco.getImagePipeline();
            final DataSource<CloseableReference<PooledByteBuffer>> source   = pipeline.fetchEncodedImage(request, Boolean.TRUE);

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
                protected void onSuccess(@Nonnull @NonNull @lombok.NonNull final File image) {
                    FrescoImageLoader.CANCELLABLE_SOURCES.remove(requestId);

                    FrescoImageLoader.HANDLER.post(() -> {
                        if (callback != null) {
                            callback.onFinish();
                            callback.onCacheMiss(ImageInfoExtractor.TYPE_STILL_IMAGE, image);
                            callback.onSuccess(image);
                        }
                    });
                }

                @WorkerThread
                @Override
                protected void onFailure(@Nonnull @NonNull @lombok.NonNull final Throwable error) {
                    FrescoImageLoader.CANCELLABLE_SOURCES.remove(requestId);

                    RxUtils.handleError(error);

                    FrescoImageLoader.HANDLER.post(() -> {
                        if (callback != null) callback.onFail(new RuntimeException(error));
                    });
                }
            }, FrescoImageLoader.executorSupplier.forBackgroundTasks());

            FrescoImageLoader.CANCELLABLE_SOURCES.put(requestId, source);
        }
    }

    @Override
    public void prefetch(@Nonnull @NonNull @lombok.NonNull final Uri uri) {
        FrescoImageLoader.SOURCES.add(Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(uri), Boolean.FALSE));
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

    @Nonnull
    @NonNull
    private static File getCacheFile(@Nonnull @NonNull @lombok.NonNull final ImageRequest request) {
        final FileCache      mainFileCache = ImagePipelineFactory.getInstance().getMainFileCache();
        final CacheKey       cacheKey      = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(request, Boolean.FALSE);
        final BinaryResource resource      = mainFileCache.hasKey(cacheKey) ? mainFileCache.getResource(cacheKey) : null;

        if (resource == null) return request.getSourceFile();

        return ((FileBinaryResource)resource).getFile();
    }
}
