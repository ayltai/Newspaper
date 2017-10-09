package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import com.github.ayltai.newspaper.analytics.EventLogger;
import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.view.PresenterTest;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class SettingsPresenterTest extends PresenterTest<MainActivity, SettingsPresenter, SettingsPresenter.View> {
    private final FlowableProcessor<Integer> optionsChanges = PublishProcessor.create();

    @NonNull
    @Override
    protected SettingsPresenter createPresenter() {
        final SettingsPresenter presenter = Mockito.spy(new SettingsPresenter());

        return presenter;
    }

    @NonNull
    @Override
    protected SettingsPresenter.View createView() {
        final SettingsPresenter.View view = Mockito.mock(SettingsPresenter.View.class);

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
        Mockito.verify(this.getView(), Mockito.times(4)).addOption(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void When_optionsChanges_Then_settingsAreUpdated() {
        // When
        this.attachments.onNext(true);
        this.optionsChanges.onNext(0);

        // Then
        Mockito.verify(this.getPresenter(), Mockito.times(1)).updateViewStyle(Mockito.anyList(), Mockito.any(UserConfig.class), Mockito.any(EventLogger.class));

        this.optionsChanges.onNext(1);
        Mockito.verify(this.getPresenter(), Mockito.times(1)).updateTheme(Mockito.anyList(), Mockito.any(UserConfig.class), Mockito.any(EventLogger.class));

        this.optionsChanges.onNext(2);
        Mockito.verify(this.getPresenter(), Mockito.times(1)).updateAutoPlay(Mockito.anyList(), Mockito.any(UserConfig.class), Mockito.any(EventLogger.class));

        this.optionsChanges.onNext(3);
        Mockito.verify(this.getPresenter(), Mockito.times(1)).updatePanorama(Mockito.anyList(), Mockito.any(UserConfig.class), Mockito.any(EventLogger.class));
    }
}
