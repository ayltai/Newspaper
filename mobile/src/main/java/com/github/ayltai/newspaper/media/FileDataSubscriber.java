package com.github.ayltai.newspaper.media;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.arch.lifecycle.LifecycleObserver;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ayltai.newspaper.util.IOUtils;

abstract class FileDataSubscriber extends BaseDataSubscriber<CloseableReference<PooledByteBuffer>> implements LifecycleObserver {
    private final ImageRequest request;

    private volatile boolean isFinished;

    protected FileDataSubscriber(@NonNull final ImageRequest request) {
        this.request = request;
    }

    @SuppressWarnings("CheckStyle")
    @Override
    public void onProgressUpdate(@NonNull final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (!this.isFinished) this.onProgress((int)(dataSource.getProgress() * 100f + 0.5f));
    }

    @Override
    protected void onNewResultImpl(@NonNull final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (dataSource.isFinished() && dataSource.getResult() != null) {
            final FileCache mainFileCache = ImagePipelineFactory.getInstance().getMainFileCache();
            final CacheKey  cacheKey      = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(this.request, false);
            final File      file          = mainFileCache.hasKey(cacheKey) ? ((FileBinaryResource)mainFileCache.getResource(cacheKey)).getFile() : this.request.getSourceFile();

            try {
                mainFileCache.insert(cacheKey, outputStream -> {
                    final CloseableReference<PooledByteBuffer> reference = dataSource.getResult();

                    if (reference == null) {
                        this.onFailureImpl(dataSource);
                    } else {
                        InputStream inputStream = null;

                        try {
                            inputStream = new PooledByteBufferInputStream(reference.get());

                            IOUtils.copy(inputStream, outputStream);

                            this.isFinished = true;

                            this.onSuccess(file);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                            IOUtils.closeQuietly(outputStream);
                        }
                    }
                });
            } catch (final IOException e) {
                this.onFailure(e);
            }
        }
    }

    @Override
    protected void onFailureImpl(@NonNull final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        this.isFinished = true;

        final Throwable error = dataSource.getFailureCause();
        if (error != null) this.onFailure(error);
    }

    @WorkerThread
    protected abstract void onProgress(int progress);

    @WorkerThread
    protected abstract void onSuccess(@NonNull File image);

    @WorkerThread
    protected abstract void onFailure(@NonNull Throwable error);
}
