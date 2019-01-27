package com.github.ayltai.newspaper.data.model;

import io.realm.RealmObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Video extends RealmObject {
    @Getter
    private String videoUrl;

    @Getter
    private String imageUrl;
}
