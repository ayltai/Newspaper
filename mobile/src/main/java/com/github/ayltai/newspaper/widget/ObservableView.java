package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.disposables.Disposable;

public class ObservableView extends BaseView {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    public ObservableView(@NonNull final Context context) {
        super(context);
    }

    protected void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        RxUtils.resetDisposables(this.disposables);

        super.onDetachedFromWindow();
    }
}
