package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.Single;

public abstract class OptionsPresenter<M, V extends OptionsPresenter.View> extends Presenter<V> {
    public interface View extends Presenter.View {
        void addOption(@NonNull CharSequence text, boolean selected);

        @NonNull
        Flowable<Integer> optionsChanges();
    }

    @NonNull
    protected abstract Single<List<M>> load();
}
