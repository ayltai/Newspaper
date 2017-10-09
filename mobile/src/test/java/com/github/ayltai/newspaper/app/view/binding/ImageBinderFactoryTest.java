package com.github.ayltai.newspaper.app.view.binding;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.ImageView;

public final class ImageBinderFactoryTest extends UnitTest {
    @Test
    public void test() {
        final ImageBinderFactory factory = new ImageBinderFactory();

        Assert.assertEquals(ImageView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertFalse(factory.isNeeded(new NewsItem()));

        final NewsItem model = new NewsItem();
        model.getImages().add(new Image());

        Assert.assertTrue(factory.isNeeded(model));
    }
}
