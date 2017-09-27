package com.github.ayltai.newspaper.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Sets;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class UserConfig {
    //region Constants

    private static final String KEY_SOURCES    = "sources";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_VIEW_STYLE = "viewStyle";
    private static final String KEY_THEME      = "theme";
    private static final String KEY_AUTO_PLAY  = "autoPlay";

    //endregion

    //region Subscriptions

    private static final FlowableProcessor<Boolean> VIDEO_PLAYBACK_STATE_CHANGES = PublishProcessor.create();
    private static final FlowableProcessor<Long>    VIDEO_SEEK_POSITION_CHANGES  = PublishProcessor.create();

    //endregion

    //region Global app states

    private static final AtomicBoolean VIDEO_IS_PLAYING    = new AtomicBoolean(false);
    private static final AtomicLong    VIDEO_SEEK_POSITION = new AtomicLong(0);

    //endregion

    private UserConfig() {
    }

    @NonNull
    public static Set<String> getSources(@NonNull final Context context) {
        final Set<String> sources = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(UserConfig.KEY_SOURCES, null);
        return sources == null ? UserConfig.getDefaultSources(context) : sources;
    }

    public static void setSources(@NonNull final Context context, @NonNull final Set<String> sources) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(UserConfig.KEY_SOURCES, sources).apply();
    }

    @NonNull
    public static Set<String> getDefaultSources(@NonNull final Context context) {
        return Sets.from(context.getResources().getStringArray(R.array.sources));
    }

    @NonNull
    public static List<String> getCategories(@NonNull final Context context) {
        final String json = PreferenceManager.getDefaultSharedPreferences(context).getString(UserConfig.KEY_CATEGORIES, null);
        if (json == null) return UserConfig.getDefaultCategories(context);

        return new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public static void setCategories(@NonNull final Context context, @NonNull final List<String> categories) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(UserConfig.KEY_CATEGORIES, new Gson().toJson(categories)).apply();
    }

    @NonNull
    public static List<String> getDefaultCategories(@NonNull final Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.categories));
    }

    @SuppressWarnings("WrongConstant")
    @Constants.ViewStyle
    public static int getViewStyle(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(UserConfig.KEY_VIEW_STYLE, Constants.VIEW_STYLE_DEFAULT);
    }

    public static void setViewStyle(@NonNull final Context context, @Constants.ViewStyle final int viewStyle) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(UserConfig.KEY_VIEW_STYLE, viewStyle).apply();
    }

    @SuppressWarnings("WrongConstant")
    @Constants.Theme
    public static int getTheme(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(UserConfig.KEY_THEME, Constants.THEME_DEFAULT);
    }

    public static void setTheme(@NonNull final Context context, @Constants.Theme final int theme) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(UserConfig.KEY_THEME, theme).apply();
    }

    public static boolean isAutoPlayEnabled(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(UserConfig.KEY_AUTO_PLAY, Constants.AUTO_PLAY_DEFAULT);
    }

    public static void setAutoPlayEnabled(@NonNull final Context context, final boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(UserConfig.KEY_AUTO_PLAY, enabled).apply();
    }

    public static boolean isVideoPlaying() {
        return VIDEO_IS_PLAYING.get();
    }

    public static void setVideoPlaying(final boolean isPlaying) {
        VIDEO_IS_PLAYING.set(isPlaying);

        UserConfig.VIDEO_PLAYBACK_STATE_CHANGES.onNext(isPlaying);
    }

    public static long getVideoSeekPosition() {
        return VIDEO_SEEK_POSITION.get();
    }

    public static void setVideoSeekPosition(final long seekPosition) {
        VIDEO_SEEK_POSITION.set(seekPosition);

        UserConfig.VIDEO_SEEK_POSITION_CHANGES.onNext(seekPosition);
    }

    @NonNull
    public static Flowable<Boolean> videoPlaybackStateChanges() {
        return UserConfig.VIDEO_PLAYBACK_STATE_CHANGES;
    }

    @NonNull
    public static Flowable<Long> videoSeekPositionChanges() {
        return UserConfig.VIDEO_SEEK_POSITION_CHANGES;
    }
}
