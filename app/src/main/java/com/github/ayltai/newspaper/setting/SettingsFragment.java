package com.github.ayltai.newspaper.setting;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

@SuppressFBWarnings("PDP_POORLY_DEFINED_PARAMETER")
public final class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@NonNull final Bundle savedInstanceState, final String rootKey) {
        this.addPreferencesFromResource(R.xml.preferences);

        final boolean     isCompact  = Settings.getListViewType(this.getContext()) == Constants.LIST_VIEW_TYPE_COMPACT;
        final Set<String> categories = Settings.getCategories(this.getContext());

        this.findPreference(Settings.PREF_COMPACT_LAYOUT).setOnPreferenceChangeListener((preference, newValue) -> {
            if (isCompact != (boolean)newValue) this.getActivity().setResult(Activity.RESULT_OK);

            return true;
        });

        this.findPreference(Settings.PREF_CATEGORIES).setOnPreferenceChangeListener((preference, newValue) -> {
            final Set<String> newCategories = (Set<String>)newValue;

            if (!categories.containsAll(newCategories) || !newCategories.containsAll(categories)) this.getActivity().setResult(Activity.RESULT_OK);

            return true;
        });
    }
}
