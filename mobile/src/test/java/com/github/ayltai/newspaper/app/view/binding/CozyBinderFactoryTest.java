package com.github.ayltai.newspaper.app.view.binding;

import com.github.ayltai.newspaper.AppUnitTest;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.app.widget.CozyItemView;

import org.junit.Assert;
import org.junit.Test;

public final class CozyBinderFactoryTest extends AppUnitTest {
    @Test
    public void test() {
        final CozyBinderFactory factory = new CozyBinderFactory();

        Assert.assertEquals(CozyItemView.VIEW_TYPE, factory.getPartType());
        Assert.assertFalse(factory.isNeeded(null));
        Assert.assertTrue(factory.isNeeded(new NewsItem()));
    }
}
