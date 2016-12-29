package com.github.ayltai.newspaper.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.setting.SettingsActivity;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import flow.ClassKey;
import io.realm.Realm;
import rx.Observable;
import rx.subjects.BehaviorSubject;

@SuppressLint("ViewConstructor")
public final class MainScreen extends FrameLayout implements MainPresenter.View {
    public static final class Key extends ClassKey implements Parcelable {
        public Key() {
        }

        //region Parcelable

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        }

        protected Key(@NonNull final Parcel in) {
        }

        public static final Parcelable.Creator<MainScreen.Key> CREATOR = new Parcelable.Creator<MainScreen.Key>() {
            @NonNull
            @Override
            public MainScreen.Key createFromParcel(@NonNull final Parcel source) {
                return new MainScreen.Key(source);
            }

            @NonNull
            @Override
            public MainScreen.Key[] newArray(final int size) {
                return new MainScreen.Key[size];
            }
        };

        //endregion
    }

    //region Events

    private final BehaviorSubject<Void> attachedToWindow   = BehaviorSubject.create();
    private final BehaviorSubject<Void> detachedFromWindow = BehaviorSubject.create();

    //endregion

    //region Variables

    private final Realm realm;

    private MainAdapter adapter;
    private boolean     hasAttached;

    //endregion

    //region Components

    private DrawerLayout          drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    //endregion

    public MainScreen(@NonNull final Context context, @NonNull final Realm realm) {
        super(context);

        this.realm = realm;
    }

    public boolean goBack() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        }

        return false;
    }

    //region Lifecycle

    @NonNull
    @Override
    public Observable<Void> attachments() {
        return this.attachedToWindow;
    }

    @NonNull
    @Override
    public Observable<Void> detachments() {
        return this.detachedFromWindow;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!this.hasAttached) {
            final View      view      = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, false);
            final Toolbar   toolbar   = (Toolbar)view.findViewById(R.id.toolbar);
            final ViewPager viewPager = (ViewPager)view.findViewById(R.id.viewPager);

            this.drawerLayout = (DrawerLayout)view.findViewById(R.id.drawerLayout);
            this.drawerToggle = new ActionBarDrawerToggle((Activity)this.getContext(), this.drawerLayout, toolbar, R.string.app_name, R.string.app_name);

            this.drawerLayout.addDrawerListener(this.drawerToggle);

            ((CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(false);

            toolbar.setNavigationIcon(R.drawable.ic_menu_white_24px);
            toolbar.setNavigationOnClickListener(v -> this.drawerLayout.openDrawer(GravityCompat.START));
            toolbar.setTitle(R.string.app_name);

            this.setupNavigationView((NavigationView)view.findViewById(R.id.navigationView));

            ((TabLayout)view.findViewById(R.id.tabLayout)).setupWithViewPager(viewPager);
            viewPager.setAdapter(this.adapter = new MainAdapter(this.getContext(), this.realm));

            this.addView(view);

            this.hasAttached = true;
        }

        this.drawerToggle.syncState();

        this.attachedToWindow.onNext(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.detachedFromWindow.onNext(null);
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        this.drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void close() {
        if (this.adapter != null) this.adapter.close();
    }

    //endregion

    private void setupNavigationView(@NonNull final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            this.drawerLayout.closeDrawer(GravityCompat.START);

            switch (item.getItemId()) {
                case R.id.action_settings:
                    this.showSettings();
                    return true;

                case R.id.action_about:
                    this.showAbout();
                    return true;

                default:
                    return false;
            }
        });
    }

    private void showSettings() {
        ((Activity)this.getContext()).startActivityForResult(new Intent(this.getContext(), SettingsActivity.class), Constants.REQUEST_SETTINGS);
    }

    @SuppressFBWarnings({"NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS"})
    private void showAbout() {
        new MaterialStyledDialog.Builder(this.getContext())
            .setStyle(Style.HEADER_WITH_ICON)
            .setHeaderColor(R.color.colorPrimary)
            .setIcon(R.mipmap.ic_launcher)
            .setTitle(R.string.app_name)
            .setDescription(String.format(this.getContext().getString(R.string.app_version), BuildConfig.VERSION_NAME))
            .setPositiveText(android.R.string.ok)
            .setNegativeText(R.string.rate_app)
            .onNegative((dialog, which) -> {
                final String name = this.getContext().getPackageName();

                try {
                    this.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + name)));
                } catch (final ActivityNotFoundException e) {
                    this.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + name)));
                }
            })
            .withIconAnimation(true)
            .withDialogAnimation(true, Duration.NORMAL)
            .withDivider(true)
            .show();
    }
}
