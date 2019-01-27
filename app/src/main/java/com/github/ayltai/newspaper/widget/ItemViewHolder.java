package com.github.ayltai.newspaper.widget;

import javax.annotation.Nonnull;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public final class ItemViewHolder extends RecyclerView.ViewHolder {
    public final ItemView view;

    public ItemViewHolder(@Nonnull @NonNull @lombok.NonNull final View itemView) {
        super(itemView);

        this.view = (ItemView)itemView;
    }
}
