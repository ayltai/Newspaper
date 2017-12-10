package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.Views;
import com.github.ayltai.newspaper.view.Presenter;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class BaseView extends FrameLayout implements Presenter.View {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    //region Subscriptions

    protected final FlowableProcessor<Boolean>    attachments = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> detachments = PublishProcessor.create();

    //endregion

    protected boolean isFirstTimeAttachment = true;

    public BaseView(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @Nullable
    @Override
    public Activity getActivity() {
        return Views.getActivity(this);
    }

    @Nullable
    @Override
    public LifecycleOwner getLifecycleOwner() {
        final Activity activity = this.getActivity();

        if (activity == null) return null;
        if (activity instanceof LifecycleOwner) return (LifecycleOwner)activity;

        return null;
    }

    //endregion

    //region Events

    @NonNull
    @Override
    public Flowable<Boolean> attachments() {
        return this.attachments;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> detachments() {
        return this.detachments;
    }

    //endregion

    protected void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }

    //region Lifecycle

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.attachments.onNext(this.isFirstTimeAttachment);

        this.isFirstTimeAttachment = false;
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        RxUtils.resetDisposables(this.disposables);

        super.onDetachedFromWindow();

        this.detachments.onNext(Irrelevant.INSTANCE);
    }

    //endregion

    protected void init() {
        this.setLayoutParams(Views.createWrapContentLayoutParams());
    }
}
