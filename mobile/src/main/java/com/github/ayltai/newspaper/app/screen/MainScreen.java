package com.github.ayltai.newspaper.app.screen;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.SearchManager;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.google.auto.value.AutoValue;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.widget.OptionsView;
import com.github.ayltai.newspaper.config.UserConfig;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.ListView;
import com.github.ayltai.newspaper.widget.Screen;
import com.jakewharton.rxbinding2.support.v4.view.RxViewPager;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.view.RxView;
import com.roughike.bottombar.BottomBar;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainScreen extends Screen implements MainPresenter.View {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @NonNull
        static MainScreen.Key create() {
            return new AutoValue_MainScreen_Key();
        }
    }

    public static final MainScreen.Key KEY = MainScreen.Key.create();

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> upActions      = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> refreshActions = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> filterActions  = PublishProcessor.create();
    private final FlowableProcessor<Integer>    pageSelections = PublishProcessor.create();

    //endregion

    //region Components

    private SearchView           searchView;
    private BottomBar            bottomBar;
    private FloatingActionButton upAction;
    private FloatingActionButton refreshAction;
    private FloatingActionButton filterAction;
    private FloatingActionButton moreAction;
    private ViewPager            viewPager;

    //endregion

    private MainAdapter adapter;
    private boolean     isMoreActionsShown;

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

    //region Events

    @NonNull
    @Override
    public Flowable<Irrelevant> upActions() {
        return this.upActions;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> refreshActions() {
        return this.refreshActions;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> filterActions() {
        return this.filterActions;
    }

    @NonNull
    @Override
    public Flowable<Integer> pageSelections() {
        return this.pageSelections;
    }

    //endregion

    @Override
    protected void onAttachedToWindow() {
        final Activity activity = this.getActivity();

        if (activity != null) {
            final SearchManager manager = (SearchManager)this.getContext().getSystemService(Context.SEARCH_SERVICE);
            this.searchView.setSearchableInfo(manager.getSearchableInfo(activity.getComponentName()));
        }

        this.manageDisposable(RxSearchView.queryTextChanges(this.searchView).subscribe(newText -> {
            if (this.adapter != null) this.adapter.getFilter().filter(newText);
        }));

        this.manageDisposable(RxView.clicks(this.moreAction).subscribe(irrelevant -> {
            if (this.isMoreActionsShown) {
                this.hideMoreActions();
            } else {
                this.showMoreActions();
            }
        }));

        this.manageDisposable(RxView.clicks(this.upAction).subscribe(irrelevant -> {
            this.hideMoreActions();

            this.upActions.onNext(Irrelevant.INSTANCE);
        }));

        this.manageDisposable(RxView.clicks(this.refreshAction).subscribe(irrelevant -> {
            this.hideMoreActions();

            this.refreshActions.onNext(Irrelevant.INSTANCE);
        }));

        this.manageDisposable(RxView.clicks(this.filterAction).subscribe(irrelevant -> {
            this.hideMoreActions();

            this.filterActions.onNext(Irrelevant.INSTANCE);
        }));

        if (this.isFirstTimeAttachment) {
            this.adapter = new MainAdapter(this.getContext());

            final LifecycleOwner lifecycleOwner = this.getLifecycleOwner();
            if (lifecycleOwner != null) lifecycleOwner.getLifecycle().addObserver(this.adapter);

            this.viewPager.setAdapter(this.adapter);
            this.manageDisposable(RxViewPager.pageSelections(this.viewPager).subscribe(index -> {
                this.adapter.setCurrentPosition(index);

                this.pageSelections.onNext(index);
            }));

            this.pageSelections.onNext(0);
        }

        super.onAttachedToWindow();
    }

    //region Methods

    @Override
    public void up() {
        final ListView view = this.adapter.getItem(this.viewPager.getCurrentItem());
        if (view != null) view.up();
    }

    @Override
    public void refresh() {
        final ListView view = this.adapter.getItem(this.viewPager.getCurrentItem());
        if (view != null) view.refresh();
    }

    @Override
    public void filter() {
        final OptionsView view = new OptionsView(this.getContext(), UserConfig.getTheme(this.getContext()) == Constants.THEME_LIGHT ? R.style.AppDialogThemeLight : R.style.AppDialogThemeDark);

        this.manageDisposable(view.cancelClicks().subscribe(irrelevant -> view.dismiss()));

        this.manageDisposable(view.okClicks().subscribe(irrelevant -> {
            view.dismiss();

            final Activity activity = this.getActivity();
            if (activity != null) activity.finish();

            this.getContext().startActivity(new Intent(this.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }));

        view.show();
    }

    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, true);

        this.upAction      = view.findViewById(R.id.action_up);
        this.refreshAction = view.findViewById(R.id.action_refresh);
        this.filterAction  = view.findViewById(R.id.action_filter);
        this.moreAction    = view.findViewById(R.id.action_more);
        this.viewPager     = view.findViewById(R.id.viewPager);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);

        this.searchView = (SearchView)toolbar.getMenu().findItem(R.id.action_search).getActionView();
        this.searchView.setQueryHint(this.getContext().getText(R.string.search_hint));
        this.searchView.setMaxWidth(Integer.MAX_VALUE);

        final TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(this.viewPager, true);

        this.bottomBar = view.findViewById(R.id.bottomBar);
        for (int i = 0; i < this.bottomBar.getTabCount(); i++) this.bottomBar.getTabAtPosition(i).setBarColorWhenSelected(ContextUtils.getColor(this.getContext(), R.attr.tabBarBackgroundColor));
        this.bottomBar.selectTabAtPosition(0);
    }

    private void showMoreActions() {
        this.isMoreActionsShown = true;

        this.moreAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_clockwise));
        this.upAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
        this.refreshAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
        this.filterAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));

        this.upAction.setClickable(true);
        this.refreshAction.setClickable(true);
        this.filterAction.setClickable(true);
    }

    private void hideMoreActions() {
        this.isMoreActionsShown = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ValueAnimator.areAnimatorsEnabled()) {
            this.moreAction.setVisibility(View.INVISIBLE);
            this.upAction.setVisibility(View.INVISIBLE);
            this.refreshAction.setVisibility(View.INVISIBLE);
            this.filterAction.setVisibility(View.INVISIBLE);
        } else {
            this.moreAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.rotate_anti_clockwise, R.integer.fab_animation_duration));
            this.upAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
            this.refreshAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
            this.filterAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
        }

        this.upAction.setClickable(false);
        this.refreshAction.setClickable(false);
        this.filterAction.setClickable(false);
    }

    //endregion
}
