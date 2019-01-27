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
import com.github.ayltai.newspaper.view.ItemPagerPresenter;
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

    private final FlowableProcessor<Irrelevant> refreshActions       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clearActions         = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> showSettingsActions  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> showNewsActions      = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> showHistoriesActions = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> showBookmarksActions = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> showAboutActions     = PublishProcessor.create();

    private final AppBarLayout       appBarLayout;
    private final MaterialSearchBar  searchBar;
    private final ItemPagerPresenter itemPagerPresenter;
    private final ItemPagerView itemPagerView;

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

        this.itemPagerPresenter = new ItemPagerPresenter();
        this.itemPagerView = new ItemPagerView(context);

        ((ViewGroup)view.findViewById(R.id.content)).addView(itemPagerView);

        this.addView(view);
        this.updateLayout(BaseView.LAYOUT_SCREEN);
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> refreshActions() {
        return this.refreshActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> clearActions() {
        return this.clearActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> showSettingsActions() {
        return this.showSettingsActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> showNewsActions() {
        return this.showNewsActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> showHistoriesActions() {
        return this.showHistoriesActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> showBookmarksActions() {
        return this.showBookmarksActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> showAboutActions() {
        return this.showAboutActions;
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

        this.itemPagerPresenter.onViewAttached(this.itemPagerView, this.isFirstAttachment);

        this.appBarLayout.addOnOffsetChangedListener(this);
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        this.itemPagerPresenter.onViewDetached();

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
                this.showNewsActions.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_histories:
                this.dialog.dismiss();
                this.showHistoriesActions.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_bookmarks:
                this.dialog.dismiss();
                this.showBookmarksActions.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_about:
                this.dialog.dismiss();
                this.showAboutActions.onNext(Irrelevant.INSTANCE);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onMenuItemClick(@Nonnull @NonNull @lombok.NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                this.refreshActions.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_clear:
                this.clearActions.onNext(Irrelevant.INSTANCE);
                return true;

            case R.id.action_settings:
                this.showSettingsActions.onNext(Irrelevant.INSTANCE);
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
