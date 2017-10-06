package com.github.ayltai.newspaper.app.view;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.ObservablePresenter;
import com.github.ayltai.newspaper.view.Presenter;

import io.reactivex.Flowable;

public class AboutPresenter extends ObservablePresenter<AboutPresenter.View> {
    public interface View extends Presenter.View {
        void setAppName(@NonNull CharSequence appName);

        void setAppIcon(@DrawableRes int appIcon);

        void setAppVersion(@NonNull CharSequence appVersion);

        void visit(@NonNull String url);

        void rate();

        void report(@NonNull String url);

        Flowable<Irrelevant> visitActions();

        Flowable<Irrelevant> rateActions();

        Flowable<Irrelevant> reportActions();
    }

    @Override
    public void onViewAttached(@NonNull final AboutPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        if (isFirstTimeAttachment) {
            view.setAppName(view.getContext().getText(R.string.app_name));
            view.setAppIcon(R.mipmap.ic_launcher);
            view.setAppVersion(BuildConfig.VERSION_NAME);
        }

        this.manageDisposable(view.visitActions().subscribe(irrelevant -> {
            view.visit("https://github.com/ayltai/Newspaper");

            ComponentFactory.getInstance()
                .getAnalyticsComponent(view.getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("About - Visit"));
        }));

        this.manageDisposable(view.rateActions().subscribe(irrelevant -> {
            ComponentFactory.getInstance()
                .getAnalyticsComponent(view.getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("About - Rate"));

            view.rate();
        }));

        this.manageDisposable(view.reportActions().subscribe(irrelevant -> {
            ComponentFactory.getInstance()
                .getAnalyticsComponent(view.getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("About - Report"));

            view.report("https://github.com/ayltai/Newspaper/issues");
        }));
    }
}
