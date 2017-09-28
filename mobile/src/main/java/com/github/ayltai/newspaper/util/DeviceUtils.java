package com.github.ayltai.newspaper.util;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public final class DeviceUtils {
    private DeviceUtils() {
    }

    @NonNull
    public static Point getScreenSize(@NonNull final Context context) {
        final DisplayMetrics metrics = new DisplayMetrics();

        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        return new Point(metrics.widthPixels, metrics.heightPixels);
    }
}
