package com.github.ayltai.newspaper;

import android.support.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public final class Configs {
    private static final String KEY_DEFAULT_LIST_VIEW_TYPE = "default_list_view_type";
    private static final String KEY_UPDATE_INTERVAL        = "update_interval";
    private static final String KEY_SCROLL_SNAP            = "scroll_snap";
    private static final String KEY_ITEM_ANIMATION         = "item_animation";

    private static FirebaseRemoteConfig config;

    @Constants.ListViewType
    public static int getDefaultListViewType() {
        if (Configs.config == null) return Constants.LIST_VIEW_TYPE_DEFAULT;

        return (int)Configs.config.getLong(Configs.KEY_DEFAULT_LIST_VIEW_TYPE);
    }

    public static int getUpdateInterval() {
        if (Configs.config == null) return Constants.UPDATE_INTERVAL;

        return (int)Configs.config.getLong(Configs.KEY_UPDATE_INTERVAL);
    }

    public static boolean isScrollSnapEnabled() {
        if (Configs.config == null) return Constants.SCROLL_SNAP;

        return Configs.config.getBoolean(Configs.KEY_SCROLL_SNAP);
    }

    public static boolean isItemAnimationEnabled() {
        if (Configs.config == null) return Constants.ITEM_ANIMATION;

        return Configs.config.getBoolean(Configs.KEY_ITEM_ANIMATION);
    }

    static void apply(@NonNull final FirebaseRemoteConfig config) {
        Configs.config = config;
    }
}
