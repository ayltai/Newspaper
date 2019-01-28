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
        Flowable<Irrelevant> refreshClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> clearClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> settingsClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> newsClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> historiesClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> bookmarksClicks();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> aboutClicks();

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

        this.manageDisposable(view.refreshClicks().subscribe(irrelevant -> view.refresh()));
        this.manageDisposable(view.clearClicks().subscribe(irrelevant -> view.clear()));
        this.manageDisposable(view.settingsClicks().subscribe(irrelevant -> view.showSettings()));
        this.manageDisposable(view.newsClicks().subscribe(irrelevant -> view.showNews()));
        this.manageDisposable(view.historiesClicks().subscribe(irrelevant -> view.showHistories()));
        this.manageDisposable(view.bookmarksClicks().subscribe(irrelevant -> view.showBookmarks()));
        this.manageDisposable(view.aboutClicks().subscribe(irrelevant -> view.showAbout()));
    }
}
