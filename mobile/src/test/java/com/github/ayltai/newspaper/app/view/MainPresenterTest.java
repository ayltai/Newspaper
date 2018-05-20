package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.PresenterTest;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class MainPresenterTest extends PresenterTest<MainActivity, MainPresenter, MainPresenter.View> {
    private final FlowableProcessor<Irrelevant> upActions       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> refreshActions  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> filterActions   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clearAllActions = PublishProcessor.create();

    @NonNull
    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @NonNull
    @Override
    protected MainPresenter.View createView() {
        final MainPresenter.View view = Mockito.mock(MainPresenter.View.class);

        Mockito.doReturn(this.upActions).when(view).upActions();
        Mockito.doReturn(this.refreshActions).when(view).refreshActions();
        Mockito.doReturn(this.filterActions).when(view).settingsActions();
        Mockito.doReturn(this.clearAllActions).when(view).clearAllActions();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void Give_onViewAttached_When_upActions_Then_upIsCalled() {
        // Given
        this.attaches.onNext(Boolean.TRUE);

        // When
        this.upActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).up();
    }

    @Test
    public void Give_onViewAttached_When_refreshActions_Then_refreshIsCalled() {
        // Given
        this.attaches.onNext(Boolean.TRUE);

        // When
        this.refreshActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).refresh();
    }

    @Test
    public void Give_onViewAttached_When_filterActions_Then_settingsIsCalled() {
        // Given
        this.attaches.onNext(Boolean.TRUE);

        // When
        this.filterActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).settings();
    }

    @Test
    public void Give_onViewAttached_When_clearAllActions_Then_clearAllIsCalled() {
        // Given
        this.attaches.onNext(Boolean.TRUE);

        // When
        this.clearAllActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).clearAll();
    }
}
