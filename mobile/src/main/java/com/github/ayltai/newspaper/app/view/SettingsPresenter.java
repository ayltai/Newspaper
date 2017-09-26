package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class SettingsPresenter extends OptionsPresenter<Boolean, OptionsPresenter.View> {
    //region Constants

    private static final int INDEX_LAYOUT    = 0;
    private static final int INDEX_THEME     = 1;
    private static final int INDEX_AUTO_PLAY = 2;

    //endregion

    @NonNull
    @Override
    protected Single<List<Boolean>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            emitter.onSuccess(this.getSettings());
        });
    }

    @Override
    public void onViewAttached(@NonNull final View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                switch (index) {
                    case SettingsPresenter.INDEX_LAYOUT:
                        UserConfig.setViewStyle(view.getContext(), this.getSettings().get(SettingsPresenter.INDEX_LAYOUT) ? Constants.VIEW_STYLE_COMPACT : Constants.VIEW_STYLE_COZY);
                        break;

                    case SettingsPresenter.INDEX_THEME:
                        UserConfig.setTheme(view.getContext(), this.getSettings().get(SettingsPresenter.INDEX_THEME) ? Constants.THEME_LIGHT : Constants.THEME_DARK);
                        break;

                    case SettingsPresenter.INDEX_AUTO_PLAY:
                        UserConfig.setAutoPlayEnabled(view.getContext(), !this.getSettings().get(SettingsPresenter.INDEX_AUTO_PLAY));
                        break;

                    default:
                        break;
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
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }

    @NonNull
    private List<Boolean> getSettings() {
        if (this.getView() == null) return Collections.emptyList();

        final List<Boolean> settings = new ArrayList<>();

        settings.add(UserConfig.getViewStyle(this.getView().getContext()) == Constants.VIEW_STYLE_DEFAULT);
        settings.add(UserConfig.getTheme(this.getView().getContext()) != Constants.THEME_DEFAULT);
        settings.add(UserConfig.isAutoPlayEnabled(this.getView().getContext()));

        return settings;
    }
}
