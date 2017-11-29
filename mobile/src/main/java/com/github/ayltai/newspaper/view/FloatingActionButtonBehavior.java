package com.github.ayltai.newspaper.view;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.ContextUtils;

public final class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<View> {
    private final float toolbarHeight;
    private final float fabHeight;
    private final float bottomMargin;

    public FloatingActionButtonBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        this.toolbarHeight = ContextUtils.getDimensionPixelSize(context, R.attr.actionBarSize);
        this.fabHeight     = context.getResources().getDimensionPixelSize(R.dimen.fabSize);
        this.bottomMargin  = context.getResources().getDimensionPixelSize(R.dimen.space16);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final View child, final View dependency) {
        if (this.layoutDependsOn(parent, child, dependency)) {
            child.setTranslationY((this.fabHeight + this.bottomMargin) * (dependency.getY() / -this.toolbarHeight));

            final float alpha = 1f - dependency.getY() / -this.toolbarHeight;

            if (alpha == 0) {
                child.setVisibility(View.GONE);
            } else {
                child.setVisibility(View.VISIBLE);

                if (child instanceof ViewGroup) {
                    final ViewGroup container = (ViewGroup)child;
                    for (int i = 0; i < container.getChildCount(); i++) container.getChildAt(i).setAlpha(alpha);
                }
            }

            return true;
        }

        return false;
    }
}
