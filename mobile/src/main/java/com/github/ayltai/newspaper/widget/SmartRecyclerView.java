package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

public class SmartRecyclerView extends RecyclerView {
    private final FlowableProcessor<Integer> bestVisibleItemPositionChanges = BehaviorProcessor.create();

    private Disposable disposable;
    private int        previousBestVisibleItemPosition = RecyclerView.NO_POSITION;

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (this.disposable == null) this.disposable = RxRecyclerView.scrollEvents(this).subscribe(event -> {
            final int bestVisibleItemPosition = ((SmartLayoutManager)this.getLayoutManager()).findBestVisibleItemPosition();

            if (bestVisibleItemPosition != this.previousBestVisibleItemPosition) {
                this.previousBestVisibleItemPosition = bestVisibleItemPosition;
                this.bestVisibleItemPositionChanges.onNext(bestVisibleItemPosition);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
            this.disposable = null;
        }
    }

    public FlowableProcessor<Integer> bestVisibleItemPositionChanges() {
        return this.bestVisibleItemPositionChanges;
    }
}
