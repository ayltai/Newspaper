package com.github.ayltai.newspaper.app.view;

import android.support.annotation.NonNull;

import com.github.ayltai.architecture.test.PresenterTest;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.MainActivity;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class CategoriesPresenterTest extends PresenterTest<MainActivity, CategoriesPresenter, CategoriesPresenter.View> {
    private final FlowableProcessor<Integer> optionsChanges = PublishProcessor.create();

    @NonNull
    @Override
    protected CategoriesPresenter createPresenter() {
        return new CategoriesPresenter();
    }

    @NonNull
    @Override
    protected CategoriesPresenter.View createView() {
        final CategoriesPresenter.View view = Mockito.mock(CategoriesPresenter.View.class);

        Mockito.doReturn(this.optionsChanges).when(view).optionsChanges();

        return view;
    }

    @NonNull
    @Override
    protected MainActivity createActivity() {
        return Robolectric.buildActivity(MainActivity.class).get();
    }

    @Test
    public void When_onViewAttached_Then_addOptionIsCalled() {
        // When
        this.attachments.onNext(true);

        // Then
        Mockito.verify(this.getView(), Mockito.times(9)).addOption(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void When_optionsChanges_Then_setCategoriesIsCalled() {
        // When
        this.attachments.onNext(true);
        this.optionsChanges.onNext(0);

        // Then
        final List<String> categories = ComponentFactory.getInstance()
            .getConfigComponent(this.getView().getActivity())
            .userConfig().getCategories();

        Assert.assertEquals(16, categories.size());
        Assert.assertEquals("國際", categories.get(0));
    }
}
