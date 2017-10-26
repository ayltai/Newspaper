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
import com.github.ayltai.newspaper.app.view.CategoriesPresenter;
import com.github.ayltai.newspaper.app.view.SettingsPresenter;
import com.github.ayltai.newspaper.app.view.SourcesPresenter;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.view.OptionsPresenter;
import com.github.ayltai.newspaper.widget.SwitchOptionsView;
import com.github.ayltai.newspaper.widget.TextOptionsView;

import io.reactivex.disposables.Disposable;

public final class OptionsAdapter extends PagerAdapter {
    //region Constants

    private static final int POSITION_SOURCES    = 0;
    private static final int POSITION_CATEGORIES = 1;
    private static final int POSITION_SETTINGS   = 2;

    //endregion

    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    private final Context context;

    public OptionsAdapter(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return view == object;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case POSITION_SOURCES:
                return this.context.getText(R.string.pref_sources);

            case POSITION_CATEGORIES:
                return this.context.getText(R.string.pref_categories);

            case POSITION_SETTINGS:
                return this.context.getText(R.string.pref_settings);

            default:
                throw new IndexOutOfBoundsException("Position not found: " + position);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object instantiateItem(@NonNull final ViewGroup parent, final int position) {
        final OptionsPresenter      presenter;
        final OptionsPresenter.View view;

        switch (position) {
            case POSITION_SOURCES:
                presenter = new SourcesPresenter();
                view      = new TextOptionsView(this.context);

                break;

            case POSITION_CATEGORIES:
                presenter = new CategoriesPresenter();
                view      = new TextOptionsView(this.context);

                break;

            case POSITION_SETTINGS:
                presenter = new SettingsPresenter();
                view      = new SwitchOptionsView(this.context);
                break;

            default:
                throw new IndexOutOfBoundsException("Position not found: " + position);
        }

        this.manageDisposable(view.attachments().subscribe(isFirstAttachment -> presenter.onViewAttached(view, isFirstAttachment)));
        this.manageDisposable(view.detachments().subscribe(irrelevant -> presenter.onViewDetached()));

        parent.addView((View)view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup parent, final int position, @NonNull final Object object) {
        parent.removeView((View)object);

        RxUtils.resetDisposables(this.disposables);
    }

    private void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }
}
