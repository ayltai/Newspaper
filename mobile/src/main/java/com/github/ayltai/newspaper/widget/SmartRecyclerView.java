package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

public final class SmartRecyclerView extends RecyclerView {
    private final class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            final int bestVisibleItemPosition = ((SmartLayoutManager)SmartRecyclerView.this.getLayoutManager()).findBestVisibleItemPosition();

            if (bestVisibleItemPosition != SmartRecyclerView.this.previousBestVisibleItemPosition) {
                SmartRecyclerView.this.previousBestVisibleItemPosition = bestVisibleItemPosition;
                SmartRecyclerView.this.bestVisibleItemPositionChanges.onNext(bestVisibleItemPosition);
            }
        }
    }

    private final FlowableProcessor<Integer> bestVisibleItemPositionChanges = BehaviorProcessor.create();

    private final OnScrollListener onScrollListener = new OnScrollListener();

    private int previousBestVisibleItemPosition = RecyclerView.NO_POSITION;

    //region Constructors

    public SmartRecyclerView(final Context context) {
        super(context);
    }

    public SmartRecyclerView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartRecyclerView(final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    //endregion

    @Override
    public void setLayoutManager(final RecyclerView.LayoutManager layout) {
        if (layout instanceof SmartLayoutManager) {
            super.setLayoutManager(layout);
        } else {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " only supports " + SmartLayoutManager.class.getSimpleName());
        }
    }

    //region Lifecycle

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.addOnScrollListener(this.onScrollListener);
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        this.removeOnScrollListener(this.onScrollListener);

        super.onDetachedFromWindow();
    }

    //endregion

    public FlowableProcessor<Integer> bestVisibleItemPositionChanges() {
        return this.bestVisibleItemPositionChanges;
    }
}
