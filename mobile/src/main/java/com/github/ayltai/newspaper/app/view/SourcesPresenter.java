package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class SourcesPresenter extends OptionsPresenter<String, OptionsPresenter.View> {
    private final List<Boolean> selectedSources = new ArrayList<>();

    private List<String> sources;

    @NonNull
    @Override
    protected Single<List<String>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            final List<String> sources = new ArrayList<>(UserConfig.getDefaultSources(this.getView().getContext()));

            Collections.sort(sources);

            emitter.onSuccess(sources);
        });
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                this.selectedSources.set(index, !this.selectedSources.get(index));

                final List<String> sources = new ArrayList<>();

                for (int i = 0; i < this.sources.size(); i++) {
                    if (this.selectedSources.get(i)) sources.add(this.sources.get(i));
                }

                this.optionsChanges.onNext(sources);
            },
            error -> {
                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        if (isFirstTimeAttachment) {
            this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    sources -> {
                        this.sources = sources;
                        this.selectedSources.clear();

                        final Set<String> selectedSources = UserConfig.getSources(view.getContext());

                        for (int i = 0; i < sources.size(); i++) {
                            this.selectedSources.add(selectedSources.contains(sources.get(i)));

                            view.addOption(sources.get(i), this.selectedSources.get(i));
                        }
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }
}
