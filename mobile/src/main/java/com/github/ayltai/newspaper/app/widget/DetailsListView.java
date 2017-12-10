package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.DetailsListAdapter;
import com.github.ayltai.newspaper.app.view.binding.DetailsBinderFactory;
import com.github.ayltai.newspaper.view.UniversalAdapter;
import com.github.ayltai.newspaper.widget.HorizontalListView;

import flow.ClassKey;

public final class DetailsListView extends HorizontalListView<Item> {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @Nullable
        public abstract String getCategory();

        public abstract boolean isHistorical();

        public abstract boolean isBookmarked();

        public abstract int getItemPosition();

        @NonNull
        public static DetailsListView.Key create(@Nullable final String category, final boolean isHistorical, final boolean isBookmarked, final int itemPosition) {
            return new AutoValue_DetailsListView_Key(category, isHistorical, isBookmarked, itemPosition);
        }
    }

    public DetailsListView(@NonNull final Context context) {
        super(context);
    }

    @LayoutRes
    @Override
    protected int getLayoutId() {
        return R.layout.view_list_details;
    }

    @LayoutRes
    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerView;
    }

    @NonNull
    @Override
    protected UniversalAdapter<Item, ?, ?> createAdapter() {
        return new DetailsListAdapter.Builder(this.getContext())
            .addBinderFactory(new DetailsBinderFactory())
            .build();
    }

    @Override
    public void scrollTo(final int scrollPosition, final boolean smoothScroll) {
        this.recyclerView.scrollToPosition(scrollPosition);
    }
}
