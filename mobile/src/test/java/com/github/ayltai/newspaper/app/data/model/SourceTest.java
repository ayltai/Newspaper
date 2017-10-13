package com.github.ayltai.newspaper.app.data.model;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

import io.realm.RealmList;

public final class SourceTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Bundle bundle1 = new Bundle();
        final Source source1 = new Source();

        bundle1.putParcelable("key", source1);

        Assert.assertEquals(source1, bundle1.getParcelable("key"));

        final Bundle bundle2 = new Bundle();
        final Source source2 = new Source("name", new RealmList<>(), 0);

        bundle2.putParcelable("key", source2);

        Assert.assertEquals(source2, bundle2.getParcelable("key"));
    }
}
