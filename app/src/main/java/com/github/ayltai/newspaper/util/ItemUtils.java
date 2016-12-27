package com.github.ayltai.newspaper.util;

import java.util.regex.Pattern;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.rss.Item;

public final class ItemUtils {
    private static final String IDEOGRAPHIC_SPACE = new String(new byte[] { (byte)227, (byte)128, (byte)128 }); // E3 80 80
    private static final String ELLIPSIS          = Pattern.quote(" ...");

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
            .replaceAll(ItemUtils.IDEOGRAPHIC_SPACE, "<br />")
            .replaceAll(ItemUtils.ELLIPSIS, Constants.EMPTY));
    }

    @NonNull
    public static String getOriginalMediaUrl(@NonNull final String url) {
        final int index = url.lastIndexOf("http://media.zenfs.com/");

        if (index == -1) return url;

        return url.substring(index);
    }
}
