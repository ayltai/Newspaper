package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;

import com.github.ayltai.newspaper.app.view.SourceFilterPresenter;
import com.github.ayltai.newspaper.widget.TextOptionsView;

public final class SourceFilterView extends TextOptionsView implements SourceFilterPresenter.View {
    //region Constructors

    public SourceFilterView(@NonNull final Context context) {
        super(context);
    }

    public SourceFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public SourceFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SourceFilterView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
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
