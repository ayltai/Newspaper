package com.github.ayltai.newspaper;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.item.ItemPresenter;
import com.github.ayltai.newspaper.item.ItemScreen;
import com.github.ayltai.newspaper.list.ListPresenter;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.main.MainAdapter;
import com.github.ayltai.newspaper.main.MainPresenter;
import com.github.ayltai.newspaper.main.MainScreen;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

@Module
public final class MainModule {
    private final Activity activity;

    public MainModule(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @Provides
    FlowController provideFlowController() {
        return new FlowController(this.activity);
    }

    @Provides
    static MainPresenter provideMainPresenter() {
        return new MainPresenter();
    }

    @Provides
    MainPresenter.View provideMainView() {
        return new MainScreen(this.activity);
    }

    @Provides
    MainAdapter provideMainAdapter() {
        return new MainAdapter(this.activity);
    }

    @Provides
    static ListPresenter provideListPresenter() {
        return new ListPresenter();
    }

    @Provides
    ListPresenter.View provideListView() {
        return new ListScreen(this.activity);
    }

    @Provides
    static ItemPresenter provideItemPresenter() {
        return new ItemPresenter(Realm.getDefaultInstance());
    }

    @Provides
    ItemPresenter.View provideItemView() {
        return new ItemScreen(this.activity);
    }
}
