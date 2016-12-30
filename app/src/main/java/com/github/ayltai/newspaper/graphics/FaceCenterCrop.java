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

public final class FaceCenterCrop {
    //region Constants

    private static final int MAX_WIDTH = 720;
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

    public PointF findCroppedCenter(@NonNull final File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = 1;
        while (options.outWidth / options.inSampleSize > FaceCenterCrop.MAX_WIDTH || options.outHeight / options.inSampleSize > FaceCenterCrop.MAX_HEIGHT) options.inSampleSize *= 2;

        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        final float scaleX = (float)width / bitmap.getWidth();
        final float scaleY = (float)height / bitmap.getHeight();
        final float scale  = Math.max(scaleX, scaleY);

        final PointF center = findCroppedCenter(bitmap, width, height, scale, scaleX, scaleY);

        center.x *= options.inSampleSize * scale;
        center.y *= options.inSampleSize * scale;

        return center;
    }

    private static PointF findCroppedCenter(@NonNull final Bitmap bitmap, final int width, final int height, final float scale, final float scaleX, final float scaleY) {
        final float halfWidth  = scale * bitmap.getWidth() / 2f;
        final float halfHeight = scale * bitmap.getHeight() / 2f;

        if (width == 0 || height == 0 || Math.abs(scaleX - scaleY) < MAX_DELTA) return new PointF(halfWidth, halfHeight);

        final PointF center = new PointF();

        FaceCenterCrop.detectFace(bitmap, center);

        if (scaleX < scaleY) return new PointF(halfWidth + FaceCenterCrop.getLeftPoint(width, scale * bitmap.getWidth(), scale * center.x), halfHeight);

        return new PointF(halfWidth, halfHeight + FaceCenterCrop.getTopPoint(height, scale * bitmap.getHeight(), scale * center.y));
    }

    private static void detectFace(@NonNull final Bitmap bitmap, @NonNull final PointF centerOfAllFaces) {
        final FaceDetector faceDetector = FaceDetectorFactory.getDetector();

        if (faceDetector.isOperational()) {
            final SparseArray<Face> faces      = faceDetector.detect(new Frame.Builder().setBitmap(bitmap).build());
            final int               totalFaces = faces.size();

            if (totalFaces > 0) {
                float sumX = 0f;
                float sumY = 0f;

                for (int i = 0; i < totalFaces; i++) {
                    final PointF faceCenter = new PointF();

                    FaceCenterCrop.getFaceCenter(faces.get(faces.keyAt(i)), faceCenter);

                    sumX = sumX + faceCenter.x;
                    sumY = sumY + faceCenter.y;
                }

                centerOfAllFaces.set(sumX / totalFaces, sumY / totalFaces);
            } else {
                centerOfAllFaces.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            }
        } else {
            centerOfAllFaces.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        }
    }

    private static void getFaceCenter(@NonNull final Face face, @NonNull final PointF center) {
        center.set(face.getPosition().x + (face.getWidth() / 2), face.getPosition().y + (face.getHeight() / 2));
    }

    private static float getTopPoint(final int height, final float scaledHeight, final float faceCenterY) {
        if (faceCenterY <= height / 2) {
            return 0f;
        }

        if ((scaledHeight - faceCenterY) <= height / 2) {
            return height - scaledHeight;
        }

        return (height / 2) - faceCenterY;
    }

    private static float getLeftPoint(final int width, final float scaledWidth, final float faceCenterX) {
        if (faceCenterX <= width / 2) {
            return 0f;
        }

        if ((scaledWidth - faceCenterX) <= width / 2) {
            return width - scaledWidth;
        }

        return (width / 2) - faceCenterX;
    }
}
