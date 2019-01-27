package com.github.ayltai.newspaper.widget;

import javax.annotation.Nonnull;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.view.AboutPresenter;
import com.google.auto.value.AutoValue;

import flow.ClassKey;
import flow.Flow;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public final class AboutView extends BaseView implements AboutPresenter.View {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @Nonnull
        @NonNull
        static AboutView.Key create() {
            return new AutoValue_AboutView_Key();
        }
    }

    public static final AboutView.Key KEY = AboutView.Key.create();

    private ViewGroup content;
    private AboutPage page;

    public AboutView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_generic, this, false);

        this.content = view.findViewById(R.id.content);
        this.page    = new AboutPage(context);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Flow.get(this).goBack());

        this.addView(view);
    }

    @Override
    public void setLogo(final int logo) {
        this.page.setImage(logo);
    }

    @Override
    public void setDescription(@Nonnull @NonNull @lombok.NonNull final String description) {
        this.page.setDescription(description);
    }

    @Override
    public void setVersion(@Nonnull @NonNull @lombok.NonNull final String version) {
        this.page.addItem(new Element()
            .setTitle(version)
            .setGravity(Gravity.CENTER_HORIZONTAL));
    }

    @Override
    public void setWebsite(@Nonnull @NonNull @lombok.NonNull final String uri, @Nonnull @NonNull @lombok.NonNull final String website) {
        this.page.addItem(new Element()
            .setTitle(website)
            .setIconDrawable(R.drawable.ic_github_black_24dp)
            .setOnClickListener(view -> new CustomTabsIntent.Builder()
                .build()
                .launchUrl(this.getContext(), Uri.parse(uri))));
    }

    @Override
    public void setIssues(@Nonnull @NonNull @lombok.NonNull final String uri, @Nonnull @NonNull @lombok.NonNull final String issues) {
        this.page.addItem(new Element()
            .setTitle(issues)
            .setIconDrawable(R.drawable.ic_bug_report_black_24dp)
            .setOnClickListener(view -> new CustomTabsIntent.Builder()
                .build()
                .launchUrl(this.getContext(), Uri.parse(uri))));
    }

    @UiThread
    @Override
    public void show() {
        if (this.content.getChildCount() == 0) this.content.addView(this.page.create());
    }
}
