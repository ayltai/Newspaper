package com.github.ayltai.newspaper.main;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.ayltai.newspaper.DaggerMainComponent;
import com.github.ayltai.newspaper.MainModule;
import com.github.ayltai.newspaper.Presenter;
import com.github.ayltai.newspaper.data.ItemManager;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.setting.Settings;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

public /* final */ class MainPresenter extends Presenter<MainPresenter.View> {
    public interface View extends Presenter.View, Closeable {
        void bind(@NonNull MainAdapter adapter);

        void updateHeaderTitle(@NonNull CharSequence title);

        void updateHeaderImages(@Nullable List<String> images);

        void enablePrevious(boolean enabled);

        void enableNext(boolean enabled);

        Flowable<Void> previousClicks();

        Flowable<Void> nextClicks();

        void navigatePrevious();

        void navigateNext();

        boolean goBack();

        @NonNull
        Flowable<Integer> pageChanges();
    }

    private final Subscriber<ImagesUpdatedEvent> subscriber = new Subscriber<ImagesUpdatedEvent>() {
        @Override
        public void onComplete() {
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
        }

        @Override
        public void onError(final Throwable e) {
            MainPresenter.this.log().w(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        @Override
        public void onNext(final ImagesUpdatedEvent imagesUpdatedEvent) {
            MainPresenter.this.updateHeader();
        }
    };

    //region Variables

    private CompositeDisposable disposable;
    private MainAdapter         adapter;
    private int                 currentPosition;

    //endregion

    public final void bind() {
        this.getView().bind(this.adapter);
    }

    @Override
    public final void onViewAttached(@NonNull final MainPresenter.View view) {
        super.onViewAttached(view);

        this.adapter = this.createMainAdapter();

        if (this.disposable != null && !this.disposable.isDisposed() && this.disposable.size() > 0) this.disposable.dispose();

        this.disposable = new CompositeDisposable();

        this.disposable.add(this.getView().pageChanges().subscribe(position -> {
            this.currentPosition = position;

            this.updateHeader();

            this.getView().enablePrevious(this.currentPosition > 0);
            this.getView().enableNext(this.currentPosition < this.adapter.getCount() - 1);
        }, error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.disposable.add(this.getView().previousClicks().subscribe(dummy -> this.getView().navigatePrevious(), error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));
        this.disposable.add(this.getView().nextClicks().subscribe(dummy -> this.getView().navigateNext(), error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.bus().register(ImagesUpdatedEvent.class, this.subscriber);
    }

    @Override
    public final void onViewDetached() {
        super.onViewDetached();

        if (this.disposable != null && !this.disposable.isDisposed() && this.disposable.size() > 0) {
            this.disposable.dispose();
            this.disposable = null;
        }

        this.bus().unregister(ImagesUpdatedEvent.class, this.subscriber);
    }

    private void updateHeader() {
        if (this.getView() == null) return;

        if (this.adapter.getCount() > 0) {
            final CharSequence category = this.adapter.getPageTitle(this.currentPosition);

            this.getView().updateHeaderTitle(category);

            this.getHeaderImages(category)
                .subscribe(
                    images -> this.getView().updateHeaderImages(images),
                    error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)
                );
        }
    }

    @VisibleForTesting
    @NonNull
    /* private final */ MainAdapter createMainAdapter() {
        return DaggerMainComponent.builder().mainModule(new MainModule((Activity)this.getView().getContext())).build().mainAdapter();
    }

    @VisibleForTesting
    @NonNull
    /* private */ Single<List<String>> getHeaderImages(@NonNull final CharSequence category) {
        return Single.create(emitter -> {
            final List<String> images = new ArrayList<>();
            final Realm        realm  = Realm.getDefaultInstance();

            try {
                new ItemManager(realm).getItemsObservable(new ArrayList<>(Settings.getSources(this.getView().getContext())), Collections.singletonList(category.toString()))
                    .subscribe(
                        items -> {
                            for (final Item item : items) {
                                for (final Image image : item.getImages()) images.add(image.getUrl());
                            }

                            emitter.onSuccess(images);
                        },
                        error -> this.log().e(this.getClass().getSimpleName(), error.getMessage(), error)
                    );
            } finally {
                realm.close();
            }
        });
    }
}
