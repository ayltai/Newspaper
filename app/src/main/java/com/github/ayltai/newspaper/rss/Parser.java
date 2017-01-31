package com.github.ayltai.newspaper.rss;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.util.ItemUtils;

final class Parser {
    //region Constants

    private static final String TAG_RSS     = "rss";
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_ITEM    = "item";

    //endregion

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        }
    };

    private Parser() {
    }

    @NonNull
    static List<Item> parse(@Nullable final InputStream inputStream) throws XmlPullParserException, IOException {
        if (inputStream == null) return new ArrayList<>();

        final XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, Constants.ENCODING_UTF8);
        parser.nextTag();

        return Parser.readChannel(parser);
    }

    @NonNull
    private static List<Item> readChannel(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final List<Item> items = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, Parser.TAG_RSS);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (Parser.TAG_CHANNEL.equals(parser.getName())) {
                items.addAll(Parser.readItems(parser));
            } else {
                Parser.skipTags(parser);
            }
        }

        return items;
    }

    @NonNull
    private static List<Item> readItems(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final List<Item> items = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, Parser.TAG_CHANNEL);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            if (Parser.TAG_ITEM.equals(parser.getName())) {
                final Item item = Parser.readItem(parser);

                if (ItemUtils.filter(item)) continue;

                items.add(item);
            } else {
                Parser.skipTags(parser);
            }
        }

        return items;
    }

    @NonNull
    private static Item readItem(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, Parser.TAG_ITEM);

        final Item item = new Item();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) continue;

            final String name = parser.getName();

            switch (name) {
                case Item.TAG_TITLE:
                    item.title = Parser.readTag(parser, name);
                    break;

                case Item.TAG_DESCRIPTION:
                    item.description = Parser.readTag(parser, name);
                    break;

                case Item.TAG_LINK:
                    item.link = Parser.readTag(parser, name);
                    break;

                case Item.TAG_SOURCE:
                    item.source = Parser.readTag(parser, name);
                    break;

                case Item.TAG_PUBLISH_DATE:
                    item.publishDate = Parser.readPublishDate(parser);
                    break;

                case Item.TAG_MEDIA_CONTENT:
                    item.mediaUrl = Parser.readMediaUrl(parser);
                    break;

                case Item.TAG_GUID:
                    item.guid = Parser.readTag(parser, name);
                    break;

                default:
                    Parser.skipTags(parser);
                    break;
            }
        }

        return item;
    }

    private static long readPublishDate(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final String value = Parser.readTag(parser, Item.TAG_PUBLISH_DATE);

        try {
            return Parser.DATE_FORMAT.get().parse(value).getTime();
        } catch (final ParseException e) {
            Log.e(Parser.class.getName(), e.getMessage(), e);
        }

        return 0;
    }

    @NonNull
    private static String readMediaUrl(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, Item.TAG_MEDIA_CONTENT);

        final String value = parser.getAttributeValue(null, Item.ATTR_URL);
        parser.nextTag();

        parser.require(XmlPullParser.END_TAG, null, Item.TAG_MEDIA_CONTENT);

        return value;
    }

    @Nullable
    private static String readTag(@NonNull final XmlPullParser parser, @NonNull final String tag) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, tag);

        String value = null;

        if (parser.next() == XmlPullParser.TEXT) {
            value = parser.getText();
            parser.nextTag();
        }

        parser.require(XmlPullParser.END_TAG, null, tag);

        return value;
    }

    private static void skipTags(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) throw new IllegalStateException();

        int depth = 1;

        while (depth != 0) {
            final int type = parser.next();

            if (type == XmlPullParser.END_TAG) {
                depth--;
            } else if (type == XmlPullParser.START_TAG) {
                depth++;
            }
        }
    }
}
