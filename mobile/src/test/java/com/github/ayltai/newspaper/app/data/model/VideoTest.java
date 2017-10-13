package com.github.ayltai.newspaper.app.data.model;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class VideoTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Bundle bundle1 = new Bundle();
        final Video  video1  = new Video();

        bundle1.putParcelable("key", video1);

        Assert.assertEquals(video1, bundle1.getParcelable("key"));

        final Bundle bundle2 = new Bundle();
        final Video  video2  = new Video("url", "thumbnail");

        bundle2.putParcelable("key", video2);

        Assert.assertEquals(video2, bundle2.getParcelable("key"));
    }
}
