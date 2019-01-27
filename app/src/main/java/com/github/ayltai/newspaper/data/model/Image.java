package com.github.ayltai.newspaper.data.model;

import io.realm.RealmObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Image extends RealmObject {
    @Getter
    private String imageUrl;

    @Getter
    private String description;
}
