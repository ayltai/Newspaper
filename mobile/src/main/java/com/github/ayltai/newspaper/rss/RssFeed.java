package com.github.ayltai.newspaper.rss;

import java.util.List;

import android.support.annotation.Nullable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public final class RssFeed {
    @Element(name = "title", required = false)
    @Path("channel")
    private String title;

    @Element(name = "description", required = false)
    @Path("channel")
    private String description;

    @Element(name = "copyright", required = false)
    @Path("channel")
    private String copyright;

    @ElementList(name = "item", inline = true, required = false)
    @Path("channel")
    private List<RssItem> items;

    @Nullable
    public String getTitle() {
        return this.title;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getCopyright() {
        return this.copyright;
    }

    @Nullable
    public List<RssItem> getItems() {
        return this.items;
    }
}
