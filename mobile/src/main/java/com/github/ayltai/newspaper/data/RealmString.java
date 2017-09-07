package com.github.ayltai.newspaper.data;

import io.realm.RealmObject;

public class RealmString extends RealmObject {
    private String value;

    public RealmString() {
    }

    public RealmString(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
