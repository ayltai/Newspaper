package com.github.ayltai.newspaper.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.disposables.Disposable;

public class ObservablePresenter<V extends Presenter.View> extends Presenter<V> {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    protected void manageDisposable(@NonNull final Disposable disposable) {
        synchronized (this.disposables) {
            this.disposables.add(disposable);
        }
    }

    @CallSuper
    @UiThread
    @Override
    public void onViewDetached() {
        super.onViewDetached();

        RxUtils.resetDisposables(this.disposables);
    }
}
