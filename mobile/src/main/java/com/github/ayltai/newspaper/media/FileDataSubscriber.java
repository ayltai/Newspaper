package com.github.ayltai.newspaper.media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.github.ayltai.newspaper.util.IOUtils;

abstract class FileDataSubscriber extends BaseDataSubscriber<CloseableReference<PooledByteBuffer>> {
    private final Context context;

    private volatile boolean isFinished;

    FileDataSubscriber(@NonNull final Context context) {
        this.context = context;
    }

    @SuppressWarnings("magicnumber")
    @Override
    public void onProgressUpdate(@NonNull final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (!this.isFinished) this.onProgress((int)(dataSource.getProgress() * 100f + 0.5f));
    }

    @Override
    protected void onNewResultImpl(@NonNull final DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
        if (dataSource.isFinished()) {
            final CloseableReference<PooledByteBuffer> reference = dataSource.getResult();

            if (reference == null) {
                this.onFailureImpl(dataSource);
            } else {
                final File file = new File(this.context.getCacheDir(), UUID.randomUUID().toString() + ".tmp");

                PooledByteBufferInputStream inputStream  = null;
                FileOutputStream            outputStream = null;

                try {
                    inputStream  = new PooledByteBufferInputStream(reference.get());
                    outputStream = new FileOutputStream(file);

                    IOUtils.copy(inputStream, outputStream);

                    this.isFinished = true;

                    this.onSuccess(file);
                } catch (final IOException e) {
                    this.onFailure(e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
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
