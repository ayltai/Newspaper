package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.widget.MainView;

import io.reactivex.Flowable;

public final class MainPresenter extends BasePresenter<MainPresenter.View> {
    public interface View extends Presenter.View {
        @Nonnull
        @NonNull
        Flowable<Irrelevant> refreshActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> clearActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> showSettingsActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> showNewsActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> showHistoriesActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> showBookmarksActions();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> showAboutActions();

        void refresh();

        void clear();

        void showSettings();

        void showNews();

        void showHistories();

        void showBookmarks();

        void showAbout();
    }

    public static final class Factory implements Presenter.Factory<MainPresenter, MainPresenter.View> {
        @Override
        public boolean isSupported(@Nonnull @NonNull @lombok.NonNull final Object key) {
            return key instanceof MainView.Key;
        }

        @Nonnull
        @NonNull
        @Override
        public MainPresenter createPresenter() {
            return new MainPresenter();
        }

        @Nonnull
        @NonNull
        @Override
        public MainPresenter.View createView(@Nonnull @NonNull @lombok.NonNull final Context context) {
            return new MainView(context);
        }
    }

    @CallSuper
    @Override
    public void onViewAttached(@Nonnull @NonNull @lombok.NonNull final MainPresenter.View view, final boolean isFirstAttachment) {
        super.onViewAttached(view, isFirstAttachment);

        this.manageDisposable(view.refreshActions().subscribe(irrelevant -> view.refresh()));
        this.manageDisposable(view.clearActions().subscribe(irrelevant -> view.clear()));
        this.manageDisposable(view.showSettingsActions().subscribe(irrelevant -> view.showSettings()));
        this.manageDisposable(view.showNewsActions().subscribe(irrelevant -> view.showNews()));
        this.manageDisposable(view.showHistoriesActions().subscribe(irrelevant -> view.showHistories()));
        this.manageDisposable(view.showBookmarksActions().subscribe(irrelevant -> view.showBookmarks()));
        this.manageDisposable(view.showAboutActions().subscribe(irrelevant -> view.showAbout()));
    }
}
