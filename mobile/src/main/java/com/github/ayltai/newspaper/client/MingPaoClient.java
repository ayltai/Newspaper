package com.github.ayltai.newspaper.client;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.net.ApiService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

final class MingPaoClient extends RssClient {
    //region Constants

    private static final String BASE_URI     = "https://news.mingpao.com/";
    private static final String BASE_IMAGE   = "https://fs.mingpao.com/";
    private static final String SLASH        = "/";
    private static final String UNDERSCORE   = "_";
    private static final String DATA         = "dat/";
    private static final String TAG_URL      = "URL";
    private static final String ONE_SLASH    = "1/";
    private static final String JS_EXTENSION = ".js";

    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_VIDEO = "video";

    //endregion

    @Inject
    MingPaoClient(@NonNull final OkHttpClient client, @NonNull final ApiService apiService, @NonNull final Source source) {
        super(client, apiService, source);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @WorkerThread
    @NonNull
    @Override
    public Single<NewsItem> updateItem(@NonNull final NewsItem item) {
        final String[] tokens    = item.getLink().substring(MingPaoClient.BASE_URI.length()).split(MingPaoClient.SLASH);
        final boolean  isInstant = item.getLink().contains("/ins/");

        final Observable<String> url = isInstant
            ? this.apiService
            .getHtml(MingPaoClient.BASE_URI + MingPaoClient.DATA + tokens[0] + MingPaoClient.SLASH + tokens[0] + MingPaoClient.UNDERSCORE + tokens[2] + MingPaoClient.SLASH + tokens[3] + MingPaoClient.ONE_SLASH + tokens[4] + "/content_" + tokens[6] + MingPaoClient.JS_EXTENSION)
            : this.apiService
            .getHtml(MingPaoClient.BASE_URI + MingPaoClient.DATA + tokens[0] + "/issuelist" + MingPaoClient.JS_EXTENSION)
            .compose(RxUtils.applyObservableBackgroundSchedulers())
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry))
            .map(html -> MingPaoClient.BASE_URI + MingPaoClient.DATA + tokens[0] + MingPaoClient.SLASH + tokens[0] + MingPaoClient.UNDERSCORE + tokens[2] + MingPaoClient.SLASH + tokens[3] + MingPaoClient.ONE_SLASH + tokens[4] + new JSONObject(html).getJSONObject((tokens[0] + MingPaoClient.UNDERSCORE + tokens[2]).toUpperCase()).getJSONObject("1 " + tokens[4]).getString("E").toLowerCase() + "/todaycontent_" + tokens[6] + MingPaoClient.JS_EXTENSION)
            .flatMap(this.apiService::getHtml)
            .retryWhen(RxUtils.exponentialBackoff(Constants.INITIAL_RETRY_DELAY, Constants.MAX_RETRIES, NetworkUtils::shouldRetry));

        return Single.create(emitter -> url.compose(RxUtils.applyObservableBackgroundSchedulers())
            .map(JSONObject::new)
            .subscribe(
                json -> {
                    final JSONArray images = json.getJSONArray("media:group");

                    if (images != null) {
                        final List<Image> fullImages = MingPaoClient.extractImages(item, images);

                        if (!fullImages.isEmpty()) {
                            item.getImages().clear();
                            item.getImages().addAll(fullImages);
                        }
                    }

                    item.setDescription(json.getString("DESCRIPTION"));
                    item.setIsFullDescription(true);

                    emitter.onSuccess(item);
                },
                error -> {
                    if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), "Error URL = " + item.getLink(), error);

                    emitter.onError(error);
                }
            ));
    }

    @NonNull
    private static List<Image> extractImages(@NonNull final NewsItem item, @NonNull final JSONArray images) {
        final List<Image> fullImages = new ArrayList<>();

        for (int i = 0; i < images.length(); i++) {
            try {
                final JSONObject image = images.getJSONObject(i);
                final JSONArray  array = image.getJSONArray("media:content");

                if (array != null && array.length() > 0) {
                    final Image img = MingPaoClient.findLargestImage(item, image, array);

                    if (img != null) fullImages.add(img);
                }
            } catch (final JSONException e) {
                if (TestUtils.isLoggable()) Log.e(MingPaoClient.class.getSimpleName(), e.getMessage(), e);
            }
        }

        return fullImages;
    }

    @Nullable
    private static Image findLargestImage(@NonNull final NewsItem item, @NonNull final JSONObject image, @NonNull final JSONArray array) {
        Image img = null;
        int   max = 0;

        for (int j = 0; j < array.length(); j++) {
            try {
                final JSONObject obj    = array.getJSONObject(j).getJSONObject("ATTRIBUTES");
                final String     type   = obj.getString("MEDIUM");
                final int        height = obj.getInt("HEIGHT");

                if (MingPaoClient.TYPE_IMAGE.equals(type) && height > max) {
                    img = new Image(MingPaoClient.BASE_IMAGE + obj.getString(MingPaoClient.TAG_URL), image.getString("media:title"));
                    max = height;
                } else if (MingPaoClient.TYPE_VIDEO.equals(type)) {
                    final String videoUrl = obj.getString(MingPaoClient.TAG_URL);
                    item.setVideo(new Video(videoUrl, videoUrl.replace("mp4", "jpg")));
                }
            } catch (final JSONException e) {
                if (TestUtils.isLoggable()) Log.e(MingPaoClient.class.getSimpleName(), e.getMessage(), e);
            }
        }

        return img;
    }
}
