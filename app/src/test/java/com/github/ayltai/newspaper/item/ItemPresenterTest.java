package com.github.ayltai.newspaper.item;

import java.util.Date;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.PresenterTest;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.SuppressFBWarnings;

import io.realm.RealmList;
import rx.Observable;
import rx.subjects.PublishSubject;

public final class ItemPresenterTest extends PresenterTest<ItemPresenter, ItemPresenter.View> {
    //region Constants

    private static final String                 KEY_PARENT_URL    = Constants.CATEGORY_BOOKMARK;
    private static final String                 ITEM_TITLE        = "title";
    private static final String                 ITEM_DESCRIPTION  = "description";
    private static final String                 ITEM_SOURCE       = "source";
    private static final String                 ITEM_LINK         = "link";
    private static final RealmList<Image>       ITEM_MEDIA_URLS   = new RealmList<>(new Image("media url"));
    private static final Date                   ITEM_PUBLISH_DATE = new Date();

    private static final ListScreen.Key KEY_PARENT = new ListScreen.Key(ItemPresenterTest.KEY_PARENT_URL);

    //endregion

    //region Events

    private final PublishSubject<Void>    clicks    = PublishSubject.create();
    private final PublishSubject<Integer> zooms     = PublishSubject.create();
    private final PublishSubject<Boolean> bookmarks = PublishSubject.create();
    private final PublishSubject<Void>    shares    = PublishSubject.create();

    //endregion

    //region Mocks

    @Mock private Item item;

    //endregion

    private final RealmList<Item> items = new RealmList<>();

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @NonNull
    @Override
    protected ItemPresenter createPresenter() {
        this.items.add(this.item);

        final ItemManager feedManager = Mockito.mock(ItemManager.class);
        Mockito.doReturn(Observable.just(this.items)).when(feedManager).getItemsObservable(null, new String[] { ItemPresenterTest.KEY_PARENT_URL });

        final ItemPresenter presenter = Mockito.spy(new ItemPresenter(null));
        Mockito.doReturn(feedManager).when(presenter).getItemManager();
        Mockito.doNothing().when(presenter).update(ArgumentMatchers.any(Item.class));

        final RxBus bus = Mockito.mock(RxBus.class);
        Mockito.doReturn(bus).when(presenter).bus();

        final LogUtils logUtils = Mockito.mock(LogUtils.class);
        Mockito.doReturn(logUtils).when(presenter).log();

        return presenter;
    }

    @NonNull
    @Override
    protected ItemPresenter.View createView() {
        final ItemPresenter.View view = Mockito.mock(ItemPresenter.View.class);

        Mockito.doReturn(this.clicks).when(view).clicks();
        Mockito.doReturn(this.zooms).when(view).zooms();
        Mockito.doReturn(this.bookmarks).when(view).bookmarks();
        Mockito.doReturn(this.shares).when(view).shares();

        return view;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn(ItemPresenterTest.ITEM_TITLE).when(this.item).getTitle();
        Mockito.doReturn(ItemPresenterTest.ITEM_DESCRIPTION).when(this.item).getDescription();
        Mockito.doReturn(ItemPresenterTest.ITEM_SOURCE).when(this.item).getSource();
        Mockito.doReturn(ItemPresenterTest.ITEM_LINK).when(this.item).getLink();
        Mockito.doReturn(ItemPresenterTest.ITEM_MEDIA_URLS).when(this.item).getImages();
        Mockito.doReturn(ItemPresenterTest.ITEM_PUBLISH_DATE).when(this.item).getPublishDate();
    }

    //region Tests

    @Test
    public void testViewBinding() throws Exception {
        this.bind();

        Mockito.verify(this.getView(), Mockito.times(1)).setTitle(this.item.getTitle());
        Mockito.verify(this.getView(), Mockito.times(1)).setDescription(this.item.getDescription());
        Mockito.verify(this.getView(), Mockito.times(1)).setSource(this.item.getSource());
        Mockito.verify(this.getView(), Mockito.times(1)).setLink(this.item.getLink());
        Mockito.verify(this.getView(), Mockito.times(1)).setThumbnail(this.item.getImages().first().getUrl(), Constants.LIST_VIEW_TYPE_DEFAULT);
        Mockito.verify(this.getView(), Mockito.times(1)).setThumbnails(this.item.getImages());
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

        this.zooms.onNext(0);

        Mockito.verify(this.getView(), Mockito.times(1)).showMedia(this.item.getImages().first().getUrl());
    }

    @Test
    public void testWhenBookmarkedThenUpdateItem() throws Exception {
        this.bind();

        this.bookmarks.onNext(Boolean.TRUE);

        Mockito.verify(this.getPresenter(), Mockito.times(1)).update(true);
    }

    @Test
    public void testWhenSharedThenShare() throws Exception {
        this.bind();

        this.shares.onNext(null);

        Mockito.verify(this.getView(), Mockito.times(1)).share(this.item.getLink());
    }

    //endregion

    private void bind() {
        this.getPresenter().bind(ItemPresenterTest.KEY_PARENT, this.item, Constants.LIST_VIEW_TYPE_DEFAULT, false);
    }
}
