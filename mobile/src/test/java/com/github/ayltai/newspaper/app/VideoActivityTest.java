package com.github.ayltai.newspaper.app;

import android.content.Intent;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public final class VideoActivityTest extends UnitTest {
    @Test
    public void testCreateIntent() {
        final Intent intent = VideoActivity.createIntent(RuntimeEnvironment.application, "url", false, 0);

        Assert.assertEquals("url", intent.getStringExtra(VideoActivity.EXTRA_VIDEO_URL));
        Assert.assertEquals(false, intent.getBooleanExtra(VideoActivity.EXTRA_IS_PLAYING, false));
        Assert.assertEquals(0, intent.getLongExtra(VideoActivity.EXTRA_SEEK_POSITION, 0));
    }
}
