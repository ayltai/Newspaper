package com.github.ayltai.newspaper.app.screen;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.ScreenPresenter;

import io.reactivex.Flowable;

public class MainPresenter extends ScreenPresenter<MainPresenter.View> {
    public interface View extends ScreenPresenter.View {
        @NonNull
        Flowable<Irrelevant> goTopActions();

        @NonNull
        Flowable<Irrelevant> refreshActions();

        @NonNull
        Flowable<Irrelevant> filterActions();

        @NonNull
        Flowable<Integer> pageSelections();
    }
}
