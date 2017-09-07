package com.github.ayltai.newspaper.view.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.widget.SimplePartView;

public class InfiniteLoadingBinderFactory<M, V extends SimplePartView> implements PartBinderFactory<M, V> {
    private final InfiniteLoadingCallback callback;

    public InfiniteLoadingBinderFactory(@Nullable final InfiniteLoadingCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getPartType() {
        return R.id.view_type_infinite_loading;
    }

    @Override
    public boolean isNeeded(@Nullable final M model) {
        return model == null;
    }

    @NonNull
    @Override
    public Binder<V> create(@Nullable final M model) {
        return view -> {
            if (this.callback != null) this.callback.onStartLoading();
        };
    }
}
