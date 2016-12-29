package com.github.ayltai.newspaper.list;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.item.ItemPresenter;
import com.github.ayltai.newspaper.item.ItemViewHolder;
import com.github.ayltai.newspaper.rss.Item;
import com.jakewharton.rxbinding.view.RxView;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import rx.subscriptions.CompositeSubscription;

final class ListAdapter extends RealmRecyclerViewAdapter<Item, ItemViewHolder> implements Closeable {
    //region Variables

    private final Map<ItemViewHolder, ItemPresenter> map           = new HashMap<>();
    private final CompositeSubscription              subscriptions = new CompositeSubscription();
    private final Context                            context;
    private final ListScreen.Key                     parentKey;
    private final int                                listViewType;
    private final Feed                               feed;
    private final Realm                              realm;

    //endregion

    ListAdapter(@NonNull final Context context, @NonNull final ListScreen.Key parentKey, @Constants.ListViewType final int listViewType, @NonNull final Feed feed, @NonNull final Realm realm) {
        super(context, feed.getItems(), false);

        this.context      = context;
        this.parentKey    = parentKey;
        this.listViewType = listViewType;
        this.feed         = feed;
        this.realm        = realm;
    }

    @Override
    public int getItemCount() {
        return this.feed.getItems().size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.view_item_container, parent, false);

        LayoutInflater.from(this.context).inflate(this.listViewType == Constants.LIST_VIEW_TYPE_COZY ? R.layout.view_item_cozy : R.layout.view_item_compact, (ViewGroup)view.findViewById(R.id.smContentView), true);

        final ItemViewHolder holder    = new ItemViewHolder(view);
        final ItemPresenter  presenter = new ItemPresenter(this.realm);

        this.subscriptions.add(RxView.attaches(view).subscribe(dummy -> presenter.onViewAttached(holder)));
        this.subscriptions.add(RxView.detaches(view).subscribe(dummy -> presenter.onViewDetached()));

        this.map.put(holder, presenter);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        this.map.get(holder).bind(this.parentKey, this.feed.getItems().get(position), this.listViewType);
    }

    @Override
    public void close() {
        if (this.subscriptions.hasSubscriptions()) this.subscriptions.unsubscribe();
    }
}
