package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.PresenterTest;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import java.util.Date;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.realm.RealmList;

public final class DetailsPresenterTest extends PresenterTest<MainActivity, DetailsPresenter, DetailsPresenter.View> {
    private final FlowableProcessor<Irrelevant> clicks             = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> avatarClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> sourceClicks       = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> publishDateClicks  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> titleClicks        = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> descriptionClicks  = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> linkClicks         = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> textToSpeechClicks = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bookmarkClicks     = PublishProcessor.create();
    private final FlowableProcessor<Image>      imageClicks        = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> videoClicks        = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> viewOnWebClicks    = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> shareClicks        = PublishProcessor.create();

    @NonNull
    @Override
    protected DetailsPresenter createPresenter() {
        return new DetailsPresenter();
    }

    @NonNull
    @Override
    protected DetailsPresenter.View createView() {
        final DetailsPresenter.View view = Mockito.mock(DetailsPresenter.View.class);

        Mockito.doReturn(this.clicks).when(view).clicks();
        Mockito.doReturn(this.avatarClicks).when(view).avatarClicks();
        Mockito.doReturn(this.sourceClicks).when(view).sourceClicks();
        Mockito.doReturn(this.publishDateClicks).when(view).publishDateClicks();
        Mockito.doReturn(this.titleClicks).when(view).titleClicks();
        Mockito.doReturn(this.descriptionClicks).when(view).descriptionClicks();
        Mockito.doReturn(this.linkClicks).when(view).linkClicks();
        Mockito.doReturn(this.textToSpeechClicks).when(view).textToSpeechClicks();
        Mockito.doReturn(this.bookmarkClicks).when(view).bookmarkClicks();
        Mockito.doReturn(this.imageClicks).when(view).imageClicks();
        Mockito.doReturn(this.videoClicks).when(view).videoClicks();
        Mockito.doReturn(this.viewOnWebClicks).when(view).viewOnWebClicks();
        Mockito.doReturn(this.shareClicks).when(view).shareClicks();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void Given_modelIsFullDescription_When_modelIsBound_Then_bindModelIsCalled() {
        final Item model = this.getModel();
        Mockito.doReturn(true).when(model).isFullDescription();

        // Given
        this.getPresenter().setModel(model);

        // When
        this.attaches.onNext(true);

        Mockito.verify(this.getView(), Mockito.times(1)).setDescription(Mockito.anyString());
    }

    @Test
    public void Given_onViewAttached_When_textToSpeechClicks_Then_textToSpeechIsCalled() {
        // Given
        this.getPresenter().setModel(this.getModel());

        // When
        this.attaches.onNext(true);
        this.textToSpeechClicks.onNext(Irrelevant.INSTANCE);

        Mockito.verify(this.getView(), Mockito.times(1)).textToSpeech();
    }

    @Test
    public void Given_onViewAttached_When_viewOnWebClicks_Then_viewOnWebIsCalled() {
        // Given
        this.getPresenter().setModel(this.getModel());

        // When
        this.attaches.onNext(true);
        this.viewOnWebClicks.onNext(Irrelevant.INSTANCE);

        Mockito.verify(this.getView(), Mockito.times(1)).viewOnWeb("link");
    }

    @Test
    public void Given_onViewAttached_When_shareClicks_Then_shareIsCalled() {
        // Given
        this.getPresenter().setModel(this.getModel());

        // When
        this.attaches.onNext(true);
        this.shareClicks.onNext(Irrelevant.INSTANCE);

        Mockito.verify(this.getView(), Mockito.times(1)).share("link");
    }

    @Test
    public void Given_onViewAttached_When_imageClicks_Then_showImage() {
        // Given
        this.getPresenter().setModel(this.getModel());

        // When
        this.attaches.onNext(true);
        this.imageClicks.onNext(new Image(""));

        Mockito.verify(this.getView(), Mockito.times(1)).showImage(Mockito.anyString());
    }

    @NonNull
    private Item getModel() {
        final Item model = Mockito.mock(Item.class);

        Mockito.doReturn("蘋果日報").when(model).getSource();
        Mockito.doReturn(new Date()).when(model).getPublishDate();
        Mockito.doReturn("title").when(model).getTitle();
        Mockito.doReturn("description").when(model).getDescription();
        Mockito.doReturn("link").when(model).getLink();
        Mockito.doReturn(new RealmList<>()).when(model).getImages();
        Mockito.doReturn(new Video()).when(model).getVideo();

        return model;
    }
}
