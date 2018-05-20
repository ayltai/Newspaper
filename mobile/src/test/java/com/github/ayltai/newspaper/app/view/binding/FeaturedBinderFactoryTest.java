package com.github.ayltai.newspaper.app.view.binding;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.FeaturedView;

import org.junit.Assert;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Collections;

public final class FeaturedBinderFactoryTest extends UnitTest {
    @Test
    public void test() {
        final FeaturedBinderFactory factory = new FeaturedBinderFactory();

        Assert.assertEquals(FeaturedView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertFalse(factory.isNeeded(new NewsItem()));

        final NewsItem model = new NewsItem();
        model.setTitle("title");
        model.getImages().add(new Image());

        Assert.assertTrue(factory.isNeeded(FeaturedItem.create(Collections.singletonList(model))));
    }
}
