package com.github.ayltai.newspaper.widget;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.MainPresenter;
import com.github.ayltai.newspaper.view.PagerPresenter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.auto.value.AutoValue;
import com.mancj.materialsearchbar.MaterialSearchBar;

import flow.ClassKey;
import flow.Flow;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainView extends BaseView implements MainPresenter.View, AppBarLayout.OnOffsetChangedListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, Toolbar.OnMenuItemClickListener {
    @AutoValue
    public abstract static class Key extends ClassKey implements Parcelable {
        @Nonnull
        @NonNull
        static MainView.Key create() {
            return new AutoValue_MainView_Key();
        }
    }

    public static final MainView.Key KEY = MainView.Key.create();

    private final FlowableProcessor<Irrelevant> refreshClicks   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clearClicks     = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> settingsClicks  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> newsClicks      = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> historiesClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bookmarksClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> aboutClicks     = PublishProcessor.create();

    private final AppBarLayout      appBarLayout;
    private final MaterialSearchBar searchBar;
    private final PagerPresenter    pagerPresenter;
    private final PagerView         pagerView;

    private Dialog dialog;

    public MainView(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_main, this, false);

        this.appBarLayout = view.findViewById(R.id.appBarLayout);
        this.searchBar    = view.findViewById(R.id.searchBar);

        final BottomAppBar bottomAppBar = view.findViewById(R.id.bottomAppBar);
        bottomAppBar.replaceMenu(R.menu.bottom);
        bottomAppBar.setNavigationOnClickListener(this);
        bottomAppBar.setOnMenuItemClickListener(this);

        this.pagerPresenter = new PagerPresenter();
        this.pagerView      = new PagerView(context);

        ((ViewGroup)view.findViewById(R.id.content)).addView(pagerView);

        this.addView(view);
        this.updateLayout(BaseView.LAYOUT_SCREEN);
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> refreshClicks() {
        return this.refreshClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> clearClicks() {
        return this.clearClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> settingsClicks() {
        return this.settingsClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> newsClicks() {
        return this.newsClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> historiesClicks() {
        return this.historiesClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> bookmarksClicks() {
        return this.bookmarksClicks;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> aboutClicks() {
        return this.aboutClicks;
    }

    @Override
    public void refresh() {
        //
    }

    @Override
    public void clear() {
        //
    }

    @Override
    public void showSettings() {
        //
    }

    @Override
    public void showNews() {
        //
    }

    @Override
    public void showHistories() {
        //
    }

    @Override
    public void showBookmarks() {
        //
    }

    @Override
    public void showAbout() {
        Flow.get(this).set(AboutView.KEY);
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.pagerPresenter.onViewAttached(this.pagerView, this.isFirstAttachment);

        this.appBarLayout.addOnOffsetChangedListener(this);
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        this.pagerPresenter.onViewDetached();

        super.onDetachedFromWindow();

        this.appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        this.searchBar.setTranslationY(verticalOffset);
    }

    @Override
    public void onClick(@Nonnull @NonNull @lombok.NonNull final View view) {
        final Activity activity = this.getActivity();
        if (activity != null) {
            final NavigationView navigationView = (NavigationView)LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_main, null);
            navigationView.setNavigationItemSelectedListener(this);

            final BottomSheetDialog dialog = new RoundedBottomSheetDialog(activity);
            dialog.setContentView(navigationView);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            this.dialog = dialog;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@Nonnull @NonNull @lombok.NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_news:
                this.dialog.dismiss();
                this.newsClicks.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_histories:
                this.dialog.dismiss();
                this.historiesClicks.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_bookmarks:
                this.dialog.dismiss();
                this.bookmarksClicks.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_about:
                this.dialog.dismiss();
                this.aboutClicks.onNext(Irrelevant.INSTANCE);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onMenuItemClick(@Nonnull @NonNull @lombok.NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                this.refreshClicks.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_clear:
                this.clearClicks.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_settings:
                this.settingsClicks.onNext(Irrelevant.INSTANCE);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean handleBack() {
        if (this.searchBar.isSuggestionsVisible()) {
            this.searchBar.hideSuggestionsList();

            return true;
        }

        return false;
    }
}
