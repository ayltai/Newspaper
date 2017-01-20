package com.github.ayltai.newspaper.list;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.PresenterTest;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

import io.realm.RealmList;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class ListPresenterTest extends PresenterTest<ListPresenter, ListPresenter.View> {
    //region Constants

    private static final String KEY_PARENT_URL = Constants.SOURCE_BOOKMARK;

    private static final ListScreen.Key KEY = new ListScreen.Key(ListPresenterTest.KEY_PARENT_URL);

    //endregion

    //region Events

    private final PublishSubject<Void> refreshes = PublishSubject.create();

    //endregion

    private final Feed feed = new Feed(ListPresenterTest.KEY_PARENT_URL, new RealmList<>());

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @NonNull
    @Override
    protected ListPresenter createPresenter() {
        final FeedManager feedManager = Mockito.mock(FeedManager.class);
        Mockito.when(feedManager.getFeed(ListPresenterTest.KEY_PARENT_URL)).thenReturn(Observable.just(this.feed));

        final ListPresenter presenter = Mockito.spy(new ListPresenter());
        Mockito.when(presenter.getFeedManager()).thenReturn(feedManager);

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

        Mockito.when(view.refreshes()).thenReturn(this.refreshes);

        return view;
    }

    //region Tests

    @Test
    public void testViewBinding() throws Exception {
        this.bind();

        Mockito.verify(this.getView(), Mockito.times(1)).setItems(ListPresenterTest.KEY, this.feed);
    }

    @Test
    public void testWhenRefreshedThenSetItems() throws Exception {
        this.bind();

        this.refreshes.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(2)).setItems(ListPresenterTest.KEY, this.feed);
    }

    //endregion

    private void bind() {
        this.getPresenter().bind(null, ListPresenterTest.KEY);
    }
}
