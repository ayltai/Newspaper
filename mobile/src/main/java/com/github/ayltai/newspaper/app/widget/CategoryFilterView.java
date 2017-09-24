package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;

import com.github.ayltai.newspaper.app.view.CategoryFilterPresenter;
import com.github.ayltai.newspaper.widget.TextOptionsView;

public final class CategoryFilterView extends TextOptionsView implements CategoryFilterPresenter.View {
    //region Constructors

    public CategoryFilterView(@NonNull final Context context) {
        super(context);
    }

    public CategoryFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CategoryFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    @Override
    protected void onSelect(@NonNull final View view) {
        for (final View child : this.getTextOptions()) {
            if (view == child) view.setSelected(!view.isSelected());
        }
    }
}
