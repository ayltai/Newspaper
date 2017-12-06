package com.github.ayltai.newspaper.app.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.view.AboutPresenter;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.BaseView;
import com.instabug.library.Instabug;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class AboutView extends BaseView implements AboutPresenter.View {
    //region Subscriptions

    private final FlowableProcessor<Irrelevant> visitActions   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> rateActions    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> reportActions  = PublishProcessor.create();

    //endregion

    //region Components

    private ViewGroup                container;
    private TextView                 appName;
    private android.widget.ImageView appIcon;
    private TextView                 appVersion;
    private View                     visitAction;
    private View                     rateAction;
    private View                     reportAction;

    //endregion

    private boolean isAnimated;

    public AboutView(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @Override
    public void setAppName(@NonNull final CharSequence appName) {
        this.appName.setText(appName);
    }

    @VisibleForTesting
    protected CharSequence getAppName() {
        return this.appName.getText();
    }

    @Override
    public void setAppIcon(@DrawableRes final int appIcon) {
        this.appIcon.setImageResource(appIcon);
    }

    @Override
    public void setAppVersion(@NonNull final CharSequence appVersion) {
        this.appVersion.setText(String.format(this.getContext().getString(R.string.app_version), appVersion));

        if (Animations.isEnabled() && !this.isAnimated) {
            this.isAnimated = true;

            Animations.animateViewGroup(this.container);
        }
    }

    @VisibleForTesting
    protected CharSequence getAppVersion() {
        return this.appVersion.getText();
    }

    //endregion

    //region Methods

    @Override
    public void visit(@NonNull final String url) {
        this.openUrl(url);
    }

    @Override
    public void rate() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));

        if (this.getContext().getPackageManager().resolveActivity(intent, 0) == null) {
            this.openUrl("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        } else {
            this.getContext().startActivity(intent);
        }
    }

    @Override
    public void report(@NonNull final String url) {
        try {
            Instabug.invoke();
        } catch (final IllegalStateException e) {
            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));

            this.openUrl(url);
        }
    }

    //endregion

    //region Events

    @Override
    public Flowable<Irrelevant> visitActions() {
        return this.visitActions;
    }

    @Override
    public Flowable<Irrelevant> rateActions() {
        return this.rateActions;
    }

    @Override
    public Flowable<Irrelevant> reportActions() {
        return this.reportActions;
    }

    //endregion

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.manageDisposable(RxView.clicks(this.visitAction).subscribe(irrelevant -> this.visitActions.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.rateAction).subscribe(irrelevant -> this.rateActions.onNext(Irrelevant.INSTANCE)));
        this.manageDisposable(RxView.clicks(this.reportAction).subscribe(irrelevant -> this.reportActions.onNext(Irrelevant.INSTANCE)));
    }

    @Override
    protected void init() {
        super.init();

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_about, this, true);

        this.container     = view.findViewById(R.id.container);
        this.appName       = view.findViewById(R.id.app_name);
        this.appIcon       = view.findViewById(R.id.app_icon);
        this.appVersion    = view.findViewById(R.id.app_version);
        this.visitAction   = view.findViewById(R.id.visit_container);
        this.rateAction    = view.findViewById(R.id.rate_container);
        this.reportAction  = view.findViewById(R.id.report_container);
    }

    private void openUrl(@NonNull final String url) {
        try {
            new CustomTabsIntent.Builder()
                .setToolbarColor(ContextUtils.getColor(this.getContext(), R.attr.primaryColor))
                .build()
                .launchUrl(this.getContext(), Uri.parse(url));
        } catch (final ActivityNotFoundException e) {
            if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), e.getMessage(), RxJava2Debug.getEnhancedStackTrace(e));

            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (this.getContext().getPackageManager().resolveActivity(intent, 0) != null) this.getContext().startActivity(intent);
        }
    }
}
