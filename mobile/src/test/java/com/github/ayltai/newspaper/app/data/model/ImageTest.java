package com.github.ayltai.newspaper.app.data.model;

import android.os.Parcel;

import com.github.ayltai.newspaper.AppUnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class ImageTest extends AppUnitTest {
    @Test
    public void testParcelable() {
        final Parcel parcel1 = Parcel.obtain();
        final Image  image1  = new Image();

        image1.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        Assert.assertEquals(image1.toString(), Image.CREATOR.createFromParcel(parcel1).toString());

        parcel1.recycle();

        final Parcel parcel2 = Parcel.obtain();
        final Image  image2  = new Image("url", "name");

        image2.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        Assert.assertEquals(image2.toString(), Image.CREATOR.createFromParcel(parcel2).toString());

        parcel2.recycle();
    }
}
