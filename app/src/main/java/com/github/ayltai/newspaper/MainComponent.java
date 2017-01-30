package com.github.ayltai.newspaper;

import com.github.ayltai.newspaper.item.ItemPresenter;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.main.MainAdapter;
import com.github.ayltai.newspaper.main.MainPresenter;

import dagger.Component;

@Component(modules = { MainModule.class })
public interface MainComponent {
    MainPresenter mainPresenter();

    MainPresenter.View mainView();

    MainAdapter mainAdapter();

    ListPresenter listPresenter();

    ListPresenter.View listView();

    ItemPresenter itemPresenter();

    ItemPresenter.View itemView();

    void inject(MainActivity activity);
}
