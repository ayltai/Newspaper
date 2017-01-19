package com.github.ayltai.newspaper.item;

import java.util.Date;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.PresenterTest;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.data.FeedManager;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

import io.realm.RealmList;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class ItemPresenterTest extends PresenterTest<ItemPresenter, ItemPresenter.View> {
    //region Constants

    private static final String KEY_PARENT_URL    = Constants.SOURCE_BOOKMARK;
    private static final String ITEM_TITLE        = "title";
    private static final String ITEM_DESCRIPTION  = "description";
    private static final String ITEM_SOURCE       = "source";
    private static final String ITEM_LINK         = "link";
    private static final String ITEM_MEDIA_URL    = "media url";
    private static final Date   ITEM_PUBLISH_DATE = new Date();

    private static final ListScreen.Key KEY_PARENT = new ListScreen.Key(ItemPresenterTest.KEY_PARENT_URL);

    //endregion

    //region Events

    private final PublishSubject<Void>    clicks    = PublishSubject.create();
    private final PublishSubject<Void>    zooms     = PublishSubject.create();
    private final PublishSubject<Boolean> bookmarks = PublishSubject.create();
    private final PublishSubject<Void>    shares    = PublishSubject.create();

    //endregion

    //region Mocks

    @Mock private Item item;

    //endregion

    private final Feed feed = new Feed(ItemPresenterTest.KEY_PARENT_URL, new RealmList<>());

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @NonNull
    @Override
    protected ItemPresenter createPresenter() {
        final FeedManager feedManager = Mockito.mock(FeedManager.class);
        Mockito.when(feedManager.getFeed(ItemPresenterTest.KEY_PARENT_URL)).thenReturn(Observable.just(this.feed));

        final ItemPresenter presenter = Mockito.spy(new ItemPresenter(null));
        Mockito.doReturn(feedManager).when(presenter).getFeedManager();
        Mockito.doNothing().when(presenter).updateFeed(Mockito.any(Feed.class), Mockito.anyBoolean());

        final LogUtils logUtils = Mockito.mock(LogUtils.class);
        Mockito.doReturn(logUtils).when(presenter).log();

        return presenter;
    }

    @NonNull
    @Override
    protected ItemPresenter.View createView() {
        final ItemPresenter.View view = Mockito.mock(ItemPresenter.View.class);

        Mockito.when(view.clicks()).thenReturn(this.clicks);
        Mockito.when(view.zooms()).thenReturn(this.zooms);
        Mockito.when(view.bookmarks()).thenReturn(this.bookmarks);
        Mockito.when(view.shares()).thenReturn(this.shares);

        return view;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Mockito.when(this.item.getTitle()).thenReturn(ItemPresenterTest.ITEM_TITLE);
        Mockito.when(this.item.getDescription()).thenReturn(ItemPresenterTest.ITEM_DESCRIPTION);
        Mockito.when(this.item.getSource()).thenReturn(ItemPresenterTest.ITEM_SOURCE);
        Mockito.when(this.item.getLink()).thenReturn(ItemPresenterTest.ITEM_LINK);
        Mockito.when(this.item.getMediaUrl()).thenReturn(ItemPresenterTest.ITEM_MEDIA_URL);
        Mockito.when(this.item.getPublishDate()).thenReturn(ItemPresenterTest.ITEM_PUBLISH_DATE);
    }

    //region Tests

    @Test
    public void testViewBinding() throws Exception {
        this.bind();

        Mockito.verify(this.getView(), Mockito.times(1)).setTitle(this.item.getTitle());
        Mockito.verify(this.getView(), Mockito.times(1)).setDescription(this.item.getDescription());
        Mockito.verify(this.getView(), Mockito.times(1)).setSource(this.item.getSource());
        Mockito.verify(this.getView(), Mockito.times(1)).setLink(this.item.getLink());
        Mockito.verify(this.getView(), Mockito.times(1)).setThumbnail(this.item.getMediaUrl(), Constants.LIST_VIEW_TYPE_DEFAULT);
        Mockito.verify(this.getView(), Mockito.times(1)).setIsBookmarked(false);
        Mockito.verify(this.getView(), Mockito.times(1)).setPublishDate(this.item.getPublishDate().getTime());
    }

    @Test
    public void testWhenClickedThenShowItem() throws Exception {
        this.bind();

        this.clicks.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(1)).showItem(ItemPresenterTest.KEY_PARENT, this.item);
    }

    @Test
    public void testWhenZoomedThenShowOriginalMedia() throws Exception {
        this.bind();

        this.zooms.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(1)).showOriginalMedia(this.item.getMediaUrl());
    }

    @Test
    public void testWhenBookmarkedThenUpdateFeed() throws Exception {
        this.bind();

        this.bookmarks.onNext(Boolean.TRUE);

        Mockito.verify(this.getPresenter(), Mockito.times(1)).updateFeed(this.feed, true);
    }

    @Test
    public void testWhenSharedThenShare() throws Exception {
        this.bind();

        this.shares.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(1)).share(this.item.getLink());
    }

    //endregion

    private void bind() {
        this.getPresenter().bind(ItemPresenterTest.KEY_PARENT, this.item, Constants.LIST_VIEW_TYPE_DEFAULT);
    }
}
