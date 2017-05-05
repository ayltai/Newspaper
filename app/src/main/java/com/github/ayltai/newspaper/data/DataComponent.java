package com.github.ayltai.newspaper.data;

import com.github.ayltai.newspaper.main.MainAdapter;

import dagger.Component;

@Component(modules = { DataModule.class })
public interface DataComponent {
    void inject(MainAdapter mainAdapter);
}
