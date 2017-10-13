package com.github.ayltai.newspaper.app.data.model;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class ImageTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Bundle bundle1 = new Bundle();
        final Image  image1  = new Image();

        bundle1.putParcelable("key", image1);

        Assert.assertEquals(image1, bundle1.getParcelable("key"));

        final Bundle bundle2 = new Bundle();
        final Image  image2  = new Image("url", "description");

        bundle2.putParcelable("key", image2);

        Assert.assertEquals(image2, bundle2.getParcelable("key"));
    }
}
