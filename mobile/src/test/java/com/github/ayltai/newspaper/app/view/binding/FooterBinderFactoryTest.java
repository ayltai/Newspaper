package com.github.ayltai.newspaper.app.view.binding;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.FooterView;

public final class FooterBinderFactoryTest extends UnitTest {
    @Test
    public void test() {
        final FooterBinderFactory factory = new FooterBinderFactory();

        Assert.assertEquals(FooterView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertFalse(factory.isNeeded(new NewsItem()));

        final NewsItem model = new NewsItem();
        model.setTitle("title");

        Assert.assertTrue(factory.isNeeded(model));

        model.setTitle(null);
        model.setDescription("description");

        Assert.assertTrue(factory.isNeeded(model));
    }
}
