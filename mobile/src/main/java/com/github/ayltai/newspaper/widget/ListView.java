package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.view.ListPresenter;
import com.github.ayltai.newspaper.view.UniversalAdapter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class ListView<M> extends BaseView implements ListPresenter.View<M> {
    protected final FlowableProcessor<Integer> bestVisibleItemPositionChanges = PublishProcessor.create();

    protected UniversalAdapter<M, ?, ?> adapter;
    protected SmartRecyclerView         recyclerView;

    protected ListView(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @Nullable
    public UniversalAdapter<M, ?, ?> getAdapter() {
        return this.adapter;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @IdRes
    protected abstract int getRecyclerViewId();

    //endregion

    @NonNull
    protected abstract UniversalAdapter<M, ?, ?> createAdapter();

    @NonNull
    @Override
    public Flowable<Integer> bestVisibleItemPositionChanges() {
        return this.bestVisibleItemPositionChanges;
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        this.manageDisposable(this.recyclerView.bestVisibleItemPositionChanges().subscribe(this.bestVisibleItemPositionChanges::onNext));

        super.onAttachedToWindow();
    }

    @Override
    protected void init() {
        super.init();

        this.adapter = this.createAdapter();

        final View view = LayoutInflater.from(this.getContext()).inflate(this.getLayoutId(), this, false);

        this.recyclerView = view.findViewById(this.getRecyclerViewId());
        this.recyclerView.setLayoutManager(new SmartLayoutManager(this.getContext()));
        this.recyclerView.setAdapter(this.adapter);

        this.addView(view);
    }
}
