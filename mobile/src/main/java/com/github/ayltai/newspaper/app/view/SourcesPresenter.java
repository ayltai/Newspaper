package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.data.model.Source;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class SourcesPresenter extends OptionsPresenter<String, OptionsPresenter.View> {
    private final List<Boolean> selectedSources = new ArrayList<>();

    private List<String> sources;

    @NonNull
    @Override
    protected Single<List<String>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            final List<String> sources      = new ArrayList<>(ComponentFactory.getInstance().getConfigComponent(activity).userConfig().getDefaultSources());
            final Set<String>  displayNames = new HashSet<>();

            for (final String source : sources) displayNames.add(Source.toDisplayName(source));

            if (!emitter.isDisposed()) emitter.onSuccess(new ArrayList<>(displayNames));
        });
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Activity   activity   = view.getActivity();
        final UserConfig userConfig = activity == null
            ? null
            : ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .userConfig();

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                this.selectedSources.set(index, !this.selectedSources.get(index));

                final Set<String> sources = new ArraySet<>();

                for (int i = 0; i < this.sources.size(); i++) {
                    if (this.selectedSources.get(i)) sources.addAll(Source.fromDisplayName(this.sources.get(i)));
                }

                if (userConfig != null) userConfig.setSources(sources);
            },
            error -> {
                if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
            }
        ));

        if (isFirstTimeAttachment) {
            this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    sources -> {
                        this.sources = sources;
                        this.selectedSources.clear();

                        final Set<String> selectedSources = userConfig == null ? Collections.emptySet() : userConfig.getSources();

                        for (int i = 0; i < sources.size(); i++) {
                            this.selectedSources.add(selectedSources.contains(sources.get(i)));

                            view.addOption(sources.get(i), this.selectedSources.get(i));
                        }
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }
                );
        }
    }
}
