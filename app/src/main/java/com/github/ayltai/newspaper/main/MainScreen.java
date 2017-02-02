package com.github.ayltai.newspaper.main;

import java.util.concurrent.Executors;

import javax.inject.Inject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.Source;
import com.github.ayltai.newspaper.list.ImagesUpdatedEvent;
import com.github.ayltai.newspaper.setting.SettingsActivity;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.yalantis.guillotine.animation.GuillotineAnimation;
import com.yalantis.guillotine.interfaces.GuillotineListener;

import flow.ClassKey;
import rx.Observable;
import rx.Subscriber;
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

    private final Subscriber<ImagesUpdatedEvent> subscriber = new Subscriber<ImagesUpdatedEvent>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable e) {
            LogUtils.getInstance().w(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        @Override
        public void onNext(final ImagesUpdatedEvent imagesUpdatedEvent) {
            final Source source = MainScreen.this.adapter.getSource(MainScreen.this.viewPager.getCurrentItem());

            if (source != null && imagesUpdatedEvent.getUrl().equals(source.getUrl()) && !imagesUpdatedEvent.getImages().isEmpty()) {
                final DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(ImageRequest.fromUri(imagesUpdatedEvent.getImages().get(0)), null);

                dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                    @Override
                    protected void onNewResultImpl(final DataSource<CloseableReference<CloseableImage>> dataSource) {
                        if (dataSource.hasResult()) {
                            MainScreen.this.logoBackground.post(() -> MainScreen.this.logoBackground.setImageBitmap(((CloseableBitmap)dataSource.getResult().get()).getUnderlyingBitmap()));
                        }
                    }

                    @Override
                    protected void onFailureImpl(final DataSource<CloseableReference<CloseableImage>> dataSource) {
                    }
                }, Executors.newSingleThreadExecutor());
            }
        }
    };

    private ViewPager    viewPager;
    private KenBurnsView logoBackground;

    //region Variables

    private MainAdapter         adapter;
    private boolean             hasAttached;
    private boolean             isDrawerOpened;
    private GuillotineAnimation animation;

    //endregion

    @Inject
    public MainScreen(@NonNull final Context context) {
        super(context);
    }

    public boolean goBack() {
        if (this.isDrawerOpened) {
            this.animation.close();

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
            final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, false);

            this.viewPager = (ViewPager)view.findViewById(R.id.viewPager);

            ((CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(false);
            ((TabLayout)view.findViewById(R.id.tabLayout)).setupWithViewPager(this.viewPager);

            this.viewPager.setAdapter(this.adapter = DaggerMainComponent.builder().mainModule(new MainModule((Activity)view.getContext())).build().mainAdapter());

            this.logoBackground = (KenBurnsView)view.findViewById(R.id.logoBackground);
            this.logoBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);

            this.addView(view);

            this.setUpDrawerMenu(view);

            this.hasAttached = true;
        }

        this.attachedToWindow.onNext(null);

        RxBus.getInstance().register(ImagesUpdatedEvent.class, this.subscriber);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        RxBus.getInstance().unregister(ImagesUpdatedEvent.class, this.subscriber);

        this.detachedFromWindow.onNext(null);
    }

    @Override
    public void close() {
        if (this.adapter != null) {
            this.adapter.close();
            this.adapter = null;
        }

        RxBus.getInstance().unregister(ImagesUpdatedEvent.class, this.subscriber);
    }

    //endregion

    private void showSettings() {
        ((Activity)this.getContext()).startActivityForResult(new Intent(this.getContext(), SettingsActivity.class), Constants.REQUEST_SETTINGS);
    }

    @SuppressFBWarnings({"NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS"})
    private void showAbout() {
        new MaterialStyledDialog.Builder(this.getContext())
            .setStyle(Style.HEADER_WITH_ICON)
            .setHeaderColor(ContextUtils.getResourceId(this.getContext(), R.attr.primaryColor))
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

    private void setUpDrawerMenu(@NonNull final View view) {
        final View drawerMenu = LayoutInflater.from(this.getContext()).inflate(R.layout.view_drawer_menu, this, false);
        drawerMenu.setOnClickListener(v -> {
            // Prevent click-through
        });

        drawerMenu.findViewById(R.id.action_settings).setOnClickListener(v -> {
            this.animation.close();
            this.showSettings();
        });

        drawerMenu.findViewById(R.id.action_about).setOnClickListener(v -> {
            this.animation.close();
            this.showAbout();
        });

        this.addView(drawerMenu);

        this.animation = new GuillotineAnimation.GuillotineBuilder(drawerMenu, drawerMenu.findViewById(R.id.drawer_close), this.findViewById(R.id.drawer_open))
            .setStartDelay(Constants.DRAWER_MENU_ANIMATION_DELAY)
            .setActionBarViewForAnimation(view.findViewById(R.id.toolbar))
            .setClosedOnStart(true)
            .setGuillotineListener(new GuillotineListener() {
                @Override
                public void onGuillotineOpened() {
                    MainScreen.this.isDrawerOpened = true;
                }

                @Override
                public void onGuillotineClosed() {
                    MainScreen.this.isDrawerOpened = false;
                }
            })
            .build();
    }
}
