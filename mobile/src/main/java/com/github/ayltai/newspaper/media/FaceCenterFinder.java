package com.github.ayltai.newspaper.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import com.github.ayltai.newspaper.Constants;

import io.reactivex.disposables.Disposable;

@Singleton
public final class FaceCenterFinder implements Disposable, LifecycleObserver {
    private final FaceDetector detector;

    FaceCenterFinder(@NonNull final FaceDetector detector) {
        this.detector = detector;
    }

    @Override
    public boolean isDisposed() {
        return !this.detector.isOperational();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        this.detector.release();
    }

    @NonNull
    public PointF findFaceCenter(@NonNull final File file) {
        final int scale = FaceCenterFinder.findDownSamplingScale(file);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;

        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        final Collection<PointF> centers = this.findFaceCenters(bitmap);
        if (centers.isEmpty()) return new PointF(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

        float sumX = 0f;
        float sumY = 0f;

        for (final PointF center : centers) {
            sumX += center.x;
            sumY += center.y;
        }

        return new PointF(scale * sumX / centers.size(), scale * sumY / centers.size());
    }

    @NonNull
    private Collection<PointF> findFaceCenters(@NonNull final Bitmap bitmap) {
        if (this.detector.isOperational()) {
            final SparseArray<Face> faces = this.detector.detect(new Frame.Builder()
                .setBitmap(bitmap)
                .build());

            final Collection<PointF> centers = new ArrayList<>();

            for (int i = 0; i < faces.size(); i++) {
                final Face face = faces.get(faces.keyAt(i));
                centers.add(new PointF(face.getPosition().x + face.getWidth() / 2f, face.getPosition().y + face.getHeight() / 2f));
            }

            return centers;
        }

        return Collections.emptyList();
    }

    private static int findDownSamplingScale(@NonNull final File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        int width  = options.outWidth;
        int height = options.outHeight;
        int scale  = 1;

        while (width * height > Constants.FACE_DETECTION_SIZE_MAX) {
            width  /= 2;
            height /= 2;
            scale  *= 2;
        }

        return scale;
    }
}
