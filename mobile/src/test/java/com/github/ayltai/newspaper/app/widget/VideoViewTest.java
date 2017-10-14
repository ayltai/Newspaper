package com.github.ayltai.newspaper.app.widget;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.subscribers.TestSubscriber;

public final class VideoViewTest extends UnitTest {
    @Test
    public void test() {
        final TestSubscriber<Irrelevant> subscriber = new TestSubscriber<>();
        final VideoView                  view       = Mockito.spy(new VideoView(RuntimeEnvironment.application));

        Mockito.doNothing().when(view).setUpPlayer();

        view.onAttachedToWindow();
        view.setVideo(new Video("url", "thumbnail"));

        view.videoClicks().subscribe(subscriber);

        view.playAction.performClick();

        subscriber.assertValue(Irrelevant.INSTANCE);
    }
}
