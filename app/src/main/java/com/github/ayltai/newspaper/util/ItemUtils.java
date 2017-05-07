package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.text.Html;

import com.github.ayltai.newspaper.Constants;

@SuppressFBWarnings({ "DM_DEFAULT_ENCODING", "MDM_DEFAULT_ENCODING", "MDM_STRING_BYTES_ENCODING" })
@SuppressWarnings("checkstyle:magicnumber")
public final class ItemUtils {
    private ItemUtils() {
    }

    @NonNull
    public static CharSequence removeImages(@NonNull final String value) {
        //noinspection deprecation
        return Html.fromHtml(value.replaceAll("(<(/)img>)|(<img.+?>)", Constants.EMPTY));
    }

    @NonNull
    public static String removeHtml(@NonNull final String value) {
        return value.replaceAll("<h3>", "").replaceAll("</h3>", Constants.SPACE).replaceAll("<h4>", "").replaceAll("</h4>", Constants.SPACE).replaceAll("<br>", "\n");
    }
}
