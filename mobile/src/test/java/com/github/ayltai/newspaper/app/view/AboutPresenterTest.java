package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.PresenterTest;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class AboutPresenterTest extends PresenterTest<MainActivity, AboutPresenter, AboutPresenter.View> {
    private final FlowableProcessor<Irrelevant> visitActions  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> rateActions   = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> reportActions = PublishProcessor.create();

    @NonNull
    @Override
    protected AboutPresenter createPresenter() {
        return new AboutPresenter();
    }

    @NonNull
    @Override
    protected AboutPresenter.View createView() {
        final AboutPresenter.View view = Mockito.mock(AboutPresenter.View.class);

        Mockito.doReturn(this.visitActions).when(view).visitActions();
        Mockito.doReturn(this.rateActions).when(view).rateActions();
        Mockito.doReturn(this.reportActions).when(view).reportActions();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void When_onViewAttached_Then_propertiesAreSet() {
        // When
        this.attaches.onNext(true);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).setAppName(Mockito.anyString());
        Mockito.verify(this.getView(), Mockito.times(1)).setAppIcon(Mockito.anyInt());
        Mockito.verify(this.getView(), Mockito.times(1)).setAppVersion(Mockito.anyString());
    }

    @Test
    public void Given_onViewAttached_When_visitActions_Then_visitIsCalled() {
        // Given
        this.attaches.onNext(true);

        // When
        this.visitActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).visit(Mockito.anyString());
    }

    @Test
    public void Given_onViewAttached_When_rateActions_Then_rateIsCalled() {
        // Given
        this.attaches.onNext(true);

        // When
        this.rateActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).rate();
    }

    @Test
    public void Given_onViewAttached_When_reportActions_Then_reportIsCalled() {
        // Given
        this.attaches.onNext(true);

        // When
        this.reportActions.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).report(Mockito.anyString());
    }
}
