package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.analytics.EventLogger;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
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

        return Single.create(emitter -> emitter.onSuccess(this.getSettings(ComponentFactory.getInstance()
            .getConfigComponent(activity)
            .userConfig())));
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    settings -> {
                        final Activity activity = view.getActivity();

                        if (activity != null) {
                            final UserConfig userConfig = ComponentFactory.getInstance()
                                .getConfigComponent(activity)
                                .userConfig();

                            final EventLogger eventLogger = ComponentFactory.getInstance()
                                .getAnalyticsComponent(view.getContext())
                                .eventLogger();

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

                                default:
                                    break;
                            }
                        }
                    }
                ),
            error -> {
                if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
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
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }
                );
        }
    }

    @NonNull
    private List<Boolean> getSettings(@Nullable final UserConfig userConfig) {
        final List<Boolean> settings = new ArrayList<>();

        settings.add(userConfig == null || userConfig.getViewStyle() == Constants.VIEW_STYLE_DEFAULT);
        settings.add(userConfig != null && userConfig.getTheme() != Constants.THEME_DEFAULT);
        settings.add(userConfig != null && userConfig.isAutoPlayEnabled());

        return settings;
    }

    @VisibleForTesting
    protected void updateViewStyle(@Nullable final List<Boolean> settings, @Nullable final UserConfig userConfig, @Nullable final EventLogger eventLogger) {
        final boolean isCozyLayout = settings == null || settings.isEmpty() ? true : settings.get(SettingsPresenter.INDEX_LAYOUT);
        if (userConfig != null) userConfig.setViewStyle(isCozyLayout ? Constants.VIEW_STYLE_COMPACT : Constants.VIEW_STYLE_COZY);

        if (eventLogger != null) eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isCozyLayout ? "Compact" : "Cozy") + " Layout"));
    }

    @VisibleForTesting
    protected void updateTheme(@Nullable final List<Boolean> settings, @Nullable final UserConfig userConfig, @Nullable final EventLogger eventLogger) {
        final boolean isDarkTheme = settings == null || settings.isEmpty() ? false : settings.get(SettingsPresenter.INDEX_THEME);
        if (userConfig != null) userConfig.setTheme(isDarkTheme ? Constants.THEME_LIGHT : Constants.THEME_DARK);

        if (eventLogger != null) eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isDarkTheme ? "Light" : "Dark") + " Theme"));
    }

    @VisibleForTesting
    protected void updateAutoPlay(@Nullable final List<Boolean> settings, @Nullable final UserConfig userConfig, @Nullable final EventLogger eventLogger) {
        final boolean isAutoPlayEnabled = settings == null || settings.isEmpty() ? false : settings.get(SettingsPresenter.INDEX_AUTO_PLAY);
        if (userConfig != null) userConfig.setAutoPlayEnabled(!isAutoPlayEnabled);

        if (eventLogger != null) eventLogger.logEvent(new ClickEvent().setElementName("Settings - " + (isAutoPlayEnabled ? "Auto Play Disabled" : "Auto Play Enabled")));
    }
}
