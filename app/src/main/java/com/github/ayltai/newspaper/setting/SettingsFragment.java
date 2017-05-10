package com.github.ayltai.newspaper.setting;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

@SuppressFBWarnings("PDP_POORLY_DEFINED_PARAMETER")
public final class SettingsFragment extends PreferenceFragmentCompat {
    private static int resultCode = Activity.RESULT_CANCELED;

    @Override
    public void onCreatePreferences(@NonNull final Bundle savedInstanceState, final String rootKey) {
        this.getActivity().setResult(SettingsFragment.resultCode);
        SettingsFragment.resultCode = Activity.RESULT_CANCELED;

        this.addPreferencesFromResource(R.xml.preferences);

        final boolean     isCompactLayout = Settings.getListViewType(this.getContext()) == Constants.LIST_VIEW_TYPE_COMPACT;
        final boolean     isDarkTheme     = Settings.isDarkTheme(this.getContext());
        final Set<String> sources         = Settings.getSources(this.getContext());
        final Set<String> categories      = Settings.getCategories(this.getContext());

        this.findPreference(Settings.PREF_COMPACT_LAYOUT).setOnPreferenceChangeListener((preference, newValue) -> {
            if (isCompactLayout != (boolean)newValue) this.getActivity().setResult(Activity.RESULT_OK);

            return true;
        });

        this.findPreference(Settings.PREF_DARK_THEME).setOnPreferenceChangeListener((preference, newValue) -> {
            if (isDarkTheme != (boolean)newValue) {
                SettingsFragment.resultCode = Activity.RESULT_OK;

                this.getActivity().recreate();
            }

            return true;
        });

        this.findPreference(Settings.PREF_PANORAMA_ENABLED).setEnabled(Settings.canPanoramaBeEnabled(this.getContext()));

        this.findPreference(Settings.PREF_SOURCES).setOnPreferenceChangeListener((preference, newValue) -> {
            final Set<String> newSources = (Set<String>)newValue;

            if (!sources.containsAll(newSources) || !newSources.containsAll(sources)) {
                SettingsFragment.resultCode = Activity.RESULT_OK;
                this.getActivity().setResult(Activity.RESULT_OK);
            }

            return true;
        });

        this.findPreference(Settings.PREF_CATEGORIES).setOnPreferenceChangeListener((preference, newValue) -> {
            final Set<String> newCategories = (Set<String>)newValue;

            if (!categories.containsAll(newCategories) || !newCategories.containsAll(categories)) {
                SettingsFragment.resultCode = Activity.RESULT_OK;
                this.getActivity().setResult(Activity.RESULT_OK);
            }

            return true;
        });
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(ContextUtils.getColor(this.getContext(), R.attr.windowBackground));
    }
}
