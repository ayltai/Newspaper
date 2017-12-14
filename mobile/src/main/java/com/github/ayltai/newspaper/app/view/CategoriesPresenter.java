package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.data.model.Category;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Single;

public class CategoriesPresenter extends OptionsPresenter<String, OptionsPresenter.View> {
    private final List<Boolean> selectedCategories = new ArrayList<>();

    private List<String> categories;

    @NonNull
    @Override
    protected Single<List<String>> load() {
        if (this.getView() == null) return Single.just(Collections.emptyList());

        final Activity activity = this.getView().getActivity();
        if (activity == null) return Single.just(Collections.emptyList());

        return Single.create(emitter -> {
            final List<String> categories   = new ArrayList<>(ComponentFactory.getInstance().getConfigComponent(activity).userConfig().getDefaultCategories());
            final List<String> displayNames = new ArrayList<>();

            for (final String category : categories) {
                final String displayName = Category.toDisplayName(category);
                if (!displayNames.contains(displayName)) displayNames.add(displayName);
            }

            if (!emitter.isDisposed()) emitter.onSuccess(new ArrayList<>(displayNames));
        });
    }

    @Override
    public void onViewAttached(@NonNull final OptionsPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        final Activity activity = view.getActivity();

        if (activity != null) {
            final UserConfig userConfig = ComponentFactory.getInstance()
                .getConfigComponent(activity)
                .userConfig();

            this.manageDisposable(view.optionsChanges().subscribe(
                index -> {
                    this.selectedCategories.set(index, !this.selectedCategories.get(index));

                    final List<String> categories = new ArrayList<>();

                    for (int i = 0; i < this.categories.size(); i++) {
                        if (this.selectedCategories.get(i)) categories.addAll(Category.fromDisplayName(this.categories.get(i)));
                    }

                    userConfig.setCategories(categories);
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }
            ));

            if (isFirstTimeAttachment) {
                this.load()
                    .compose(RxUtils.applySingleBackgroundToMainSchedulers())
                    .subscribe(
                        categories -> {
                            this.categories = categories;
                            this.selectedCategories.clear();

                            final List<String> selectedCategories = userConfig.getCategories();

                            for (int i = 0; i < categories.size(); i++) {
                                this.selectedCategories.add(selectedCategories.contains(categories.get(i)));

                                view.addOption(categories.get(i), this.selectedCategories.get(i));
                            }
                        },
                        error -> {
                            if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                        }
                    );
            }
        }
    }
}
