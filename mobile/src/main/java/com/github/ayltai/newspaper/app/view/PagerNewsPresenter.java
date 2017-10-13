package com.github.ayltai.newspaper.app.view;

import com.github.ayltai.newspaper.view.ObservablePresenter;

public class PagerNewsPresenter extends ObservablePresenter<NewsPresenterView> {
    public interface View extends NewsPresenterView {
        void settings();
    }
}
