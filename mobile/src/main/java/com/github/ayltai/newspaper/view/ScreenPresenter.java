package com.github.ayltai.newspaper.view;

public class ScreenPresenter<V extends ScreenPresenter.View> extends ObservablePresenter<V> {
    public interface View extends Presenter.View {
        /**
         * Navigates to the previous screen.
         * @return {@code true} if this view has handled the action to go back; otherwise; return {@code false}.
         */
        boolean goBack();
    }
}
