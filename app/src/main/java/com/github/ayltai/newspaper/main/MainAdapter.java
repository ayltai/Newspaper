package com.github.ayltai.newspaper.main;

import java.io.Closeable;
import java.io.IOException;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.ContextModule;
import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.LogUtils;

import io.realm.Realm;
import rx.subscriptions.CompositeSubscription;

public /* final */ class MainAdapter extends PagerAdapter implements Closeable {
    //region Variables

    private final SparseArrayCompat<String> categories = new SparseArrayCompat<>();
    private final SparseArrayCompat<View>   views      = new SparseArrayCompat<>();
    private final Context                   context;
    private final MainComponent             component;

    @Inject Realm realm;

    private CompositeSubscription subscriptions;

    //endregion

    @Inject
    public MainAdapter(@NonNull final Context context) {
        this.context   = context;
        this.component = DaggerMainComponent.builder().mainModule(new MainModule((Activity)this.context)).build();

        DaggerDataComponent.builder()
            .contextModule(new ContextModule(this.context))
            .dataModule(new DataModule(this.context))
            .build()
            .inject(this);

        // TODO: Do we need to hard-code categories
        final String[] categories = Settings.getCategories(this.context).toArray(new String[0]);

        int i;
        for (i = 0; i < categories.length; i++) this.categories.put(i, categories[i]);
        this.categories.put(i, this.context.getString(R.string.title_bookmark));
    }

    @Override
    public /* final */ int getCount() {
        return Settings.getCategories(this.context).size();
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

        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(view.attachments().subscribe(
            dummy -> {
                presenter.onViewAttached(view);
                presenter.bind(this.realm, new ListScreen.Key(this.categories.get(position)));
            },
            error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.subscriptions.add(view.detachments().subscribe(dummy -> presenter.onViewDetached(), error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

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
    public final CharSequence getPageTitle(final int position) {
        return this.categories.get(position);
    }

    @Override
    public /* final */ void close() {
        for (int i = 0; i < this.views.size(); i++) this.closeView(this.views.get(this.views.keyAt(i)));

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
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
