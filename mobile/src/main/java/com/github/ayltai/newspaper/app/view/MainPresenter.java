package com.github.ayltai.newspaper.app.view;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.widget.MainView;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.Presenter;

import io.reactivex.Flowable;

public class MainPresenter extends Presenter<MainPresenter.View> {
    public interface View extends Presenter.View {
        void up();

        void refresh();

        void settings();

        void clearAll();

        @NonNull
        Flowable<Irrelevant> upActions();

        @NonNull
        Flowable<Irrelevant> refreshActions();

        @NonNull
        Flowable<Irrelevant> settingsActions();

        @NonNull
        Flowable<Irrelevant> clearAllActions();
    }

    public static final class Factory implements Presenter.Factory<MainPresenter, MainPresenter.View> {
        @Override
        public boolean isSupported(@NonNull final Object key) {
            return key instanceof MainView.Key;
        }

        @NonNull
        @Override
        public MainPresenter createPresenter() {
            return new MainPresenter();
        }

        @NonNull
        @Override
        public MainPresenter.View createView(@NonNull final Context context) {
            return new MainView(context);
        }
    }

    @Override
    public void onViewAttached(@NonNull final MainPresenter.View view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.manageDisposable(view.upActions().subscribe(irrelevant -> view.up()));
        this.manageDisposable(view.refreshActions().subscribe(irrelevant -> view.refresh()));
        this.manageDisposable(view.settingsActions().subscribe(irrelevant -> view.settings()));
        this.manageDisposable(view.clearAllActions().subscribe(irrelevant -> view.clearAll()));
    }
}
