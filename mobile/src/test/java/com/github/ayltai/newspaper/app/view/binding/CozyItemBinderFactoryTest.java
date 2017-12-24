package com.github.ayltai.newspaper.app.view.binding;

import org.junit.Assert;
import org.junit.Test;

import com.github.ayltai.newspaper.UnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.CozyItemView;

public final class CozyItemBinderFactoryTest extends UnitTest {
    @Test
    public void test() {
        final CozyItemBinderFactory factory = new CozyItemBinderFactory();

        Assert.assertEquals(CozyItemView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertTrue(factory.isNeeded(new NewsItem()));
    }
}
