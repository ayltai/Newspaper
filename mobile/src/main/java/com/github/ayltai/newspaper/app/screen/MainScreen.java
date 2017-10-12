package com.github.ayltai.newspaper.app.screen;

import java.lang.ref.SoftReference;
import java.util.Map;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.google.auto.value.AutoValue;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.view.AboutPresenter;
import com.github.ayltai.newspaper.app.view.NewsPresenter;
import com.github.ayltai.newspaper.app.widget.AboutView;
import com.github.ayltai.newspaper.app.widget.BookmarkedNewsView;
import com.github.ayltai.newspaper.app.widget.HistoricalNewsView;
import com.github.ayltai.newspaper.app.widget.PagerNewsView;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.Screen;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.view.RxView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainScreen extends Screen implements MainPresenter.View, OnTabSelectListener {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @NonNull
        static MainScreen.Key create() {
            return new AutoValue_MainScreen_Key();
        }
    }

    public static final MainScreen.Key KEY = MainScreen.Key.create();

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> upActions       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> refreshActions  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> filterActions   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clearAllActions = PublishProcessor.create();

    //endregion

    private Map<Integer, SoftReference<View>> cachedViews = new ArrayMap<>();

    //region Components

    private Toolbar              toolbar;
    private SearchView           searchView;
    private ViewGroup            content;
    private NewsPresenter.View   newsView;
    private BottomBar            bottomBar;
    private FloatingActionButton upAction;
    private FloatingActionButton refreshAction;
    private FloatingActionButton settingsAction;
    private FloatingActionButton clearAllAction;
    private FloatingActionButton moreAction;

    //endregion

    private boolean isMoreActionsShown;

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
    public Flowable<Irrelevant> settingsActions() {
        return this.filterActions;
    }

    @NonNull
    @Override
    public Flowable<Irrelevant> clearAllActions() {
        return this.clearAllActions;
    }

    //endregion

    @SuppressWarnings("CyclomaticComplexity")
    @Override
    public void onTabSelected(@IdRes final int tabId) {
        this.toolbar.getMenu().findItem(R.id.action_search).collapseActionView();

        if (tabId == R.id.action_about) {
            this.upAction.setVisibility(View.GONE);
            this.refreshAction.setVisibility(View.GONE);
            this.settingsAction.setVisibility(View.GONE);
            this.clearAllAction.setVisibility(View.GONE);
            this.moreAction.setVisibility(View.GONE);

            this.toolbar.getMenu().findItem(R.id.action_search).setVisible(false);

            final AboutView      view      = new AboutView(this.getContext());
            final AboutPresenter presenter = new AboutPresenter();

            view.attachments().subscribe(isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment));
            view.detachments().subscribe(irrelevant -> presenter.onViewDetached());

            this.content.addView(view);
        } else {
            boolean isCached = false;

            if (this.cachedViews.containsKey(tabId)) {
                this.newsView = (NewsPresenter.View)this.cachedViews.get(tabId).get();

                if (this.newsView != null) {
                    if (this.content.indexOfChild((View)this.newsView) < 0) {
                        this.content.addView((View)this.newsView);

                        this.newsView.refresh();
                    }

                    isCached = true;
                }
            }

            if (this.isMoreActionsShown) this.hideMoreActions();

            this.upAction.setVisibility(View.INVISIBLE);
            this.refreshAction.setVisibility(View.INVISIBLE);
            this.settingsAction.setVisibility(tabId == R.id.action_news ? View.INVISIBLE : View.GONE);
            this.clearAllAction.setVisibility(tabId == R.id.action_news ? View.GONE : View.INVISIBLE);
            this.moreAction.setVisibility(View.VISIBLE);

            this.toolbar.getMenu().findItem(R.id.action_search).setVisible(true);

            if (!isCached) {
                this.newsView = tabId == R.id.action_news ? new PagerNewsView(this.getContext()) : tabId == R.id.action_history ? new HistoricalNewsView(this.getContext()) : new BookmarkedNewsView(this.getContext());
                this.content.addView((View)this.newsView);

                this.cachedViews.put(tabId, new SoftReference<>((View)this.newsView));
            }
        }

        if (this.content.getChildCount() > 1) this.content.removeViewAt(0);

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("BottomBar-" + this.bottomBar.findPositionForTabWithId(tabId)));
    }

    //region Lifecycle

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        final Activity activity = this.getActivity();
        if (activity != null) {
            final SearchManager manager = (SearchManager)this.getContext().getSystemService(Context.SEARCH_SERVICE);
            this.searchView.setSearchableInfo(manager.getSearchableInfo(activity.getComponentName()));
        }

        this.manageDisposable(RxSearchView.queryTextChanges(this.searchView).skipInitialValue().subscribe(newText -> {
            if (this.newsView != null) this.newsView.search(newText);
        }));

        this.bottomBar.setOnTabSelectListener(this);

        this.manageDisposable(RxView.clicks(this.moreAction).subscribe(irrelevant -> {
            if (this.isMoreActionsShown) {
                this.hideMoreActions();
            } else {
                this.showMoreActions();

                ComponentFactory.getInstance()
                    .getAnalyticsComponent(this.getContext())
                    .eventLogger()
                    .logEvent(new ClickEvent()
                        .setElementName("FAB - More"));
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

        this.manageDisposable(RxView.clicks(this.settingsAction).subscribe(irrelevant -> {
            this.hideMoreActions();

            this.filterActions.onNext(Irrelevant.INSTANCE);
        }));

        this.manageDisposable(RxView.clicks(this.clearAllAction).subscribe(irrelevant -> {
            this.hideMoreActions();

            this.clearAllActions.onNext(Irrelevant.INSTANCE);
        }));

        super.onAttachedToWindow();
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        this.bottomBar.removeOnTabSelectListener();

        super.onDetachedFromWindow();
    }

    //endregion

    //region Methods

    @Override
    public void up() {
        this.newsView.up();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("FAB - Up"));
    }

    @Override
    public void refresh() {
        this.newsView.refresh();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("FAB - Refresh"));
    }

    @Override
    public void settings() {
        if (this.newsView instanceof PagerNewsView) ((PagerNewsView)this.newsView).settings();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("FAB - Filter"));
    }

    @Override
    public void clearAll() {
        this.newsView.clear();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("FAB - Clear All"));
    }

    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_main, this, true);

        this.content        = view.findViewById(R.id.content);
        this.upAction       = view.findViewById(R.id.action_up);
        this.refreshAction  = view.findViewById(R.id.action_refresh);
        this.settingsAction = view.findViewById(R.id.action_settings);
        this.clearAllAction = view.findViewById(R.id.action_clear_all);
        this.moreAction     = view.findViewById(R.id.action_more);

        this.toolbar = view.findViewById(R.id.toolbar);
        this.toolbar.inflateMenu(R.menu.main);

        this.searchView = (SearchView)this.toolbar.getMenu().findItem(R.id.action_search).getActionView();
        this.searchView.setQueryHint(this.getContext().getText(R.string.search_hint));
        this.searchView.setMaxWidth(Integer.MAX_VALUE);

        this.bottomBar = view.findViewById(R.id.bottomBar);
        for (int i = 0; i < this.bottomBar.getTabCount(); i++) this.bottomBar.getTabAtPosition(i).setBarColorWhenSelected(ContextUtils.getColor(this.getContext(), R.attr.tabBarBackgroundColor));
        this.bottomBar.selectTabAtPosition(0);
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private void showMoreActions() {
        this.isMoreActionsShown = true;

        this.moreAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_clockwise));

        if (Animations.isEnabled()) {
            this.upAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            this.refreshAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
        } else {
            this.upAction.setVisibility(View.VISIBLE);
            this.refreshAction.setVisibility(View.VISIBLE);
            if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.setVisibility(View.VISIBLE);
            if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.setVisibility(View.VISIBLE);
        }

        this.upAction.setClickable(true);
        this.refreshAction.setClickable(true);
        if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.setClickable(true);
        if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.setClickable(true);
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private void hideMoreActions() {
        this.isMoreActionsShown = false;

        this.moreAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.rotate_anti_clockwise, R.integer.fab_animation_duration));

        if (Animations.isEnabled()) {
            this.upAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
            this.refreshAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
            if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
            if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, R.integer.fab_animation_duration));
        } else {
            this.upAction.setVisibility(View.INVISIBLE);
            this.refreshAction.setVisibility(View.INVISIBLE);
            if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.setVisibility(View.INVISIBLE);
            if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.setVisibility(View.INVISIBLE);
        }

        this.upAction.setClickable(false);
        this.refreshAction.setClickable(false);
        if (this.bottomBar.getCurrentTabId() == R.id.action_news) this.settingsAction.setClickable(false);
        if (this.bottomBar.getCurrentTabId() == R.id.action_history || this.bottomBar.getCurrentTabId() == R.id.action_bookmark) this.clearAllAction.setClickable(false);
    }

    //endregion
}
