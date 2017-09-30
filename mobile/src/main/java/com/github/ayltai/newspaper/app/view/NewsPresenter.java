package com.github.ayltai.newspaper.app.view;

import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.ObservablePresenter;
import com.github.ayltai.newspaper.view.Presenter;

public abstract class NewsPresenter extends ObservablePresenter<NewsPresenter.View> {
    public interface View extends Presenter.View {
        void up();

        void refresh();

        void search(@Nullable CharSequence newText);
    }
}
