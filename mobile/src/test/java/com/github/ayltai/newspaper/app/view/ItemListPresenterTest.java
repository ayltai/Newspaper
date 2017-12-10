package com.github.ayltai.newspaper.app.view;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.VerticalListPresenter;
import com.github.ayltai.newspaper.view.PresenterTest;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.realm.RealmList;

public final class ItemListPresenterTest extends PresenterTest<MainActivity, ItemListPresenter, VerticalListPresenter.View<Item>> {
    private final FlowableProcessor<List<Item>> load                           = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> clears                         = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> pullToRefreshes                = PublishProcessor.create();
    private final FlowableProcessor<Irrelevant> bestVisibleItemPositionChanges = PublishProcessor.create();

    @NonNull
    @Override
    protected ItemListPresenter createPresenter() {
        final ItemListPresenter presenter = Mockito.spy(new ItemListPresenter(Arrays.asList("港聞", "國際")));

        Mockito.doReturn(this.load).when(presenter).load();

        return presenter;
    }

    @NonNull
    @Override
    protected VerticalListPresenter.View<Item> createView() {
        final VerticalListPresenter.View<Item> view = Mockito.mock(VerticalListPresenter.View.class);

        Mockito.doReturn(this.clears).when(view).clears();
        Mockito.doReturn(this.pullToRefreshes).when(view).pullToRefreshes();
        Mockito.doReturn(this.bestVisibleItemPositionChanges).when(view).bestVisibleItemPositionChanges();

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
        final List<Item> models = this.getModels();
        this.getPresenter().bindModel(models);

        // When
        this.attachments.onNext(true);

        // Then
        Mockito.verify(this.getView(), Mockito.times(1)).showLoadingView();

        this.load.onNext(models);

        Mockito.verify(this.getView(), Mockito.times(1)).clear();
        Mockito.verify(this.getView(), Mockito.times(1)).bind(models);
    }

    @Test
    public void Given_model_When_pullToRefresh_Then_viewIsRefreshed() {
        // Given
        final List<Item> models = this.getModels();
        this.getPresenter().bindModel(models);

        // When
        this.attachments.onNext(true);
        this.pullToRefreshes.onNext(Irrelevant.INSTANCE);

        // Then
        Mockito.verify(this.getPresenter(), Mockito.times(1)).onPullToRefresh();
        Mockito.verify(this.getPresenter(), Mockito.times(1)).resetState();

        this.load.onNext(models);

        Mockito.verify(this.getView(), Mockito.times(3)).clear();
        Mockito.verify(this.getView(), Mockito.times(2)).bind(models);
    }

    @NonNull
    private List<Item> getModels() {
        return Arrays.asList(this.getModel(), this.getModel());
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
