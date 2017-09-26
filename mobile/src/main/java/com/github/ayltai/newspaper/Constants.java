package com.github.ayltai.newspaper;

import android.support.annotation.IntDef;

public final class Constants {
    public static final String ENCODING_UTF8 = "UTF-8";

    public static final int INITIAL_RETRY_DELAY = 2;
    public static final int MAX_RETRIES         = 5;
    public static final int CONNECTION_TIMEOUT  = 5;
    public static final int REFRESH_TIMEOUT     = 30;
    public static final int HOUSEKEEP_TIME      = 72 * 60 * 60 * 1000;

    public static final int  FILE_BUFFER_SIZE        = 4 * 1024;
    public static final long CACHE_SIZE_MAX          = 32 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALL    = 16 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLER  = 8 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLEST = 4 * 1024 * 1024;

    public static final int FEATURED_IMAGE_ROTATION = 5;
    public static final int IMAGE_ZOOM_MAX          = 8;

    public static final int VIEW_STYLE_COMPACT = 0;
    public static final int VIEW_STYLE_COZY    = 1;
    public static final int VIEW_STYLE_DEFAULT = VIEW_STYLE_COZY;

    @IntDef({ Constants.VIEW_STYLE_COMPACT, Constants.VIEW_STYLE_COZY })
    public @interface ViewStyle {
    }

    public static final int THEME_LIGHT   = 0;
    public static final int THEME_DARK    = 1;
    public static final int THEME_DEFAULT = THEME_LIGHT;

    @IntDef({ Constants.THEME_LIGHT, Constants.THEME_DARK })
    public @interface Theme {
    }

    public static final boolean AUTO_PLAY_DEFAULT = true;

    private Constants() {
    }
}
