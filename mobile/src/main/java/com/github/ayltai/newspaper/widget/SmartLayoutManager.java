package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class SmartLayoutManager extends LinearLayoutManager {
    //region Constructors

    public SmartLayoutManager(final Context context) {
        super(context);
    }

    public SmartLayoutManager(final Context context, final int orientation, final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SmartLayoutManager(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    public int findBestVisibleItemPosition() {
        final int firstVisibleItemPosition = this.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition  = this.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION || lastVisibleItemPosition == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION;

        final Rect rect = new Rect();

        int position = firstVisibleItemPosition;
        int max      = 0;

        for (int i = firstVisibleItemPosition; i < lastVisibleItemPosition; i++) {
            final View view = this.findViewByPosition(i);
            view.getGlobalVisibleRect(rect);

            if (this.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                if (rect.width() > max) {
                    position = i;
                    max      = rect.width();
                }
            } else {
                if (rect.height() > max) {
                    position = i;
                    max      = rect.height();
                }
            }
        }

        return position;
    }
}
