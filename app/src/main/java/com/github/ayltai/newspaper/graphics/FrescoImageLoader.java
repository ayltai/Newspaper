package com.github.ayltai.newspaper.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ayltai.newspaper.R;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;

public final class FrescoImageLoader implements ImageLoader {
    private static final List<DataSource<?>> SOURCES = new ArrayList<>();

    //region Variables

    private static String           cachePath;
    private static ImageLoader instance;
    private static ExecutorSupplier executorSupplier;

    //endregion

    public static void initialize(@NonNull final String cachePath) {
        FrescoImageLoader.cachePath        = cachePath;
        FrescoImageLoader.executorSupplier = new DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors());
    }

    public static ImageLoader getInstance() {
        if (FrescoImageLoader.cachePath == null) throw new IllegalStateException("FrescoImageLoader is not initialized");

        if (FrescoImageLoader.instance == null) FrescoImageLoader.instance = new FrescoImageLoader();

        return FrescoImageLoader.instance;
    }

    private FrescoImageLoader() {
    }

    @Override
    public void loadImage(final Uri uri, final Callback callback) {
        final ImageRequest request = ImageRequest.fromUri(uri);
        final File         file    = FrescoImageLoader.getFileCache(request);

        if (file.exists()) {
            callback.onCacheHit(file);
        } else {
            callback.onStart();
            callback.onProgress(0);

            final ImagePipeline                                    pipeline = Fresco.getImagePipeline();
            final DataSource<CloseableReference<PooledByteBuffer>> source   = pipeline.fetchEncodedImage(request, true);

            source.subscribe(new ImageDownloadSubscriber(FrescoImageLoader.cachePath) {
                @Override
                protected void onProgress(final int progress) {
                    callback.onProgress(progress);
                }

                @Override
                protected void onSuccess(@NonNull final File image) {
                    synchronized (FrescoImageLoader.SOURCES) {
                        FrescoImageLoader.SOURCES.remove(source);
                    }

                    callback.onFinish();
                    callback.onCacheMiss(image);
                }

                @Override
                protected void onFail(@NonNull final Throwable error) {
                    synchronized (FrescoImageLoader.SOURCES) {
                        FrescoImageLoader.SOURCES.remove(source);
                    }

                    Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                }
            }, FrescoImageLoader.executorSupplier.forBackgroundTasks());

            synchronized (FrescoImageLoader.SOURCES) {
                FrescoImageLoader.SOURCES.add(source);
            }
        }
    }

    @Override
    public void prefetch(final Uri uri) {
        synchronized (FrescoImageLoader.SOURCES) {
            FrescoImageLoader.SOURCES.add(Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(uri), false));
        }
    }

    @Override
    public View showThumbnail(final BigImageView parent, final Uri thumbnail, final int scaleType) {
        final SimpleDraweeView view = (SimpleDraweeView)LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_fresco_thumbnail, parent, false);

        if (scaleType == BigImageView.INIT_SCALE_TYPE_CENTER_CROP) {
            view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        } else if (scaleType == BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE) {
            view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
        }

        view.setController(Fresco.newDraweeControllerBuilder()
            .setUri(thumbnail)
            .build());

        return view;
    }

    public static void shutDown() {
        synchronized (FrescoImageLoader.SOURCES) {
            for (final DataSource<?> source : FrescoImageLoader.SOURCES) source.close();

            FrescoImageLoader.SOURCES.clear();
        }
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
