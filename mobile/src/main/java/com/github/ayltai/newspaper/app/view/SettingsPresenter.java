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
            index -> this.optionsChanges.onNext(this.getSettings()),
            error -> {
                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        if (isFirstTimeAttachment) {
            this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    settings -> {
                        view.addOption(view.getContext().getText(R.string.pref_cozy_layout), settings.get(0));
                        view.addOption(view.getContext().getText(R.string.pref_dark_theme), settings.get(1));
                        view.addOption(view.getContext().getText(R.string.pref_auto_play), settings.get(2));
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
