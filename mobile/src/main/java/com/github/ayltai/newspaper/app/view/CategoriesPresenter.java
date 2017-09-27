package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.data.model.Category;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class CategoriesPresenter extends OptionsPresenter<String, OptionsPresenter.View> {
    private final List<Boolean> selectedCategories = new ArrayList<>();

    private List<String> categories;

    @NonNull
    @Override
    protected Single<List<String>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            final List<String> categories   = new ArrayList<>(UserConfig.getDefaultCategories(this.getView().getContext()));
            final List<String> displayNames = new ArrayList<>();

            for (final String category : categories) {
                final String displayName = Category.toDisplayName(category);
                if (!displayNames.contains(displayName)) displayNames.add(displayName);
            }

            emitter.onSuccess(new ArrayList<>(displayNames));
        });
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.optionsChanges().subscribe(
            index -> {
                this.selectedCategories.set(index, !this.selectedCategories.get(index));

                final List<String> categories = new ArrayList<>();

                for (int i = 0; i < this.categories.size(); i++) {
                    if (this.selectedCategories.get(i)) categories.addAll(Category.fromDisplayName(this.categories.get(i)));
                }

                UserConfig.setCategories(view.getContext(), categories);
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
                        this.selectedCategories.clear();

                        final List<String> selectedCategories = UserConfig.getCategories(view.getContext());

                        for (int i = 0; i < categories.size(); i++) {
                            this.selectedCategories.add(selectedCategories.contains(categories.get(i)));

                            view.addOption(categories.get(i), this.selectedCategories.get(i));
                        }
                    },
                    error -> {
                        if (TestUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), error);
                    }
                );
        }
    }
}
