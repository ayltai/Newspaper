package com.github.ayltai.newspaper.graphics;

import com.github.ayltai.newspaper.main.MainScreen;

import dagger.Component;

@Component(modules = { GraphicsModule.class })
public interface GraphicsComponent {
    void inject(MainScreen mainScreen);
}
