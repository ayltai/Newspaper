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
import com.github.ayltai.newspaper.view.TextOptionsPresenter;

import io.reactivex.Single;

public class SourceFilterPresenter extends TextOptionsPresenter<String, SourceFilterPresenter.View> {
    public interface View extends TextOptionsPresenter.View {
    }

    private final List<Boolean> selected = new ArrayList<>();

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
    public void onViewAttached(@NonNull final SourceFilterPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.selects().subscribe(
            index -> {
                this.selected.set(index, !this.selected.get(index));

                final List<String> sources = new ArrayList<>();

                for (int i = 0; i < this.sources.size(); i++) {
                    if (this.selected.get(i)) sources.add(this.sources.get(i));
                }

                this.selectionChanges.onNext(sources);
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
                        this.selected.clear();

                        final Set<String> selectedSources = UserConfig.getSources(view.getContext());

                        for (int i = 0; i < sources.size(); i++) {
                            this.selected.add(selectedSources.contains(sources.get(i)));

                            view.addText(sources.get(i), this.selected.get(i));
                        }
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }
}
