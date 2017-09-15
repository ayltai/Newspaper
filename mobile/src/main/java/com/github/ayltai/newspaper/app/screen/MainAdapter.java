package com.github.ayltai.newspaper.app.screen;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

public class MainAdapter extends PagerAdapter {
    private final SparseArrayCompat<String>              categories = new SparseArrayCompat<>();
    private final SparseArrayCompat<WeakReference<View>> views      = new SparseArrayCompat<>();

    private final Realm realm;

    private CompositeDisposable disposables;

    public MainAdapter(@NonNull final Context context) {
        this.realm = DaggerDataComponent.builder()
            .dataModule(new DataModule(context))
            .build()
            .realm();

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
}
