package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.viewpager.widget.PagerAdapter;

import com.github.ayltai.newspaper.config.DaggerConfigsComponent;
import com.github.ayltai.newspaper.view.ListPresenter;
import com.github.ayltai.newspaper.view.Presenter;

final class ItemPagerAdapter extends PagerAdapter {
    private final Map<Object, Presenter<?>> presenters = new ArrayMap<>();
    private final List<String>              categories = new ArrayList<>();

    public ItemPagerAdapter(@Nonnull @NonNull @lombok.NonNull final List<String> categories) {
        this.categories.addAll(categories);
    }

    @Override
    public int getCount() {
        return this.categories.size();
    }

    @Override
    public boolean isViewFromObject(@Nonnull @NonNull @lombok.NonNull final View view, @Nonnull @NonNull @lombok.NonNull final Object object) {
        return view == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(final int position) {
        if (position < 0) return null;
        if (position < this.categories.size()) return this.categories.get(position);

        return null;
    }

    @Nonnull
    @NonNull
    @Override
    public Object instantiateItem(@Nonnull @NonNull @lombok.NonNull final ViewGroup container, final int position) {
        final ListPresenter presenter = new ListPresenter(DaggerConfigsComponent.create().userConfigs().getSourceNames(), this.categories.get(position));
        final ListView      view      = new CozyListView(container.getContext());

        container.addView(view);

        presenter.onViewAttached(view, true);

        this.presenters.put(view, presenter);

        return view;
    }

    @Override
    public void destroyItem(@Nonnull @NonNull @lombok.NonNull final ViewGroup container, final int position, @Nonnull @NonNull @lombok.NonNull final Object object) {
        final Presenter<?> presenter = this.presenters.remove(object);
        presenter.onViewDetached();

        container.removeView((View)object);
    }
}
