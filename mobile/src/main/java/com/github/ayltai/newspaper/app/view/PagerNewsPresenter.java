package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;

public class PagerNewsPresenter extends NewsPresenter {
    public interface View extends NewsPresenter.View {
        void settings();

        @NonNull
        Flowable<Integer> pageSelections();
    }
}
