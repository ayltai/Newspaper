package com.github.ayltai.newspaper.graphics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.vision.face.FaceDetector;

public final class FaceDetectorFactory {
    private static final String ERROR_MESSAGE = "Initialize FaceDetectorFactory by calling FaceDetectorFactory.initialize(context)";

    private static volatile FaceDetector detector;

    private static Context context;

    public static void initialize(@NonNull final Context context) {
        FaceDetectorFactory.context = context.getApplicationContext();
    }

    public static void release() {
        if (FaceDetectorFactory.detector != null) {
            FaceDetectorFactory.detector.release();
            FaceDetectorFactory.detector = null;
        }

        FaceDetectorFactory.context = null;
    }

    @NonNull
    static FaceDetector getDetector() {
        if (FaceDetectorFactory.context == null) throw new IllegalStateException(FaceDetectorFactory.ERROR_MESSAGE);

        if (FaceDetectorFactory.detector == null) {
            synchronized (FaceDetectorFactory.class) {
                if (FaceDetectorFactory.detector == null) {
                    FaceDetectorFactory.detector = new FaceDetector.Builder(FaceDetectorFactory.context)
                        .setTrackingEnabled(false)
                        .build();
                }
            }
        }

        return FaceDetectorFactory.detector;
    }

    @NonNull
    static Context getContext() {
        if (FaceDetectorFactory.context == null) throw new IllegalStateException(FaceDetectorFactory.ERROR_MESSAGE);

        return FaceDetectorFactory.context;
    }
}
