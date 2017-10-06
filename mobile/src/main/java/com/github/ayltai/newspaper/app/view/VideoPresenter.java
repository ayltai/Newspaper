package com.github.ayltai.newspaper.app.view;

public class VideoPresenter extends ItemPresenter<VideoPresenter.View> {
    public interface View extends ItemPresenter.View {
        void setUpThumbnail();

        void setUpPlayer();

        void startPlayer();

        void releasePlayer();
    }
}
