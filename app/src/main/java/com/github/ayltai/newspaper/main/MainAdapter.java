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
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.data.Favorite;
import com.github.ayltai.newspaper.data.FavoriteManager;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.net.NetModule;
import com.github.ayltai.newspaper.rss.DaggerRssComponent;
import com.github.ayltai.newspaper.rss.RssModule;
import com.github.ayltai.newspaper.util.LogUtils;

import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class MainAdapter extends PagerAdapter implements Closeable {
    //region Variables

    private final SparseArrayCompat<View> views = new SparseArrayCompat<>();
    private final Context                 context;
    private final MainComponent           component;

    @Inject Realm           realm;
    @Inject FavoriteManager favoriteManager;

    private CompositeSubscription subscriptions;
    private Favorite              favorite;

    //endregion

    @Inject
    public MainAdapter(@NonNull final Context context) {
        this.context   = context;
        this.component = DaggerMainComponent.builder().mainModule(new MainModule((Activity)this.context)).build();

        DaggerDataComponent.builder()
            .dataModule(new DataModule(this.context))
            .build()
            .inject(this);

        //noinspection InstanceVariableUsedBeforeInitialized
        this.favoriteManager.getFavorite()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(favorite -> {
                this.favorite = favorite;

                this.notifyDataSetChanged();
            }, error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error));
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
        final ListPresenter presenter = this.component.listPresenter();
        final ListScreen    view      = (ListScreen)this.component.listView();

        DaggerRssComponent.builder()
            .contextModule(new ContextModule(this.context))
            .netModule(new NetModule())
            .rssModule(new RssModule())
            .build()
            .inject(presenter);

        if (this.subscriptions == null) this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(view.attachments().subscribe(dummy -> {
            presenter.onViewAttached(view);
            presenter.bind(this.realm, new ListScreen.Key(this.favorite.getSources().get(position).getUrl()));
        }, error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.subscriptions.add(view.detachments().subscribe(dummy -> presenter.onViewDetached(), error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

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

        this.views.clear();

        if (!this.realm.isClosed()) this.realm.close();
    }

    private void closeView(final View view) {
        if (view instanceof Closeable) {
            try {
                ((Closeable)view).close();
            } catch (final IOException e) {
                LogUtils.getInstance().e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
