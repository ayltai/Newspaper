package com.github.ayltai.newspaper.app.data.model;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class CategoryTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Bundle   bundle1   = new Bundle();
        final Category category1 = new Category();

        bundle1.putParcelable("key", category1);

        Assert.assertEquals(category1, bundle1.getParcelable("key"));

        final Bundle   bundle2   = new Bundle();
        final Category category2 = new Category("url", "name");

        bundle2.putParcelable("key", category2);

        Assert.assertEquals(category2, bundle2.getParcelable("key"));
    }
}
