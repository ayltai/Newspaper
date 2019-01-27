package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.widget.AboutView;

public final class AboutPresenter extends BasePresenter<AboutPresenter.View> {
    public interface View extends Presenter.View {
        void setLogo(@DrawableRes int logo);

        void setDescription(@Nonnull @NonNull @lombok.NonNull String description);

        void setVersion(@Nonnull @NonNull @lombok.NonNull String version);

        void setWebsite(@Nonnull @NonNull @lombok.NonNull String uri, @Nonnull @NonNull @lombok.NonNull String website);

        void setIssues(@Nonnull @NonNull @lombok.NonNull String uri, @Nonnull @NonNull @lombok.NonNull String issues);

        @UiThread
        void show();
    }

    public static final class Factory implements Presenter.Factory<AboutPresenter, AboutPresenter.View> {
        @Override
        public boolean isSupported(@Nonnull @NonNull final Object key) {
            return key instanceof AboutView.Key;
        }

        @Nonnull
        @NonNull
        @Override
        public AboutPresenter createPresenter() {
            return new AboutPresenter();
        }

        @Nonnull
        @NonNull
        @Override
        public AboutPresenter.View createView(@Nonnull @NonNull final Context context) {
            return new AboutView(context);
        }
    }

    @Override
    public void onViewAttached(@Nonnull @NonNull final AboutPresenter.View view, final boolean isFirstAttachment) {
        super.onViewAttached(view, isFirstAttachment);

        if (isFirstAttachment) {
            view.setLogo(R.drawable.logo);
            view.setDescription(view.getContext().getString(R.string.app_description));

            try {
                view.setVersion(String.format(view.getContext().getString(R.string.app_version), view.getContext().getPackageManager().getPackageInfo(view.getContext().getPackageName(), 0).versionName));
            } catch (final PackageManager.NameNotFoundException e) {
                Log.w(e.getMessage(), e);
            }

            view.setWebsite(view.getContext().getString(R.string.app_website_uri), view.getContext().getString(R.string.app_website));
            view.setIssues(view.getContext().getString(R.string.app_issue_uri), view.getContext().getString(R.string.app_issue));

            view.show();
        }
    }
}
