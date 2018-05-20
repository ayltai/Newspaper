package com.github.ayltai.newspaper.app.data.model;

import android.os.Parcel;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

import io.realm.RealmList;

public final class SourceTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Parcel parcel1 = Parcel.obtain();
        final Source source1 = new Source();

        source1.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        Assert.assertEquals(source1.toString(), Source.CREATOR.createFromParcel(parcel1).toString());

        parcel1.recycle();

        final Parcel parcel2 = Parcel.obtain();
        final Source source2 = new Source("name", new RealmList<>(), 0);

        source2.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        Assert.assertEquals(source2.toString(), Source.CREATOR.createFromParcel(parcel2).toString());

        parcel2.recycle();
    }
}
