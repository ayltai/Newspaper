package com.github.ayltai.newspaper.app.screen;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.view.ScreenPresenter;

import io.reactivex.Flowable;

public class MainPresenter extends ScreenPresenter<MainPresenter.View> {
    public interface View extends ScreenPresenter.View {
        @NonNull
        Flowable<Integer> pageChanges();
    }
}
