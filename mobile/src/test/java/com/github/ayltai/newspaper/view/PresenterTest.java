package com.github.ayltai.newspaper.view;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;

public abstract class PresenterTest<A extends Activity, P extends Presenter<V>, V extends Presenter.View> extends UnitTest {
    protected final PublishProcessor<Boolean>    attachments = PublishProcessor.create();
    protected final PublishProcessor<Irrelevant> detachments = PublishProcessor.create();

    private final CompositeDisposable disposables = new CompositeDisposable();

    private P presenter;
    private V view;
    private A activity;

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

    @NonNull
    protected abstract A createActivity();

    @CallSuper
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.activity  = this.createActivity();
        this.view      = this.createView();
        this.presenter = this.createPresenter();

        Mockito.when(this.view.getContext()).thenReturn(RuntimeEnvironment.application);
        Mockito.when(this.view.getActivity()).thenReturn(this.activity);
        Mockito.when(this.view.attachments()).thenReturn(this.attachments);
        Mockito.when(this.view.detachments()).thenReturn(this.detachments);

        this.disposables.add(this.view.attachments().subscribe(isFirstTimeAttachment -> this.presenter.onViewAttached(this.view, isFirstTimeAttachment)));
        this.disposables.add(this.view.detachments().subscribe(dummy -> this.presenter.onViewDetached()));
    }

    @CallSuper
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        this.detachments.onNext(Irrelevant.INSTANCE);

        this.disposables.dispose();
    }
}
