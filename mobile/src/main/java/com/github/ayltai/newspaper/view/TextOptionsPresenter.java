package com.github.ayltai.newspaper.view;

import java.util.List;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class TextOptionsPresenter<M, V extends TextOptionsPresenter.View> extends ObservablePresenter<V> {
    public interface View extends Presenter.View {
        void addText(@NonNull CharSequence text, boolean selected);

        @NonNull
        Flowable<Integer> selects();
    }

    protected final FlowableProcessor<List<M>> selectionChanges = PublishProcessor.create();

    @NonNull
    public Flowable<List<M>> selectionChanges() {
        return this.selectionChanges;
    }

    @NonNull
    protected abstract Single<List<M>> load();
}
