package com.github.ayltai.newspaper.app.widget;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.Image;

public final class ImageViewTest extends UnitTest {
    @Test
    public void testProperties() {
        final ImageView view = new ImageView(RuntimeEnvironment.application);

        final List<Image> images = new ArrayList<>();
        final Image       image  = new Image("https://www.gravatar.com/avatar/4f637af7cf4d67e4301dd2f52cf43332");

        images.add(image);

        view.setImages(images);

        Assert.assertEquals(View.VISIBLE, view.getImageVisibility());
    }
}
