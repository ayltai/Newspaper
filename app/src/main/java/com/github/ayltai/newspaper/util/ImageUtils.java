package com.github.ayltai.newspaper.util;

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
    }
}
