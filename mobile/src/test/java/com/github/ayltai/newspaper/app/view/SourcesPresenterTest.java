package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import com.github.ayltai.architecture.test.PresenterTest;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.MainActivity;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import java.util.Set;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class SourcesPresenterTest extends PresenterTest<MainActivity, SourcesPresenter, SourcesPresenter.View> {
    private final FlowableProcessor<Integer> optionsChanges = PublishProcessor.create();

    @NonNull
    @Override
    protected SourcesPresenter createPresenter() {
        return new SourcesPresenter();
    }

    @NonNull
    @Override
    protected SourcesPresenter.View createView() {
        final SourcesPresenter.View view = Mockito.mock(SourcesPresenter.View.class);

        Mockito.doReturn(this.optionsChanges).when(view).optionsChanges();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void When_onViewAttached_Then_addOptionIsCalled() {
        // When
        this.attachments.onNext(true);

        // Then
        Mockito.verify(this.getView(), Mockito.times(13)).addOption(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void When_optionsChanges_Then_setSourcesIsCalled() {
        // When
        this.attachments.onNext(true);
        this.optionsChanges.onNext(0);

        // Then
        final Set<String> sources = ComponentFactory.getInstance()
            .getConfigComponent(this.getView().getActivity())
            .userConfig().getSources();

        Assert.assertEquals(14, sources.size());
    }
}
