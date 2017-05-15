package com.github.ayltai.newspaper.main;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.LogUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

public /* final */ class MainAdapter extends PagerAdapter implements Closeable {
    //region Variables

    private final SparseArrayCompat<String> categories = new SparseArrayCompat<>();
    private final SparseArrayCompat<View>   views      = new SparseArrayCompat<>();
    private final Context                   context;
    private final MainComponent             component;

    @Inject Realm realm;

    private CompositeDisposable disposables;

    //endregion

    @Inject
    public MainAdapter(@NonNull final Context context) {
        this.context   = context;
        this.component = DaggerMainComponent.builder().mainModule(new MainModule((Activity)this.context)).build();

        DaggerDataComponent.builder()
            .build()
            .inject(this);

        final List<String> categories = new ArrayList<>(Settings.getPreferenceCategories(this.context));

        int i;
        for (i = 0; i < Constants.CATEGORY_COUNT; i++) this.categories.put(i, categories.get(i));
        this.categories.put(i, this.context.getString(R.string.title_bookmark));
    }

    @Override
    public /* final */ int getCount() {
        return this.categories.size();
    }

    @Override
    public final boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public final Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final ListPresenter presenter = this.component.listPresenter();
        final ListScreen    view      = (ListScreen)this.component.listView();

        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.disposables.add(view.attachments().subscribe(
            dummy -> {
                presenter.onViewAttached(view);
                presenter.bind(this.realm, new ListScreen.Key(position == this.categories.size() - 1 ? Constants.CATEGORY_BOOKMARK : this.categories.get(position)));
            },
            error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.disposables.add(view.detachments().subscribe(dummy -> presenter.onViewDetached(), error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.views.put(position, view);
        container.addView(view);

        return view;
    }

    @Override
    public final void destroyItem(@NonNull final ViewGroup container, final int position, final Object object) {
        final View view = this.views.get(position);

        if (view != null) this.closeView(view);

        if (this.views.indexOfKey(position) > -1) container.removeView(view);
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position) {
        return this.categories.get(position);
    }

    @Override
    public /* final */ void close() {
        for (int i = 0; i < this.views.size(); i++) this.closeView(this.views.get(this.views.keyAt(i)));

        if (this.disposables != null && !this.disposables.isDisposed() && this.disposables.size() > 0) {
            this.disposables.dispose();
            this.disposables = null;
        }

        for (int i = 0; i < this.views.size(); i++) this.closeView(this.views.get(this.views.keyAt(i)));

        this.views.clear();

        if (!this.realm.isClosed()) this.realm.close();
    }

    private void closeView(@NonNull final View view) {
        if (view instanceof Closeable) {
            try {
                ((Closeable)view).close();
            } catch (final IOException e) {
                LogUtils.getInstance().e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
