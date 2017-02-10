package com.github.ayltai.newspaper.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.setting.Settings;

final class DummyAdapter extends RecyclerView.Adapter<DummyViewHolder> {
    @Override
    public int getItemCount() {
        return Constants.INIT_LOAD_ITEM_COUNT;
    }

    @NonNull
    @Override
    public DummyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new DummyViewHolder(LayoutInflater.from(parent.getContext()).inflate(Settings.getListViewType(parent.getContext()) == Constants.LIST_VIEW_TYPE_COMPACT ? R.layout.view_item_compact : R.layout.view_item_cozy, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final DummyViewHolder holder, final int position) {
    }
}
