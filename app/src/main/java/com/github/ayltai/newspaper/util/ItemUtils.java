package com.github.ayltai.newspaper.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.ContextModule;
import com.github.ayltai.newspaper.net.DaggerNetComponent;
import com.github.ayltai.newspaper.net.HttpClient;
import com.github.ayltai.newspaper.net.NetModule;
import com.github.ayltai.newspaper.rss.Item;

import rx.Observable;

@SuppressFBWarnings({ "DM_DEFAULT_ENCODING", "MDM_DEFAULT_ENCODING", "MDM_STRING_BYTES_ENCODING" })
@SuppressWarnings("checkstyle:magicnumber")
public final class ItemUtils {
    //region Constants

    private static final String IDEOGRAPHIC_SPACE          = new String(new byte[] { (byte)227, (byte)128, (byte)128 }); // E3 80 80
    private static final String ELLIPSIS                   = Pattern.quote(" ...");
    private static final String PHISHING_TITLE_1           = "【熱門搜尋】";
    private static final String PHISHING_TITLE_2           = "【人氣搜尋】";
    private static final String PHISHING_TITLE_3           = "【搜尋熱選】";
    private static final String PROMOTIONS                 = "promotions";
    private static final String UNIFIED                    = "unified";
    private static final String ORIGINAL_MEDIA_URL_TOKEN   = "http://media.zenfs.com/";
    private static final String FULL_DESCRIPTION_TAG_START = "<!-- google_ad_section_start -->";
    private static final String FULL_DESCRIPTION_TAG_END   = "<!-- google_ad_section_end -->";

    //endregion

    private ItemUtils() {
    }

    public static boolean filter(@NonNull final Item item) {
        if (!TextUtils.isEmpty(item.getLink()) && (item.getLink().contains(ItemUtils.PROMOTIONS) || item.getLink().contains(ItemUtils.UNIFIED))) return true;
        if (!TextUtils.isEmpty(item.getDescription()) && (item.getDescription().contains(ItemUtils.PROMOTIONS) || item.getDescription().contains(ItemUtils.UNIFIED))) return true;
        if (!TextUtils.isEmpty(item.getTitle()) && (item.getTitle().contains(ItemUtils.PHISHING_TITLE_1) || item.getTitle().contains(ItemUtils.PHISHING_TITLE_2) || item.getTitle().contains(ItemUtils.PHISHING_TITLE_3))) return true;

        return false;
    }

    @NonNull
    public static CharSequence removeHtml(@NonNull final String value) {
        //noinspection deprecation
        return Html.fromHtml(value.replaceAll("(<(/)img>)|(<img.+?>)", Constants.EMPTY)
            .replaceAll(ItemUtils.IDEOGRAPHIC_SPACE, "<br />")
            .replaceAll(ItemUtils.ELLIPSIS, Constants.EMPTY));
    }

    public static boolean hasOriginalMediaUrl(@NonNull final String url) {
        return url.lastIndexOf(ItemUtils.ORIGINAL_MEDIA_URL_TOKEN) > -1;
    }

    @NonNull
    public static String getOriginalMediaUrl(@NonNull final String url) {
        final int index = url.lastIndexOf(ItemUtils.ORIGINAL_MEDIA_URL_TOKEN);

        if (index == -1) return url;

        return url.substring(index);
    }

    @SuppressFBWarnings("CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME")
    @Nullable
    public static String getId(@NonNull final Item item) {
        if (item.getGuid() == null) return item.getTitle();

        try {
            return URLDecoder.decode(item.getGuid(), Constants.ENCODING_UTF8);
        } catch (final UnsupportedEncodingException e) {
            Log.w(ItemUtils.class.getSimpleName(), e.getMessage(), e);
        }

        return item.getTitle();
    }

    public static Observable<String> getFullDescription(@NonNull final Context context, @NonNull final Item item) {
        return Observable.create(subscriber -> {
            if (TextUtils.isEmpty(item.getLink())) {
                subscriber.onNext(item.getDescription());
            } else {
                final HttpClient client = DaggerNetComponent.builder()
                    .contextModule(new ContextModule(context))
                    .netModule(new NetModule())
                    .build()
                    .httpClient();

                InputStream inputStream = null;

                try {
                    final String html = IOUtils.toString(inputStream = client.download(item.getLink()), Constants.ENCODING_UTF8);
                    final int start = html.indexOf(ItemUtils.FULL_DESCRIPTION_TAG_START);

                    if (start >= 0) {
                        final int end = html.indexOf(ItemUtils.FULL_DESCRIPTION_TAG_END);

                        if (end >= 0) subscriber.onNext(html.substring(start + ItemUtils.FULL_DESCRIPTION_TAG_START.length(), end));
                    }
                } catch (final IOException e) {
                    subscriber.onError(e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        });
    }
}
