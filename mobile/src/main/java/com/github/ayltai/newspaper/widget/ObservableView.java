package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.disposables.Disposable;

public class ObservableView extends BaseView {
    private final List<Disposable> disposables = new ArrayList<>();

    //region Constructors

    public ObservableView(@NonNull final Context context) {
        super(context);
    }

    public ObservableView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ObservableView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    //endregion

    protected void manageDisposable(@NonNull final Disposable disposable) {
        synchronized (this.disposables) {
            this.disposables.add(disposable);
        }
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        RxUtils.resetDisposables(this.disposables);

        super.onDetachedFromWindow();
    }
}
