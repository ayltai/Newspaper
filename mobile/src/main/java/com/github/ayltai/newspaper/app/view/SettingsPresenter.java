package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.analytics.EventLogger;
import com.github.ayltai.newspaper.app.config.ConfigModule;
import com.github.ayltai.newspaper.app.config.DaggerConfigComponent;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class SettingsPresenter extends OptionsPresenter<Boolean, OptionsPresenter.View> {
    //region Constants

    private static final int INDEX_LAYOUT    = 0;
    private static final int INDEX_THEME     = 1;
    private static final int INDEX_AUTO_PLAY = 2;
    private static final int INDEX_PANORAMA  = 3;

    //endregion

    @NonNull
    @Override
    protected Single<List<Boolean>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> emitter.onSuccess(this.getSettings(DaggerConfigComponent.builder().configModule(new ConfigModule(activity)).build().userConfig())));
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                final EventLogger eventLogger = DaggerAnalyticsComponent.builder()
                    .analyticsModule(new AnalyticsModule(view.getContext()))
                    .build()
                    .eventLogger();

                final Activity activity = view.getActivity();

                if (activity != null) {
                    final UserConfig userConfig = DaggerConfigComponent.builder()
                        .configModule(new ConfigModule(activity))
                        .build()
                        .userConfig();

                    final List<Boolean> settings = this.getSettings(userConfig);

                    switch (index) {
                        case SettingsPresenter.INDEX_LAYOUT:
                            this.updateViewStyle(settings, userConfig, eventLogger);
                            break;

                        case SettingsPresenter.INDEX_THEME:
                            this.updateTheme(settings, userConfig, eventLogger);
                            break;

                        case SettingsPresenter.INDEX_AUTO_PLAY:
                            this.updateAutoPlay(settings, userConfig, eventLogger);
                            break;

                        case SettingsPresenter.INDEX_PANORAMA:
                            this.updatePanorama(settings, userConfig, eventLogger);
                            break;

                        default:
                            break;
                    }
                }
            },
            error -> {
                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        if (isFirstTimeAttachment) {
            this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    settings -> {
                        view.addOption(view.getContext().getText(R.string.pref_cozy_layout), settings.get(SettingsPresenter.INDEX_LAYOUT));
                        view.addOption(view.getContext().getText(R.string.pref_dark_theme), settings.get(SettingsPresenter.INDEX_THEME));
                        view.addOption(view.getContext().getText(R.string.pref_auto_play), settings.get(SettingsPresenter.INDEX_AUTO_PLAY));
                        view.addOption(view.getContext().getText(R.string.pref_panorama), settings.get(SettingsPresenter.INDEX_PANORAMA));
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }

    @NonNull
    private List<Boolean> getSettings(@NonNull final UserConfig userConfig) {
        if (this.getView() == null) return Collections.emptyList();

        final List<Boolean> settings = new ArrayList<>();

        settings.add(userConfig.getViewStyle() == Constants.VIEW_STYLE_DEFAULT);
        settings.add(userConfig.getTheme() != Constants.THEME_DEFAULT);
        settings.add(userConfig.isAutoPlayEnabled());
        settings.add(userConfig.isPanoramaEnabled());

        return settings;
    }

    private void updateViewStyle(@NonNull final List<Boolean> settings, @NonNull final UserConfig userConfig, @NonNull final EventLogger eventLogger) {
        final boolean isCozyLayout = settings.get(SettingsPresenter.INDEX_LAYOUT);
        userConfig.setViewStyle(isCozyLayout ? Constants.VIEW_STYLE_COMPACT : Constants.VIEW_STYLE_COZY);

        eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isCozyLayout ? "Compact" : "Cozy") + " Layout"));
    }

    private void updateTheme(@NonNull final List<Boolean> settings, @NonNull final UserConfig userConfig, @NonNull final EventLogger eventLogger) {
        final boolean isDarkTheme = settings.get(SettingsPresenter.INDEX_THEME);
        userConfig.setTheme(isDarkTheme ? Constants.THEME_LIGHT : Constants.THEME_DARK);

        eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isDarkTheme ? "Light" : "Dark") + " Theme"));
    }

    private void updateAutoPlay(@NonNull final List<Boolean> settings, @NonNull final UserConfig userConfig, @NonNull final EventLogger eventLogger) {
        final boolean isAutoPlayEnabled = settings.get(SettingsPresenter.INDEX_AUTO_PLAY);
        userConfig.setAutoPlayEnabled(!isAutoPlayEnabled);

        eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isAutoPlayEnabled ? "Auto Play Disabled" : "Auto Play Enabled")));
    }

    private void updatePanorama(@NonNull final List<Boolean> settings, @NonNull final UserConfig userConfig, @NonNull final EventLogger eventLogger) {
        final boolean isPanoramaEnabled = settings.get(SettingsPresenter.INDEX_PANORAMA);
        userConfig.setPanoramaEnabled(!isPanoramaEnabled);

        eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isPanoramaEnabled ? "Panorama Disabled" : "Panorama Enabled")));
    }
}
