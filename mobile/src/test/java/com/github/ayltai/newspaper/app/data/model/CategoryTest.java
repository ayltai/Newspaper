package com.github.ayltai.newspaper.app.data.model;

import android.os.Parcel;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class CategoryTest extends AppUnitTest {
    @Test
    public void testParcelable() {
        final Parcel   parcel1   = Parcel.obtain();
        final Category category1 = new Category();

        category1.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        Assert.assertEquals(category1.toString(), Category.CREATOR.createFromParcel(parcel1).toString());

        parcel1.recycle();

        final Parcel   parcel2   = Parcel.obtain();
        final Category category2 = new Category("url", "name");

        category2.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        Assert.assertEquals(category2.toString(), Category.CREATOR.createFromParcel(parcel2).toString());

        parcel2.recycle();
    }
}
