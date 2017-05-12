package com.github.ayltai.newspaper;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public abstract class PresenterTest<P extends Presenter, V extends Presenter.View> {
    private final PublishProcessor<Object> attachments = PublishProcessor.create();
    private final PublishProcessor<Object> detachments = PublishProcessor.create();

    private final CompositeDisposable disposables = new CompositeDisposable();

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
        this.presenter = this.createPresenter();
        this.view      = this.createView();

        Mockito.when(this.view.getContext()).thenReturn(RuntimeEnvironment.application);
        Mockito.when(this.view.attachments()).thenReturn(this.attachments);
        Mockito.when(this.view.detachments()).thenReturn(this.detachments);

        this.disposables.add(this.view.attachments().subscribe(dummy -> this.presenter.onViewAttached(this.view)));
        this.disposables.add(this.view.detachments().subscribe(dummy -> this.presenter.onViewDetached()));

        this.attachments.onNext(Irrelevant.INSTANCE);
    }

    @CallSuper
    @After
    public void tearDown() throws Exception {
        this.detachments.onNext(Irrelevant.INSTANCE);

        this.disposables.dispose();
    }
}
