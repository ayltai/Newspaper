package com.github.ayltai.newspaper.app.widget;

import android.view.View;

import com.github.ayltai.newspaper.AppUnitTest;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.util.DateUtils;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CompactItemViewTest extends AppUnitTest {
    @Test
    public void testProperties() {
        final CompactItemView view   = new CompactItemView(RuntimeEnvironment.application);
        final Date            date   = new Date();
        final List<Image>     images = new ArrayList<>();
        final Image           image  = new Image("https://www.gravatar.com/avatar/4f637af7cf4d67e4301dd2f52cf43332");

        images.add(image);

        view.setSource("source");
        view.setPublishDate(date);
        view.setImages(images);
        view.setTitle("title");
        view.setDescription("description");

        Assert.assertEquals("source", view.getSource());
        Assert.assertEquals(DateUtils.toApproximateTime(RuntimeEnvironment.application, date.getTime()), view.getPublishDate());
        Assert.assertEquals(View.VISIBLE, view.getImageVisibility());
        Assert.assertEquals("title", view.getTitle().toString());
        Assert.assertEquals("description", view.getDescription().toString());
    }
}
