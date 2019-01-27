package com.github.ayltai.newspaper.data.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Item extends RealmObject {
    //region Constants

    public static final String FIELD_TITLE        = "title";
    public static final String FIELD_DESCRIPTION  = "description";
    public static final String FIELD_SOURCE       = "source.name";
    public static final String FIELD_CATEGORY     = "category.name";
    public static final String FIELD_PUBLISH_DATE = "publishDate";

    //endregion

    @Getter
    private String title;

    @Getter
    private String description;

    @Getter
    @PrimaryKey
    private String url;

    @Getter
    private Date publishDate;

    @Getter
    private Source source;

    @Getter
    private Category category;

    @Getter
    private RealmList<Image> images;

    @Getter
    private RealmList<Video> videos;

    @Getter
    @Setter
    private boolean isRead;

    @Getter
    @Setter
    private boolean isBookmarked;
}
