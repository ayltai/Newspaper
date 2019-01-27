package com.github.ayltai.newspaper.util;

import javax.annotation.Nonnull;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DeviceUtils {
    @Nonnull
    @NonNull
    public Point getScreenSize(@Nonnull @NonNull @lombok.NonNull final Context context) {
        final DisplayMetrics metrics = new DisplayMetrics();

        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        return new Point(metrics.widthPixels, metrics.heightPixels);
    }
}
