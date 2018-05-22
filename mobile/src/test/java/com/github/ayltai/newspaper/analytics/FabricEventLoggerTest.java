package com.github.ayltai.newspaper.analytics;

import com.github.ayltai.newspaper.UnitTest;

import org.junit.Assert;
import org.junit.Test;

public final class FabricEventLoggerTest extends UnitTest {
    @Test
    public void testLogAppOpenEvent() {
        final AppOpenEvent event = new AppOpenEvent();

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals(AppOpenEvent.NAME, event.getName());
    }

    @Test
    public void testLogCountEvent() {
        final CountEvent event = new CountEvent("name", 1);

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals("Count (name)", event.getName());
        Assert.assertEquals(CountEvent.ATTRIBUTE_COUNTER_NAME, event.getAttribute(CountEvent.ATTRIBUTE_COUNTER_NAME).getName());
        Assert.assertEquals(String.valueOf(1), event.getAttribute(CountEvent.ATTRIBUTE_COUNTER_NAME).getValue());
    }

    @Test
    public void testLogShareEvent() {
        final ShareEvent event = new ShareEvent()
            .setCategory("category")
            .setSource("source");

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals(ShareEvent.NAME, event.getName());
        Assert.assertEquals("category", event.getAttribute(ShareEvent.ATTRIBUTE_CATEGORY).getValue());
        Assert.assertEquals("source", event.getAttribute(ShareEvent.ATTRIBUTE_SOURCE).getValue());
    }

    @Test
    public void testLogSearchEvent() {
        final SearchEvent event = new SearchEvent()
            .setCategory("category")
            .setScreenName("screen")
            .setQuery("query");

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals(SearchEvent.NAME, event.getName());
        Assert.assertEquals("category", event.getAttribute(SearchEvent.ATTRIBUTE_CATEGORY).getValue());
        Assert.assertEquals("screen", event.getAttribute(SearchEvent.ATTRIBUTE_SCREEN_NAME).getValue());
        Assert.assertEquals("query", event.getAttribute(SearchEvent.ATTRIBUTE_QUERY).getValue());
    }

    @Test
    public void testLogViewEvent() {
        final ViewEvent event = new ViewEvent()
            .setCategory("category")
            .setSource("source")
            .setScreenName("screen");

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals(ViewEvent.NAME, event.getName());
        Assert.assertEquals("category", event.getAttribute(ViewEvent.ATTRIBUTE_CATEGORY).getValue());
        Assert.assertEquals("source", event.getAttribute(ViewEvent.ATTRIBUTE_SOURCE).getValue());
        Assert.assertEquals("screen", event.getAttribute(ViewEvent.ATTRIBUTE_SCREEN_NAME).getValue());
    }

    @Test
    public void testLogClickEvent() {
        final ClickEvent event = new ClickEvent()
            .setElementName("element");

        new FabricEventLogger().logEvent(event);

        Assert.assertEquals(ClickEvent.NAME, event.getName());
        Assert.assertEquals("element", event.getAttribute(ClickEvent.ATTRIBUTE_ELEMENT_NAME).getValue());
    }
}
