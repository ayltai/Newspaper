package com.github.ayltai.newspaper.app.view;

import java.util.Date;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.PresenterTest;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.realm.RealmList;

public final class ItemPresenterTest extends PresenterTest<MainActivity, ItemPresenter<ItemPresenter.View>, ItemPresenter.View> {
    private final FlowableProcessor<Irrelevant> clicks            = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> avatarClicks      = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> sourceClicks      = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> publishDateClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> titleClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> descriptionClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> linkClicks        = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bookmarkClicks    = PublishProcessor.create();
    private final FlowableProcessor<Image>      imageClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> videoClicks       = PublishProcessor.create();

    @NonNull
    @Override
    protected ItemPresenter<ItemPresenter.View> createPresenter() {
        return new ItemPresenter<>();
    }

    @NonNull
    @Override
    protected ItemPresenter.View createView() {
        final ItemPresenter.View view = Mockito.mock(ItemPresenter.View.class);

        Mockito.doReturn(this.clicks).when(view).clicks();
        Mockito.doReturn(this.avatarClicks).when(view).avatarClicks();
        Mockito.doReturn(this.sourceClicks).when(view).sourceClicks();
        Mockito.doReturn(this.publishDateClicks).when(view).publishDateClicks();
        Mockito.doReturn(this.titleClicks).when(view).titleClicks();
        Mockito.doReturn(this.descriptionClicks).when(view).descriptionClicks();
        Mockito.doReturn(this.linkClicks).when(view).linkClicks();
        Mockito.doReturn(this.bookmarkClicks).when(view).bookmarkClicks();
        Mockito.doReturn(this.imageClicks).when(view).imageClicks();
        Mockito.doReturn(this.videoClicks).when(view).videoClicks();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void Given_model_When_onViewAttached_Then_modelIsBound() {
        // Given
        this.getPresenter().bindModel(this.getModel());

        // When
        this.attachments.onNext(true);

        Mockito.verify(this.getView(), Mockito.times(1)).setAvatar(Mockito.anyInt());
        Mockito.verify(this.getView(), Mockito.times(1)).setSource(Mockito.anyString());
        Mockito.verify(this.getView(), Mockito.times(1)).setPublishDate(Mockito.any(Date.class));
        Mockito.verify(this.getView(), Mockito.times(1)).setTitle(Mockito.anyString());
        Mockito.verify(this.getView(), Mockito.times(1)).setDescription(Mockito.anyString());
        Mockito.verify(this.getView(), Mockito.times(1)).setLink(Mockito.anyString());
        Mockito.verify(this.getView(), Mockito.times(1)).setIsBookmarked(Mockito.anyBoolean());
        Mockito.verify(this.getView(), Mockito.times(1)).setImages(Mockito.anyList());
        Mockito.verify(this.getView(), Mockito.times(1)).setVideo(Mockito.any(Video.class));
    }

    @Test
    public void Given_onViewAttached_When_clicks_Then_onClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.clicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).clicks();
    }

    @Test
    public void Given_onViewAttached_When_avatarClicks_Then_onAvatarClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.avatarClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).avatarClicks();
    }

    @Test
    public void Given_onViewAttached_When_sourceClicks_Then_onSourceClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.sourceClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).sourceClicks();
    }

    @Test
    public void Given_onViewAttached_When_publishDateClicks_Then_onPublishDateClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.publishDateClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).publishDateClicks();
    }

    @Test
    public void Given_onViewAttached_When_titleClicks_Then_onTitleClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.titleClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).titleClicks();
    }

    @Test
    public void Given_onViewAttached_When_descriptionClicks_Then_onDescriptionClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.descriptionClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).descriptionClicks();
    }

    @Test
    public void Given_onViewAttached_When_linkClicks_Then_onLinkClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.linkClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).linkClicks();
    }

    @Test
    public void Given_onViewAttached_When_bookmarkClicks_Then_onBookmarkClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.bookmarkClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).bookmarkClicks();
    }

    @Test
    public void Given_onViewAttached_When_imageClicks_Then_onImageClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.imageClicks.onNext(new Image());

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).imageClicks();
    }

    @Test
    public void Given_onViewAttached_When_videoClicks_Then_onVideoClickIsCalled() {
        // Given
        this.attachments.onNext(true);

        // When
        this.videoClicks.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).videoClicks();
    }

    @NonNull
    private Item getModel() {
        final Item model = Mockito.mock(Item.class);

        Mockito.doReturn("蘋果日報").when(model).getSource();
        Mockito.doReturn(new Date()).when(model).getPublishDate();
        Mockito.doReturn("").when(model).getTitle();
        Mockito.doReturn("").when(model).getDescription();
        Mockito.doReturn("").when(model).getLink();
        Mockito.doReturn(new RealmList<>()).when(model).getImages();
        Mockito.doReturn(new Video()).when(model).getVideo();

        return model;
    }
}
