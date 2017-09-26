package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class OptionsPresenter<M, V extends OptionsPresenter.View> extends ObservablePresenter<V> {
    public interface View extends Presenter.View {
        void addOption(@NonNull CharSequence text, boolean selected);

        @NonNull
        Flowable<Integer> optionsChanges();
    }

    protected final FlowableProcessor<List<M>> optionsChanges = PublishProcessor.create();

    @NonNull
    public Flowable<List<M>> optionsChanges() {
        return this.optionsChanges;
    }

    @NonNull
    protected abstract Single<List<M>> load();
}
