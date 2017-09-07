package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.ScreenPresenter;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class Screen extends ObservableView implements ScreenPresenter.View {
    protected final FlowableProcessor<Irrelevant> backNavigations = PublishProcessor.create();

    //region Constructors

    public Screen(@NonNull final Context context) {
        super(context);
    }

    public Screen(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public Screen(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public Screen(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    @Override
    public boolean goBack() {
        return false;
    }
}
