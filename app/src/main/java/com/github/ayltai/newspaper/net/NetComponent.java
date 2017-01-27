package com.github.ayltai.newspaper.net;

import com.github.ayltai.newspaper.ContextModule;

import dagger.Component;

@Component(modules = { NetModule.class, ContextModule.class})
public interface NetComponent {
    HttpClient httpClient();
}
