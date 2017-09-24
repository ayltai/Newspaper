package com.github.ayltai.newspaper.app.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.CategoryFilterPresenter;
import com.github.ayltai.newspaper.app.view.SourceFilterPresenter;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.TextOptionsPresenter;
import com.github.ayltai.newspaper.widget.TextOptionsView;

import io.reactivex.disposables.Disposable;

public final class FilterAdapter extends PagerAdapter {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    private final Context context;

    public FilterAdapter(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return position == 0 ? this.context.getText(R.string.pref_sources) : this.context.getText(R.string.pref_categories);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object instantiateItem(final ViewGroup parent, final int position) {
        final TextOptionsPresenter presenter;
        final TextOptionsView      view;

        if (position == 0) {
            presenter = new SourceFilterPresenter();
            view      = new SourceFilterView(this.context);
        } else {
            presenter = new CategoryFilterPresenter();
            view      = new CategoryFilterView(this.context);
        }

        this.manageDisposable(view.attachments().subscribe(isFirstAttachment -> presenter.onViewAttached(view, isFirstAttachment)));
        this.manageDisposable(view.detachments().subscribe(irrelevant -> presenter.onViewDetached()));

        parent.addView(view);

        return view;
    }

    @Override
    public void destroyItem(final ViewGroup parent, final int position, final Object object) {
        parent.removeView((View)object);

        RxUtils.resetDisposables(this.disposables);
    }

    private void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }
}
