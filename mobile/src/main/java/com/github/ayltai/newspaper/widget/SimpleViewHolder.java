package com.github.ayltai.newspaper.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SimpleViewHolder<V extends View> extends RecyclerView.ViewHolder {
    public SimpleViewHolder(@NonNull final V itemView) {
        super(itemView);
    }
}
