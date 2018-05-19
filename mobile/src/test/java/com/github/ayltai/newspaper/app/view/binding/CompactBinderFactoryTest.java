package com.github.ayltai.newspaper.app.view.binding;

import com.github.ayltai.newspaper.AppUnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.CompactItemView;

import org.junit.Assert;
import org.junit.Test;

public final class CompactBinderFactoryTest extends AppUnitTest {
    @Test
    public void test() {
        final CompactBinderFactory factory = new CompactBinderFactory();

        Assert.assertEquals(CompactItemView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertTrue(factory.isNeeded(new NewsItem()));
    }
}
