package com.github.ayltai.newspaper.app.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class OptionsView extends BottomSheetDialog {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> okClicks     = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> cancelClicks = PublishProcessor.create();

    //endregion

    //region Components

    private View cancelAction;
    private View okAction;

    //endregion

    public OptionsView(@NonNull final Context context, @StyleRes final int theme) {
        super(context, theme);
        this.init();
    }

    //region Events

    @NonNull
    public Flowable<Irrelevant> okClicks() {
        return this.okClicks;
    }

    @NonNull
    public Flowable<Irrelevant> cancelClicks() {
        return this.cancelClicks;
    }

    //endregion

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.manageDisposable(RxView.clicks(this.cancelAction).subscribe(irrelevant -> this.cancelClicks.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.okAction).subscribe(irrelevant -> this.okClicks.onNext(Irrelevant.INSTANCE)));
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        RxUtils.resetDisposables(this.disposables);

        super.onDetachedFromWindow();
    }

    @SuppressLint("InflateParams")
    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_options, null);

        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new OptionsAdapter(this.getContext()));

        final TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        this.cancelAction = view.findViewById(R.id.action_cancel);
        this.okAction     = view.findViewById(R.id.action_ok);

        this.setContentView(view);
    }

    private void manageDisposable(@NonNull final Disposable disposable) {
        this.disposables.add(disposable);
    }
}
