package com.github.ayltai.newspaper.app.screen;

import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.auto.value.AutoValue;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.widget.Screen;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainScreen extends Screen implements MainPresenter.View {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        static MainScreen.Key create() {
            return new AutoValue_MainScreen_Key();
        }
    }

    public static final MainScreen.Key KEY = MainScreen.Key.create();

    private final FlowableProcessor<Integer> pageChanges = PublishProcessor.create();

    //region Components

    private Toolbar   toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //endregion

    private MainAdapter adapter;

    //region Constructors

    public MainScreen(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public MainScreen(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public MainScreen(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public MainScreen(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion


    @NonNull
    @Override
    public Flowable<Integer> pageChanges() {
        return this.pageChanges;
    }

    @Override
    protected void onAttachedToWindow() {
        if (this.isFirstTimeAttachment) {
            this.viewPager.setAdapter(this.adapter = new MainAdapter(this.getContext()));
            this.pageChanges.onNext(0);
        }

        super.onAttachedToWindow();
    }

    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, true);

        this.toolbar   = view.findViewById(R.id.toolbar);
        this.tabLayout = view.findViewById(R.id.tabLayout);
        this.viewPager = view.findViewById(R.id.viewPager);

        this.tabLayout.setupWithViewPager(this.viewPager, true);
    }
}
