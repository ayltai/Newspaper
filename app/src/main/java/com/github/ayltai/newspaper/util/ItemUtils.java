package com.github.ayltai.newspaper.util;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.rss.Item;

public final class ItemUtils {
    private ItemUtils() {
    }

    public static boolean filter(@NonNull final Item item) {
        if (!TextUtils.isEmpty(item.getLink())) return item.getLink().contains("promotions") && item.getLink().contains("unified");
        if (!TextUtils.isEmpty(item.getDescription())) return item.getDescription().contains("promotions") && item.getDescription().contains("unified");

        return false;
    }

    @NonNull
    public static CharSequence removeHtml(@NonNull final String value) {
        //noinspection deprecation
        return Html.fromHtml(value.replaceAll("(<(/)img>)|(<img.+?>)", Constants.EMPTY)
            .replaceAll("　", "<br />")
            .replaceAll(" ...", Constants.EMPTY));
    }

    @NonNull
    public static String getOriginalMediaUrl(@NonNull final String url) {
        final int index = url.lastIndexOf("http://media.zenfs.com/");

        if (index == -1) return url;

        return url.substring(index);
    }
}
