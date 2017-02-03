package com.github.ayltai.newspaper.main;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.Source;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter extends Presenter<MainPresenter.View> {
    public interface View extends Presenter.View, Closeable {
        void bind(@NonNull MainAdapter adapter);

        void updateHeaderTitle(@NonNull CharSequence title);

        void updateHeaderImages(@Nullable List<String> images);

        boolean goBack();

        @NonNull
        Observable<Integer> pageChanges();
    }

    private final Subscriber<ImagesUpdatedEvent> subscriber = new Subscriber<ImagesUpdatedEvent>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable e) {
            MainPresenter.this.log().w(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        @Override
        public void onNext(final ImagesUpdatedEvent imagesUpdatedEvent) {
            MainPresenter.this.images.put(imagesUpdatedEvent.getUrl(), imagesUpdatedEvent.getImages());

            if (MainPresenter.this.lastUpdatedPosition != MainPresenter.this.currentPosition) {
                MainPresenter.this.lastUpdatedPosition = MainPresenter.this.currentPosition;

                MainPresenter.this.updateHeader();
            }
        }
    };

    private final Map<String, List<String>> images = new HashMap<>();

    //region Variables

    private CompositeSubscription subscriptions;
    private MainAdapter           adapter;
    private int                   currentPosition;
    private int                   lastUpdatedPosition = -1;

    //endregion

    public final void bind() {
        this.view.bind(this.adapter);
    }

    @Override
    public final void onViewAttached(@NonNull final MainPresenter.View view) {
        super.onViewAttached(view);

        this.adapter = this.createMainAdapter();

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) this.subscriptions.unsubscribe();

        this.subscriptions = new CompositeSubscription();

        this.subscriptions.add(this.view.pageChanges().subscribe(position -> {
            this.currentPosition = position;

            this.updateHeader();
        }));

        this.bus().register(ImagesUpdatedEvent.class, this.subscriber);
    }

    @Override
    public final void onViewDetached() {
        super.onViewDetached();

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        this.bus().unregister(ImagesUpdatedEvent.class, this.subscriber);
    }

    private void updateHeader() {
        if (this.adapter.getCount() > 0) {
            final Source source = this.adapter.getSource(this.currentPosition);

            if (source != null) {
                this.view.updateHeaderTitle(this.adapter.getPageTitle(this.currentPosition));
                this.view.updateHeaderImages(this.images.get(source.getUrl()));
            }
        }
    }

    @VisibleForTesting
    @NonNull
    MainAdapter createMainAdapter() {
        return DaggerMainComponent.builder().mainModule(new MainModule((Activity)this.view.getContext())).build().mainAdapter();
    }
}
