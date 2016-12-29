package com.github.ayltai.newspaper;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

@RunWith(JUnit4.class)
public abstract class PresenterTest<P extends Presenter, V extends Presenter.View> {
    private final PublishSubject<Void> attachments = PublishSubject.create();
    private final PublishSubject<Void> detachments = PublishSubject.create();

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private P presenter;
    private V view;

    @NonNull
    protected P getPresenter() {
        return this.presenter;
    }

    @NonNull
    protected V getView() {
        return this.view;
    }

    @NonNull
    protected abstract P createPresenter();

    @NonNull
    protected abstract V createView();

    @CallSuper
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.presenter = this.createPresenter();
        this.view      = this.createView();

        Mockito.when(this.view.attachments()).thenReturn(this.attachments);
        Mockito.when(this.view.detachments()).thenReturn(this.detachments);

        this.subscriptions.add(this.view.attachments().subscribe(dummy -> this.presenter.onViewAttached(this.view)));
        this.subscriptions.add(this.view.detachments().subscribe(dummy -> this.presenter.onViewDetached()));

        this.attachments.onNext(null);
    }

    @CallSuper
    @After
    public void tearDown() throws Exception {
        this.detachments.onNext(null);

        this.subscriptions.unsubscribe();
    }
}
