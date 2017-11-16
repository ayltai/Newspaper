package com.github.ayltai.newspaper.client;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class HketClient extends RssClient {
    //region Constants

    private static final String CHINA_BASE_URI         = "http://china.hket.com/";
    private static final String INVEST_BASE_URI        = "http://invest.hket.com/";
    private static final String PAPER_BASE_URI         = "http://paper.hket.com/";
    private static final String INTERNATIONAL_BASE_URI = "http://inews.hket.com/";

    private static final String TAG_DATA_SRC  = "data-src=\"";
    private static final String TAG_PARAGRAPH = "</p>";
    private static final String TAG_QUOTE     = "\"";

    //endregion

    @Inject
    HketClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @SuppressWarnings("CyclomaticComplexity")
    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        final boolean isChinaNews         = item.getLink().startsWith(HketClient.CHINA_BASE_URI);
        final boolean isInvestNews        = item.getLink().startsWith(HketClient.INVEST_BASE_URI);
        final boolean isPaperNews         = item.getLink().startsWith(HketClient.PAPER_BASE_URI);
        final boolean isInternationalNews = item.getLink().startsWith(HketClient.INTERNATIONAL_BASE_URI);

        return Single.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    if (isChinaNews || isInvestNews) {
                        html = StringUtils.substringBetween(html, "<div id=\"content-main\">", "<div class=\"fb-page-like\">");
                    } else if (isPaperNews || isInternationalNews) {
                        html = StringUtils.substringBetween(html, "<div id=\"eti-article-content-body\"", "<div class=\"fb-like\"");
                    } else {
                        html = StringUtils.substringBetween(html, "<div class=\"article-detail\">", "<div class=\"article-detail_facebook-like\">");
                    }

                    if (html == null) {
                        if (!emitter.isDisposed()) emitter.onError(new ParseException("Unparseable content", 0));
                    } else {
                        HketClient.extraImages(html, item);

                        final String videoId = StringUtils.substringBetween(html, " src=\"//www.youtube.com/embed/", "?rel=0");
                        if (videoId != null) item.setVideo(new Video("https://www.youtube.com/watch?v=" + videoId, String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", videoId)));

                        final String[]      contents = StringUtils.substringsBetween(html, "<p>", HketClient.TAG_PARAGRAPH);
                        final StringBuilder builder  = new StringBuilder();

                        for (final String content : contents) {
                            if (!TextUtils.isEmpty(content)) builder.append(content).append("<br>");
                        }

                        item.setDescription(builder.toString());
                        item.setIsFullDescription(true);

                        if (!emitter.isDisposed()) emitter.onSuccess(item);
                    }
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), error);

                    if (!emitter.isDisposed()) emitter.onError(error);
                }
            ));
    }

    private static void extraImages(@NonNull final String html, @NonNull final Item item) {
        final String[]    imageContainers = StringUtils.substringsBetween(html, "<img ", "/>");
        final List<Image> images          = new ArrayList<>();

        for (final String imageContainer : imageContainers) {
            final String imageUrl         = StringUtils.substringBetween(imageContainer, HketClient.TAG_DATA_SRC, HketClient. TAG_QUOTE);
            final String imageDescription = StringUtils.substringBetween(imageContainer, "alt=\"", HketClient.TAG_QUOTE);

            if (imageUrl != null) images.add(new Image(imageUrl, imageDescription));
        }

        if (!images.isEmpty()) {
            item.getImages().clear();
            item.getImages().addAll(images);
        }
    }
}
