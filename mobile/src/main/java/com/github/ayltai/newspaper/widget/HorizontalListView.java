package com.github.ayltai.newspaper.widget;

import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.view.ListPresenter;

public abstract class HorizontalListView<M> extends ListView<M> implements ListPresenter.View<M> {
    protected HorizontalListView(@NonNull final Context context) {
        super(context);

        this.init();
    }

    @Override
    public void bind(@NonNull final List<M> models) {
        if (DevUtils.isLoggable()) {
            for (final M model : models) Log.v(this.getClass().getSimpleName(), model.toString());
        }

        if (this.adapter.getItemCount() == 0) {
            this.adapter.onItemRangeInserted(models, 0);
        } else {
            this.adapter.onItemRangeInserted(models, this.adapter.getItemCount() - 1);
        }
    }

    @Override
    protected void init() {
        this.adapter = this.createAdapter();

        final View view = LayoutInflater.from(this.getContext()).inflate(this.getLayoutId(), this, false);

        this.recyclerView = view.findViewById(this.getRecyclerViewId());
        this.recyclerView.setLayoutManager(new SmartLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL));
        this.recyclerView.setAdapter(this.adapter);

        new PagerSnapHelper().attachToRecyclerView(this.recyclerView);

        this.addView(view);
    }
}
