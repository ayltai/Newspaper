package com.github.ayltai.newspaper.list;

import java.util.Collections;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.PresenterTest;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

import io.realm.RealmList;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class ListPresenterTest extends PresenterTest<ListPresenter, ListPresenter.View> {
    //region Constants

    private static final String KEY_PARENT_URL = Constants.CATEGORY_BOOKMARK;

    private static final ListScreen.Key KEY = new ListScreen.Key(ListPresenterTest.KEY_PARENT_URL);

    //endregion

    //region Events

    private final PublishSubject<Void> refreshes = PublishSubject.create();

    //endregion

    private final RealmList<Item> items = new RealmList<>();

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @NonNull
    @Override
    protected ListPresenter createPresenter() {
        final ItemManager itemManager = Mockito.mock(ItemManager.class);
        Mockito.doReturn(Observable.just(this.items)).when(itemManager).getItemsObservable(Collections.emptyList(), Collections.singletonList(ListPresenterTest.KEY_PARENT_URL));

        final ListPresenter presenter = Mockito.spy(new ListPresenter());
        Mockito.doReturn(itemManager).when(presenter).getItemManager();

        final RxBus bus = Mockito.mock(RxBus.class);
        Mockito.doReturn(bus).when(presenter).bus();

        final LogUtils logUtils = Mockito.mock(LogUtils.class);
        Mockito.doReturn(logUtils).when(presenter).log();

        return presenter;
    }

    @NonNull
    @Override
    protected ListPresenter.View createView() {
        final ListPresenter.View view = Mockito.mock(ListPresenter.View.class);
        Mockito.doReturn(this.refreshes).when(view).refreshes();
        return view;
    }

    //region Tests

    @Test
    public void testViewBinding() throws Exception {
        this.bind();

        Mockito.verify(this.getView(), Mockito.times(1)).setItems(ListPresenterTest.KEY, this.items);
    }

    @Test
    public void testWhenRefreshedThenSetItems() throws Exception {
        this.bind();

        this.refreshes.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(2)).setItems(ListPresenterTest.KEY, this.items);
    }

    //endregion

    private void bind() {
        this.getPresenter().bind(null, ListPresenterTest.KEY);
    }
}
