package com.github.ayltai.newspaper;

public final class Constants {
    public static final String ENCODING_UTF8 = "UTF-8";

    public static final int  FILE_BUFFER_SIZE        = 4 * 1024;
    public static final long CACHE_SIZE_MAX          = 32 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALL    = 16 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLER  = 8 * 1024 * 1024;
    public static final long CACHE_SIZE_MAX_SMALLEST = 4 * 1024 * 1024;

    private Constants() {
    }
}
