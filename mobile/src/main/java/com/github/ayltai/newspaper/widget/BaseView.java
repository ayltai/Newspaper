package com.github.ayltai.newspaper.widget;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.view.Presenter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class BaseView extends PartView implements Presenter.View {
    //region Subscriptions

    protected final FlowableProcessor<Boolean>    attachments = PublishProcessor.create();
    protected final FlowableProcessor<Irrelevant> detachments = PublishProcessor.create();

    //endregion

    protected boolean isFirstTimeAttachment = true;

    public BaseView(@NonNull final Context context) {
        super(context);
    }

    @Nullable
    @Override
    public Activity getActivity() {
        return ViewUtils.getActivity(this);
    }

    @Nullable
    @Override
    public LifecycleOwner getLifecycleOwner() {
        final Activity activity = this.getActivity();

        if (activity == null) return null;
        if (activity instanceof LifecycleOwner) return (LifecycleOwner)activity;

        return null;
    }

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

    //region Lifecycle

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.attachments.onNext(this.isFirstTimeAttachment);

        this.isFirstTimeAttachment = false;
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.detachments.onNext(Irrelevant.INSTANCE);
    }

    //endregion
}
