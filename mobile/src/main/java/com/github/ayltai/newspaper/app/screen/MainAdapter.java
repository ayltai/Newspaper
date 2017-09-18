package com.github.ayltai.newspaper.app.screen;

import java.lang.ref.WeakReference;
import java.util.List;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.app.widget.CompactItemListView;
import com.github.ayltai.newspaper.app.widget.CozyItemListView;
import com.github.ayltai.newspaper.app.widget.ItemListView;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.widget.ListView;

import io.reactivex.disposables.CompositeDisposable;

public class MainAdapter extends PagerAdapter implements LifecycleObserver {
    private final SparseArrayCompat<String>              categories = new SparseArrayCompat<>();
    private final SparseArrayCompat<WeakReference<View>> views      = new SparseArrayCompat<>();

    private CompositeDisposable disposables;

    public MainAdapter(@NonNull final Context context) {
        final List<String> categories = UserConfig.getCategories(context);
        for (int i = 0; i < categories.size(); i++) this.categories.put(i, categories.get(i));
    }

    @Override
    public int getCount() {
        return this.categories.size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return this.categories.get(position);
    }

    @Nullable
    public ListView getItem(final int position) {
        final WeakReference<View> view = this.views.get(position);

        if (view == null) return null;
        return (ListView)view.get();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final ItemListPresenter presenter = new ItemListPresenter(this.categories.get(position));
        final ItemListView      view      = UserConfig.getViewStyle(container.getContext()) == Constants.VIEW_STYLE_COZY ? new CozyItemListView(container.getContext()) : new CompactItemListView(container.getContext());

        if (this.disposables == null) this.disposables = new CompositeDisposable();

        this.disposables.add(view.attachments().subscribe(
            isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        this.disposables.add(view.detachments().subscribe(
            irrelevant -> presenter.onViewDetached(),
            error -> {
                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
            }
        ));

        this.views.put(position, new WeakReference<>(view));
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        final WeakReference<View> reference = this.views.get(position);

        if (reference != null) {
            final View view = reference.get();

            if (view != null) {
                this.views.remove(position);
                container.removeView(view);
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void dispose() {
        if (this.disposables != null && !this.disposables.isDisposed()) {
            this.disposables.dispose();
            this.disposables = null;
        }
    }
}
