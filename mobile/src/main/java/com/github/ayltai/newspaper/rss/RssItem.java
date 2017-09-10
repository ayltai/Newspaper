package com.github.ayltai.newspaper.rss;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "item", strict = false)
public final class RssItem {
    @Path("title")
    @Text(data = true)
    private String title;

    @Path("link")
    @Text(required = false)
    private String link;

    @Path("guid")
    @Text(required = false)
    private String guid;

    @Path("description")
    @Text(required = false, data = true)
    private String description;

    @Path("pubDate")
    @Text(required = false)
    private String pubDate;

    @Element(name = "enclosure", required = false, type = Enclosure.class)
    private Enclosure enclosure;

    @NonNull
    public String getTitle() {
        return this.title.replace("\uFEFF", "");
    }

    public void setTitle(@NonNull final String title) {
        this.title = title;
    }

    @Nullable
    public String getLink() {
        return this.link == null ? this.guid : this.link;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getPubDate() {
        return this.pubDate;
    }

    @Nullable
    public Enclosure getEnclosure() {
        return this.enclosure;
    }
}
