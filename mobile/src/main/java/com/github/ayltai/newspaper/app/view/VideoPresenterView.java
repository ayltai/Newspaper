package com.github.ayltai.newspaper.app.view;

public interface VideoPresenterView extends ItemPresenter.View {
    void setUpThumbnail();

    void setUpPlayer();

    void startPlayer();

    void releasePlayer();
}
