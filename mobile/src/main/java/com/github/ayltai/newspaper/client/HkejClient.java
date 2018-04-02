package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;

import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class HkejClient extends RssClient {
    //region Constants

    private static final String TAG_OPEN  = "<a href=\"";
    private static final String TAG_QUOTE = "\"";

    //endregion

    @Inject
    HkejClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        return Single.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .subscribe(
                html -> {
                    final String imageContainer   = StringUtils.substringBetween(html, "<span class='enlargeImg'>", "</span>");
                    final String imageUrl         = StringUtils.substringBetween(imageContainer, HkejClient.TAG_OPEN, HkejClient.TAG_QUOTE);
                    final String imageDescription = StringUtils.substringBetween(imageContainer, "title=\"", HkejClient.TAG_QUOTE);

                    if (imageUrl != null) {
                        item.getImages().clear();
                        item.getImages().add(new Image(imageUrl, imageDescription));
                    }

                    final String[]      contents = StringUtils.substringsBetween(StringUtils.substringBetween(html, "<div id='article-content'>", "</div>"), ">", "<");
                    final StringBuilder builder  = new StringBuilder();

                    for (final String content : contents) builder.append(content).append("<br>");

                    item.setDescription(builder.toString());
                    item.setIsFullDescription(true);

                    if (!emitter.isDisposed()) emitter.onSuccess(item);
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), RxJava2Debug.getEnhancedStackTrace(error));

                    if (!emitter.isDisposed()) emitter.onSuccess(item);
                }
            ));
    }
}
