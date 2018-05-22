package com.github.ayltai.newspaper.app.widget;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.view.AboutPresenter;
import com.github.ayltai.newspaper.app.view.BaseNewsView;
import com.github.ayltai.newspaper.app.view.MainPresenter;
import com.github.ayltai.newspaper.util.Animations;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.BaseView;
import com.google.auto.value.AutoValue;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Map;

import flow.ClassKey;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainView extends BaseView implements MainPresenter.View, BottomNavigationView.OnNavigationItemSelectedListener {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @NonNull
        static MainView.Key create() {
            return new AutoValue_MainView_Key();
        }
    }

    public static final MainView.Key KEY = MainView.Key.create();

    //region Subscriptions

    private final FlowableProcessor<Irrelevant> upActions       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> refreshActions  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> filterActions   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clearAllActions = PublishProcessor.create();

    //endregion

    private Map<Integer, SoftReference<View>> cachedViews;

    //region Components

    private Toolbar              toolbar;
    private SearchView           searchView;
    private ViewGroup            content;
    private BaseNewsView         newsView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton upAction;
    private FloatingActionButton refreshAction;
    private FloatingActionButton settingsAction;
    private FloatingActionButton clearAllAction;
    private FloatingActionButton moreAction;

    //endregion

    private boolean isMoreActionsShown;

    public MainView(@NonNull final Context context) {
        super(context);
    }

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

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        this.toolbar.getMenu().findItem(R.id.action_search).collapseActionView();

        if (item.getItemId() == R.id.action_about) {
            this.upAction.setVisibility(View.GONE);
            this.refreshAction.setVisibility(View.GONE);
            this.settingsAction.setVisibility(View.GONE);
            this.clearAllAction.setVisibility(View.GONE);
            this.moreAction.setVisibility(View.GONE);

            this.toolbar.getMenu().findItem(R.id.action_search).setVisible(false);

            final AboutView      view      = new AboutView(this.getContext());
            final AboutPresenter presenter = new AboutPresenter();

            view.attaches().subscribe(isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment));
            view.detaches().subscribe(irrelevant -> presenter.onViewDetached());

            this.content.addView(view);
        } else {
            boolean isCached = false;

            if (this.cachedViews.containsKey(item.getItemId())) {
                this.newsView = (BaseNewsView)this.cachedViews.get(item.getItemId()).get();

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
            this.settingsAction.setVisibility(item.getItemId() == R.id.action_news ? View.INVISIBLE : View.GONE);
            this.clearAllAction.setVisibility(item.getItemId() == R.id.action_news ? View.GONE : View.INVISIBLE);
            this.moreAction.setVisibility(View.VISIBLE);

            this.toolbar.getMenu().findItem(R.id.action_search).setVisible(true);

            if (!isCached) {
                this.newsView = item.getItemId() == R.id.action_news
                    ? new PagedNewsView(this.getContext())
                    : item.getItemId() == R.id.action_history
                        ? new HistoricalNewsView(this.getContext())
                        : new BookmarkedNewsView(this.getContext());

                this.content.addView((View)this.newsView);

                this.cachedViews.put(item.getItemId(), new SoftReference<>((View)this.newsView));
            }
        }

        if (this.content.getChildCount() > 1) this.content.removeViewAt(0);

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("BottomNavigationView-" + item.getOrder()));

        return true;
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        final Activity activity = this.getActivity();
        if (activity != null) {
            final SearchManager manager = (SearchManager)this.getContext().getSystemService(Context.SEARCH_SERVICE);
            this.searchView.setSearchableInfo(manager.getSearchableInfo(activity.getComponentName()));
        }

        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (MainView.this.newsView == null) return false;

                MainView.this.newsView.search(newText);

                return true;
            }
        });

        this.moreAction.setOnClickListener(view -> {
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
        });

        this.upAction.setOnClickListener(view -> {
            this.hideMoreActions();

            this.upActions.onNext(Irrelevant.INSTANCE);
        });

        this.refreshAction.setOnClickListener(view -> {
            this.hideMoreActions();

            this.refreshActions.onNext(Irrelevant.INSTANCE);
        });

        this.settingsAction.setOnClickListener(view -> {
            this.hideMoreActions();

            this.filterActions.onNext(Irrelevant.INSTANCE);
        });

        this.clearAllAction.setOnClickListener(view -> {
            this.hideMoreActions();

            this.clearAllActions.onNext(Irrelevant.INSTANCE);
        });

        this.moreAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.pop_in));

        super.onAttachedToWindow();
    }

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
        if (this.newsView instanceof PagedNewsView) ((PagedNewsView)this.newsView).settings();

        ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new ClickEvent()
                .setElementName("FAB - Settings"));
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

    @Override
    protected void init() {
        super.init();

        this.cachedViews = new ArrayMap<>();

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_main, this, true);

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

        this.bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this);
        this.bottomNavigationView.setSelectedItemId(R.id.action_news);

        MainView.setShiftMode(this.bottomNavigationView, false, false);
    }

    private void showMoreActions() {
        this.isMoreActionsShown = true;

        this.moreAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_clockwise));

        if (Animations.isEnabled()) {
            this.upAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            this.refreshAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news) this.settingsAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark) this.clearAllAction.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fab_open));
        } else {
            this.upAction.setVisibility(View.VISIBLE);
            this.refreshAction.setVisibility(View.VISIBLE);
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news) this.settingsAction.setVisibility(View.VISIBLE);
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark) this.clearAllAction.setVisibility(View.VISIBLE);
        }

        this.upAction.setClickable(true);
        this.refreshAction.setClickable(true);
        if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news) this.settingsAction.setClickable(true);
        if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark) this.clearAllAction.setClickable(true);
    }

    private void hideMoreActions() {
        this.isMoreActionsShown = false;

        this.moreAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.rotate_anti_clockwise, android.R.integer.config_shortAnimTime));

        if (Animations.isEnabled()) {
            this.upAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, android.R.integer.config_shortAnimTime));
            this.refreshAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, android.R.integer.config_shortAnimTime));
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.settingsAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, android.R.integer.config_shortAnimTime));
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.clearAllAction.startAnimation(Animations.getAnimation(this.getContext(), R.anim.fab_close, android.R.integer.config_shortAnimTime));
        } else {
            this.upAction.setVisibility(View.INVISIBLE);
            this.refreshAction.setVisibility(View.INVISIBLE);
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.settingsAction.setVisibility(View.INVISIBLE);
            if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.clearAllAction.setVisibility(View.INVISIBLE);
        }

        this.upAction.setClickable(false);
        this.refreshAction.setClickable(false);
        if (this.bottomNavigationView.getSelectedItemId() == R.id.action_news || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.settingsAction.setClickable(false);
        if (this.bottomNavigationView.getSelectedItemId() == R.id.action_history || this.bottomNavigationView.getSelectedItemId() == R.id.action_bookmark || this.bottomNavigationView.getSelectedItemId() == R.id.action_about) this.clearAllAction.setClickable(false);
    }

    private static void setShiftMode(@NonNull final BottomNavigationView bottomNavigationView, final boolean shiftModeEnabled, final boolean itemShiftModeEnabled) {
        try {
            final BottomNavigationMenuView menuView = (BottomNavigationMenuView)bottomNavigationView.getChildAt(0);

            if (menuView != null) {
                final Field field = menuView.getClass().getDeclaredField("mShiftingMode");
                field.setAccessible(true);
                field.setBoolean(menuView, shiftModeEnabled);
                field.setAccessible(false);

                for (int i = 0; i < menuView.getChildCount(); i++) {
                    final BottomNavigationItemView itemView = (BottomNavigationItemView)menuView.getChildAt(i);

                    if (itemView != null) {
                        itemView.setShiftingMode(itemShiftModeEnabled);
                        itemView.setChecked(itemView.getItemData().isChecked());
                    }
                }

                menuView.updateMenuView();
            }
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            if (DevUtils.isLoggable()) Log.e(MainView.class.getSimpleName(), e.getMessage(), e);
        }
    }

    //endregion
}
