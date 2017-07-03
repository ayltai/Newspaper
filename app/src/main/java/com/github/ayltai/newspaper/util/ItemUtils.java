package com.github.ayltai.newspaper.util;

import java.util.List;

import android.support.annotation.NonNull;
import android.text.Html;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.model.Item;

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

    public static int indexOf(@NonNull final List<Item> items, @NonNull final Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (item.getLink().equals(items.get(i).getLink())) return i;
        }

        return -1;
    }

    public static boolean contains(@NonNull final List<Item> items, @NonNull final Item item) {
        return ItemUtils.indexOf(items, item) >= 0;
    }

    public static boolean isYouTube(@NonNull final String url) {
        return url.startsWith("https://www.youtube.com/");
    }
}
