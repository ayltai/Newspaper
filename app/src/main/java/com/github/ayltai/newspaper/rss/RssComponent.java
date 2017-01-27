package com.github.ayltai.newspaper.rss;

import com.github.ayltai.newspaper.ContextModule;
import com.github.ayltai.newspaper.net.NetModule;

import dagger.Component;

@Component(modules = { RssModule.class, NetModule.class, ContextModule.class})
public interface RssComponent {
    Client client();
}
