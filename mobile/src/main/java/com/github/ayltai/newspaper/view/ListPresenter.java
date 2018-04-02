package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;

public abstract class ListPresenter<M, V extends ListPresenter.View<M>> extends ModelPresenter<List<M>, V> {
    public interface View<M> extends Presenter.View {
        void bind(@NonNull List<M> models);

        void scrollTo(int scrollPosition, boolean smoothScroll);

        @NonNull
        Flowable<Integer> bestVisibleItemPositionChanges();
    }

    protected int scrollPosition;

    @NonNull
    public abstract Flowable<List<M>> load();
}
