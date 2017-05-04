package com.github.ayltai.newspaper.graphics;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import com.github.ayltai.newspaper.util.SuppressFBWarnings;

public final class FaceCenterCrop {
    //region Constants

    private static final int MAX_WIDTH  = 720;
    private static final int MAX_HEIGHT = 720;

    private static final float MAX_DELTA = 0.05f;

    //endregion

    //region Variables

    private final int width;
    private final int height;

    //endregion

    public FaceCenterCrop(final int width, final int height) {
        this.width  = width;
        this.height = height;
    }

    @NonNull
    public ScaleCenter findCroppedCenter(@NonNull final File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        final float ratio = Math.max((float)this.width / options.outWidth, (float)this.height / options.outHeight);

        options.inSampleSize = 1;
        while (options.outWidth / options.inSampleSize > FaceCenterCrop.MAX_WIDTH || options.outHeight / options.inSampleSize > FaceCenterCrop.MAX_HEIGHT) options.inSampleSize *= 2;

        options.inJustDecodeBounds = false;

        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        final float  scaleX = (float)this.width / bitmap.getWidth();
        final float  scaleY = (float)this.height / bitmap.getHeight();
        final PointF center = FaceCenterCrop.findCroppedCenter(bitmap, this.width, this.height, scaleX, scaleY);

        center.x *= options.inSampleSize;
        center.y *= options.inSampleSize;

        return new ScaleCenter(ratio, center);
    }

    @SuppressFBWarnings("MOM_MISLEADING_OVERLOAD_MODEL")
    @NonNull
    private static PointF findCroppedCenter(@NonNull final Bitmap bitmap, final int width, final int height, final float scaleX, final float scaleY) {
        final float halfWidth  = bitmap.getWidth()  / 2f;
        final float halfHeight = bitmap.getHeight() / 2f;

        if (width == 0 || height == 0 || Math.abs(scaleX - scaleY) < FaceCenterCrop.MAX_DELTA) return new PointF(halfWidth, halfHeight);

        final PointF center = new PointF();

        FaceCenterCrop.detectFace(bitmap, center);

        if (scaleX < scaleY) return new PointF(center.x, halfHeight);

        return new PointF(halfWidth, center.y);
    }

    private static void detectFace(@NonNull final Bitmap bitmap, @NonNull final PointF centerOfAllFaces) {
        if (FaceDetectorFactory.isValid()) {
            final FaceDetector      faceDetector = FaceDetectorFactory.getDetector();
            final SparseArray<Face> faces        = faceDetector.detect(new Frame.Builder().setBitmap(bitmap).build());
            final int               totalFaces   = faces.size();

            if (totalFaces > 0) {
                float sumX = 0f;
                float sumY = 0f;

                for (int i = 0; i < totalFaces; i++) {
                    final PointF faceCenter = new PointF();

                    FaceCenterCrop.getFaceCenter(faces.get(faces.keyAt(i)), faceCenter);

                    sumX += faceCenter.x;
                    sumY += faceCenter.y;
                }

                centerOfAllFaces.set(sumX / totalFaces, sumY / totalFaces);
            } else {
                centerOfAllFaces.set(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
            }
        } else {
            centerOfAllFaces.set(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        }
    }

    private static void getFaceCenter(@NonNull final Face face, @NonNull final PointF center) {
        center.set(face.getPosition().x + (face.getWidth() / 2f), face.getPosition().y + (face.getHeight() / 2f));
    }
}
