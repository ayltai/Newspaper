package com.github.ayltai.newspaper.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;

public final class IOUtils {
    private static final String TAG = IOUtils.class.getSimpleName();

    private IOUtils() {
    }

    public static String readString(@NonNull final InputStream inputStream) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final byte[]        buffer  = new byte[Constants.FILE_BUFFER_SIZE];

        int length;

        while ((length = inputStream.read(buffer)) > -1) builder.append(new String(buffer, 0, length, Constants.ENCODING_UTF8));

        return builder.toString();
    }

    public static long copy(@NonNull final InputStream inputStream, @NonNull final OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];

        long total = 0;
        int  count;

        while ((count = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, count);

            total += count;
        }

        return total;
    }

    public static void closeQuietly(@Nullable final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (DevUtils.isLoggable()) Log.e(IOUtils.TAG, e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));
            }
        }
    }
}
