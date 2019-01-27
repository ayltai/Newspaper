package com.github.ayltai.newspaper.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.SourceManager;
import com.github.ayltai.newspaper.data.model.Category;
import com.github.ayltai.newspaper.data.model.Source;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

public final class ItemPagerPresenter extends BasePresenter<ItemPagerPresenter.View> {
    public interface View extends Presenter.View {
        @Nonnull
        @NonNull
        Flowable<Irrelevant> refreshActions();

        @Nonnull
        @NonNull
        Flowable<String> categoryChanges();

        void refresh();

        void setCategories(@Nonnull @NonNull List<String> categories);
    }

    private Disposable disposable;

    @CallSuper
    @Override
    public void onViewAttached(@Nonnull @NonNull @lombok.NonNull final ItemPagerPresenter.View view, final boolean isFirstAttachment) {
        super.onViewAttached(view, isFirstAttachment);

        this.disposable = SourceManager.create(view.getContext())
            .flatMap(SourceManager::get)
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .map(HashSet::new)
            .map(sources -> {
                final Set<Category> categories = new HashSet<>();
                for (final Source source : sources) categories.addAll(source.getCategories());
                return categories;
            })
            .map(categories -> {
                final List<Category> orderedCategories = new ArrayList<>(categories);
                Collections.sort(orderedCategories);
                return orderedCategories;
            })
            .map(categories -> {
                final List<String> names = new ArrayList<>();
                for (final Category category : categories) names.add(category.getDisplayName());
                return names;
            })
            .compose(RxUtils.applySingleBackgroundToMainSchedulers())
            .subscribe(view::setCategories);

        this.manageDisposable(view.refreshActions().subscribe(irrelevant -> view.refresh()));
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }
}
