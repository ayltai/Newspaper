package com.github.ayltai.newspaper.main;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.data.Favorite;
import com.github.ayltai.newspaper.data.FavoriteManager;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.util.LogUtils;

import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

final class MainAdapter extends PagerAdapter implements Closeable {
    private final SparseArrayCompat<View> views = new SparseArrayCompat<>();
    private final Context                 context;
    private final Realm                   realm;

    private CompositeSubscription subscriptions;
    private Favorite              favorite;

    MainAdapter(@NonNull final Context context, @NonNull final Realm realm) {
        this.context = context;
        this.realm   = realm;

        new FavoriteManager(context, realm).getFavorite()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(favorite -> {
                this.favorite = favorite;

                this.notifyDataSetChanged();
            }, error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error));
    }

    @Override
    public int getCount() {
        return this.favorite == null ? 0 : this.favorite.getSources().size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final ListPresenter presenter = new ListPresenter();
        final ListScreen    view      = new ListScreen(this.context, this.realm);

        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(view.attachments().subscribe(dummy -> {
            presenter.onViewAttached(view);
            presenter.bind(this.realm, new ListScreen.Key(this.favorite.getSources().get(position).getUrl()));
        }, error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error)));

        this.subscriptions.add(view.detachments().subscribe(dummy -> presenter.onViewDetached(), error -> LogUtils.e(this.getClass().getName(), error.getMessage(), error)));

        this.views.put(position, view);
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup container, final int position, final Object object) {
        final View view = this.views.get(position);

        this.closeView(view);

        if (this.views.indexOfKey(position) > -1) container.removeView(view);
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position) {
        return this.favorite.getSources().get(position).getName();
    }

    @Override
    public void close() {
        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        for (int i = 0; i < this.views.size(); i++) this.closeView(this.views.get(this.views.keyAt(i)));
    }

    private void closeView(final View view) {
        if (view instanceof Closeable) {
            try {
                ((Closeable)view).close();
            } catch (final IOException e) {
                LogUtils.e(this.getClass().getName(), e.getMessage(), e);
            }
        }
    }
}
