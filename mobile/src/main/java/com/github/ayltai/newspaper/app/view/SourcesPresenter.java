package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.app.data.model.Source;
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
            final List<String> sources      = new ArrayList<>(UserConfig.getDefaultSources(this.getView().getContext()));
            final Set<String>  displayNames = new ArraySet<>();

            for (final String source : sources) displayNames.add(Source.toDisplayName(source));

            emitter.onSuccess(new ArrayList<>(displayNames));
        });
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                this.selectedSources.set(index, !this.selectedSources.get(index));

                final Set<String> sources = new ArraySet<>();

                for (int i = 0; i < this.sources.size(); i++) {
                    if (this.selectedSources.get(i)) sources.addAll(Source.fromDisplayName(this.sources.get(i)));
                }

                UserConfig.setSources(view.getContext(), sources);
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
