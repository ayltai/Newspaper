package com.github.ayltai.newspaper.data;

import dagger.Component;
import io.realm.Realm;

@Component(modules = { DataModule.class })
public interface DataComponent {
    Realm realm();
}
