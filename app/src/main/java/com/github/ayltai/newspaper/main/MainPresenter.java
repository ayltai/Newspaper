package com.github.ayltai.newspaper.main;

import java.io.Closeable;

import com.github.ayltai.newspaper.Presenter;

public final class MainPresenter extends Presenter<MainPresenter.View> {
    public interface View extends Presenter.View, Closeable {
    }
}
