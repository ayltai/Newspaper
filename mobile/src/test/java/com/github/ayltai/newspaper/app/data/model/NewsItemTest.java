package com.github.ayltai.newspaper.app.data.model;

import java.util.Date;

import android.os.Parcel;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;

public final class NewsItemTest extends UnitTest {
    @Test
    public void testParcelable() {
        final Parcel   parcel1 = Parcel.obtain();
        final NewsItem item1   = new NewsItem();

        item1.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);

        Assert.assertEquals(item1.toString(), NewsItem.CREATOR.createFromParcel(parcel1).toString());

        parcel1.recycle();

        final Parcel   parcel2 = Parcel.obtain();
        final NewsItem item2   = new NewsItem();

        item2.setTitle("title");
        item2.setDescription("description");
        item2.setCategory("category");
        item2.setLastAccessedDate(new Date());
        item2.setLink("link");
        item2.setSource("source");

        item2.writeToParcel(parcel2, 0);
        parcel2.setDataPosition(0);

        Assert.assertEquals(item2.toString(), NewsItem.CREATOR.createFromParcel(parcel2).toString());

        parcel2.recycle();
    }
}
