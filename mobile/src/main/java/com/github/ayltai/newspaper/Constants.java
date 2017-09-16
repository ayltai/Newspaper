package com.github.ayltai.newspaper;

import android.support.annotation.IntDef;

public final class Constants {
    public static final String ENCODING_UTF8 = "UTF-8";

    public static final int CONNECTION_TIMEOUT = 5;
    public static final int HOUSEKEEP_TIME     = 72 * 60 * 60 * 1000;

    public static final int  FILE_BUFFER_SIZE        = 4 * 1024;
    public static final long CACHE_SIZE_MAX          = 32 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALL    = 16 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLER  = 8 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLEST = 4 * 1024 * 1024;

    public static final int IMAGE_ZOOM_MAX = 8;

    public static final int VIEW_STYLE_COMPACT = 0;
    public static final int VIEW_STYLE_COZY    = 1;

    @IntDef({ Constants.VIEW_STYLE_COMPACT, Constants.VIEW_STYLE_COZY })
    public @interface ViewStyle {
    }

    private Constants() {
    }
}
