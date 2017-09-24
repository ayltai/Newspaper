package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.TextOptionsPresenter;

import io.reactivex.Single;

public class CategoryFilterPresenter extends TextOptionsPresenter<String, CategoryFilterPresenter.View> {
    public interface View extends TextOptionsPresenter.View {
    }

    private final List<Boolean> selected = new ArrayList<>();

    private List<String> categories;

    @NonNull
    @Override
    protected Single<List<String>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            final List<String> categories = new ArrayList<>(UserConfig.getDefaultCategories(this.getView().getContext()));

            Collections.sort(categories);

            emitter.onSuccess(categories);
        });
    }

    @Override
    public void onViewAttached(@NonNull final CategoryFilterPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.selects().subscribe(
            index -> {
                this.selected.set(index, !this.selected.get(index));

                final List<String> categories = new ArrayList<>();

                for (int i = 0; i < this.categories.size(); i++) {
                    if (this.selected.get(i)) categories.add(this.categories.get(i));
                }

                this.selectionChanges.onNext(categories);
            },
            error -> {
                if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        if (isFirstTimeAttachment) {
            this.load()
                .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                .subscribe(
                    categories -> {
                        this.categories = categories;
                        this.selected.clear();

                        final List<String> selectedCategories = UserConfig.getCategories(view.getContext());

                        for (int i = 0; i < categories.size(); i++) {
                            this.selected.add(selectedCategories.contains(categories.get(i)));

                            view.addText(categories.get(i), this.selected.get(i));
                        }
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }
}
