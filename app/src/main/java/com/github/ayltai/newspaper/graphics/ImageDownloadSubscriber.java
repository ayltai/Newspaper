package com.github.ayltai.newspaper.graphics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.apache.commons.io.IOUtils;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferInputStream;

abstract class ImageDownloadSubscriber extends BaseDataSubscriber<CloseableReference<PooledByteBuffer>> {
    private final File file;

    private volatile boolean isFinished;

    protected ImageDownloadSubscriber(@NonNull final String path) {
        this.file = new File(path, "" + System.nanoTime() + ".png");
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public void onProgressUpdate(final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (!this.isFinished) this.onProgress((int)(dataSource.getProgress() * 100f + 0.5f));
    }

    @Override
    protected void onNewResultImpl(final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (dataSource.isFinished() && dataSource.getResult() != null) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream  = new PooledByteBufferInputStream(dataSource.getResult().get());
                outputStream = new FileOutputStream(this.file);

                IOUtils.copy(inputStream, outputStream);

                this.isFinished = true;

                this.onSuccess(this.file);
            } catch (final IOException e) {
                this.onFail(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    @Override
    protected void onFailureImpl(final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        this.isFinished = true;

        this.onFail(dataSource.getFailureCause());
    }

    @WorkerThread
    protected abstract void onProgress(int progress);

    @WorkerThread
    protected abstract void onSuccess(@NonNull File image);

    @WorkerThread
    protected abstract void onFail(@NonNull Throwable error);
}
