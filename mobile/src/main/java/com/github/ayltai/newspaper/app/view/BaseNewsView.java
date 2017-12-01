package com.github.ayltai.newspaper.app.view;

import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.Presenter;

public interface BaseNewsView extends Presenter.View {
    void up();

    void refresh();

    void clear();

    void search(@Nullable CharSequence newText);
}
