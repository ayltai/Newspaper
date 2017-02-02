package com.github.ayltai.newspaper.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.Source;
import com.github.ayltai.newspaper.graphics.DaggerGraphicsComponent;
import com.github.ayltai.newspaper.graphics.GraphicsModule;
import com.github.ayltai.newspaper.list.ImagesUpdatedEvent;
import com.github.ayltai.newspaper.setting.SettingsActivity;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.github.piasy.biv.loader.ImageLoader;
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

    private static final Random RANDOM = new Random();

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
            if (MainScreen.this.adapter != null) {
                final Source source = MainScreen.this.adapter.getSource(MainScreen.this.viewPager.getCurrentItem());

                if (source != null && imagesUpdatedEvent.getUrl().equals(source.getUrl())) {
                    synchronized (MainScreen.this.images) {
                        MainScreen.this.images.clear();
                        MainScreen.this.images.addAll(imagesUpdatedEvent.getImages());

                        MainScreen.this.updateHeaderImages();
                    }
                }
            }
        }
    };

    private final ImageLoader.Callback callback = new ImageLoader.Callback() {
        @Override
        public void onCacheHit(final File image) {
            // FIXME: Exception may be thrown if the bitmap dimensions are too large
            MainScreen.this.headerImage.post(() -> MainScreen.this.headerImage.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath())));
        }

        @SuppressWarnings("WrongThread")
        @Override
        public void onCacheMiss(final File image) {
            this.onCacheHit(image);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onProgress(final int progress) {
        }

        @Override
        public void onFinish() {
        }
    };

    private final List<String> images = new ArrayList<>();

    @Inject
    ImageLoader imageLoader;

    //region Components

    private ViewPager    viewPager;
    private KenBurnsView headerImage;

    //endregion

    //region Variables

    private MainAdapter         adapter;
    private boolean             hasAttached;
    private boolean             isDrawerOpened;
    private GuillotineAnimation animation;

    //endregion

    @Inject
    public MainScreen(@NonNull final Context context) {
        super(context);

        DaggerGraphicsComponent.builder()
            .graphicsModule(new GraphicsModule(context))
            .build()
            .inject(this);
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
            final View                    view    = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, false);
            final CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout)view.findViewById(R.id.collapsingToolbarLayout);

            this.headerImage = (KenBurnsView)view.findViewById(R.id.headerImage);
            this.headerImage.setTransitionListener(new KenBurnsView.TransitionListener() {
                @Override
                public void onTransitionStart(final Transition transition) {
                }

                @Override
                public void onTransitionEnd(final Transition transition) {
                    MainScreen.this.updateHeaderImages();
                }
            });

            this.viewPager = (ViewPager)view.findViewById(R.id.viewPager);
            this.viewPager.setAdapter(this.adapter = DaggerMainComponent.builder().mainModule(new MainModule((Activity)view.getContext())).build().mainAdapter());
            this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(final int position) {
                    toolbar.setTitle(position == MainScreen.this.adapter.getCount() ? MainScreen.this.getResources().getText(R.string.title_bookmark) : MainScreen.this.adapter.getPageTitle(position));

                    MainScreen.this.updateHeaderImages();
                }

                @Override
                public void onPageScrollStateChanged(final int state) {
                }
            });

            toolbar.setTitle(this.adapter.getPageTitle(0));

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

    private void updateHeaderImages() {
        if (this.images.isEmpty()) {
            this.headerImage.post(() -> this.headerImage.setImageBitmap(null));
        } else {
            MainScreen.this.imageLoader.loadImage(Uri.parse(this.images.get(MainScreen.RANDOM.nextInt(this.images.size()))), this.callback);
        }
    }
}
