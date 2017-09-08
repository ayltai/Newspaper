package com.github.ayltai.newspaper.rss;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "enclosure", strict = false)
public final class Enclosure {
    @Attribute(name = "url", required = true)
    private String url;

    @Attribute(name = "length", required = false)
    private long length;

    @Attribute(name = "type", required = false)
    private String type;

    @NonNull
    public String getUrl() {
        return url;
    }

    public long getLength() {
        return length;
    }

    @Nullable
    public String getType() {
        return type;
    }
}
