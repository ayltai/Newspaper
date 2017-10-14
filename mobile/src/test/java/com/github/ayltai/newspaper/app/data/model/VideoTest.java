package com.github.ayltai.newspaper.app.data.model;

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class VideoTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Parcel parcel1 = Parcel.obtain();
        final Video  video1  = new Video();

        video1.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        Assert.assertEquals(video1.toString(), Video.CREATOR.createFromParcel(parcel1).toString());

        parcel1.recycle();

        final Parcel parcel2 = Parcel.obtain();
        final Video  video2  = new Video("url", "thumbnail");

        video2.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        Assert.assertEquals(video2.toString(), Video.CREATOR.createFromParcel(parcel2).toString());

        parcel2.recycle();
    }
}
