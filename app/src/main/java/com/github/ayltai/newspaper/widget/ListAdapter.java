package com.github.ayltai.newspaper.widget;

import java.util.List;

import javax.annotation.Nonnull;

import android.graphics.Point;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.data.model.Item;

import flow.Flow;

public final class ListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    //region Variables

    private final int style;

    private List<Item> items;

    //endregion

    public ListAdapter(@Constants.Style final int style) {
        this.style = style;
    }

    @Override
    public int getItemCount() {
        return this.items == null || this.items.isEmpty() ? 0 : this.items.size();
    }

    public void setItems(@Nonnull @NonNull @lombok.NonNull final List<Item> items) {
        this.items = items;

        this.notifyDataSetChanged();
    }

    public void clear() {
        this.items = null;

        this.notifyDataSetChanged();
    }

    @Nonnull
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@Nonnull @NonNull @lombok.NonNull final ViewGroup parent, final int viewType) {
        if (this.style == Constants.STYLE_COMFORTABLE) return new ItemViewHolder(new CozyItemView(parent.getContext()));
        //if (this.style == Constants.STYLE_COMPACT) return new ItemViewHolder(new CompactItemView(parent.getContext()));

        throw new IllegalArgumentException("Unrecognized style");
    }

    @Override
    public void onBindViewHolder(@Nonnull @NonNull @lombok.NonNull final ItemViewHolder holder, final int position) {
        final Item item = this.items.get(position);

        holder.view.setTitle(item.getTitle());
        holder.view.setDescription(item.getDescription());
        holder.view.setSource(item.getSource().getName());
        holder.view.setIcon(item.getSource().getImageUrl());
        holder.view.setPublishDate(item.getPublishDate());
        holder.view.setImages(item.getImages());

        holder.view.setOnClickListener(view -> Flow.get(view).set(DetailedItemView.Key.create(item, new Point())));
    }
}
