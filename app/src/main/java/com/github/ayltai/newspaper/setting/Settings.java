package com.github.ayltai.newspaper.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.facebook.common.internal.Sets;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

@SuppressFBWarnings("PMB_POSSIBLE_MEMORY_BLOAT")
public final class Settings {
    //region Constants

    static final String PREF_COMPACT_LAYOUT = "PREF_COMPACT_LAYOUT";
    static final String PREF_CATEGORIES     = "PREF_CATEGORIES";

    private static final String PREF_USER_ID = "PREF_USER_ID";

    //endregion

    private static final Map<String, Integer> POSITIONS = new HashMap<>();

    private Settings() {
    }

    public static String getUserId(@NonNull final Context context) {
        String userId = PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.PREF_USER_ID, null);

        if (TextUtils.isEmpty(userId)) {
            userId = UUID.randomUUID().toString();

            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Settings.PREF_USER_ID, userId).apply();
        }

        return userId;
    }

    @Constants.ListViewType
    public static int getListViewType(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_COMPACT_LAYOUT, true) ? Constants.LIST_VIEW_TYPE_COMPACT : Constants.LIST_VIEW_TYPE_COZY;
    }

    public static Set<String> getCategories(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(Settings.PREF_CATEGORIES, Sets.newHashSet(context.getResources().getStringArray(R.array.pref_category_values)));
    }

    public static int getPosition(@NonNull final String url) {
        if (Settings.POSITIONS.containsKey(url)) return Settings.POSITIONS.get(url);

        return 0;
    }

    public static void setPosition(@NonNull final String url, final int position) {
        Settings.POSITIONS.put(url, position);
    }
}
