package com.github.ayltai.newspaper.client;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.data.model.Item;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.StringUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Maybe;
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
    public Maybe<Item> updateItem(@NonNull final Item item) {
        return Maybe.create(emitter -> this.apiService
            .getHtml(item.getLink())
            .compose(RxUtils.applyObservableBackgroundSchedulers())
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

                    emitter.onSuccess(item);
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);

                    emitter.onError(error);
                }
            ));
    }
}
