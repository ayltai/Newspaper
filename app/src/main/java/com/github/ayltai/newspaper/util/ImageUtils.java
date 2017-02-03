package com.github.ayltai.newspaper.util;

import java.io.File;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.ayltai.newspaper.Constants;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static void configure(@NonNull final SubsamplingScaleImageView imageView) {
        imageView.setPanEnabled(false);
        imageView.setZoomEnabled(false);
        imageView.setQuickScaleEnabled(false);
        imageView.setMaxScale(Constants.MAX_IMAGE_SCALE);
        imageView.setParallelLoadingEnabled(true);
    }

    @NonNull
    public static BitmapFactory.Options createOptions(@NonNull final File file, final int maxWidth, final int maxHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = 1;
        while (options.outWidth / options.inSampleSize > maxWidth || options.outHeight / options.inSampleSize > maxHeight) options.inSampleSize *= 2;

        options.inJustDecodeBounds = false;

        return options;
    }
}
