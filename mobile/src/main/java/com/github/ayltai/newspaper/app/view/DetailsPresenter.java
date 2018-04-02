package com.github.ayltai.newspaper.app.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.analytics.ShareEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.data.ItemManager;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.data.model.NewsItem;
import com.github.ayltai.newspaper.client.Client;
import com.github.ayltai.newspaper.client.ClientFactory;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.language.LanguageService;
import com.github.ayltai.newspaper.net.NetworkUtils;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Topic;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class DetailsPresenter extends ItemPresenter<DetailsPresenter.View> {
    public interface View extends ItemPresenter.View {
        @Nullable
        Flowable<Irrelevant> textToSpeechClicks();

        @Nullable
        Flowable<Irrelevant> viewOnWebClicks();

        @Nullable
        Flowable<Irrelevant> shareClicks();

        void showProgress(boolean show);

        void textToSpeech();

        void viewOnWeb(@NonNull String url);

        void share(@NonNull String url);

        void showImage(@NonNull String url);
    }

    @UiThread
    @Override
    public void bindModel(final Item model) {
        if (this.getView() == null) {
            super.bindModel(model);

            this.analyze(model);
        } else {
            if (model instanceof NewsItem) {
                super.bindModel(model);

                final NewsItem newsItem = (NewsItem)model;
                this.updateItem(newsItem);

                if (newsItem.isFullDescription()) {
                    this.analyze(model);
                } else {
                    this.getView().showProgress(true);

                    super.bindModel(model);

                    this.updateItem(newsItem);

                    if (NetworkUtils.isOnline(this.getView().getContext())) {
                        this.manageDisposable(Single.<NewsItem>create(
                            emitter -> {
                                final Client client = ClientFactory.getInstance(this.getView().getContext()).getClient(model.getSource());

                                if (emitter == null || emitter.isDisposed()) return;

                                if (client == null) {
                                    emitter.onError(new IllegalArgumentException("Unrecognized source " + model.getSource()));
                                } else {
                                    client.updateItem((NewsItem)model)
                                        .subscribe(
                                            emitter::onSuccess,
                                            emitter::onError
                                        );
                                }
                            })
                            .compose(RxUtils.applySingleBackgroundSchedulers())
                            .flatMap(item -> DetailsPresenter.updateItem(this.getView().getContext(), item)).compose(RxUtils.applySingleBackgroundToMainSchedulers())
                            .subscribe(
                                items -> {
                                    super.bindModel(items.get(0));

                                    this.analyze(model);

                                    this.getView().showProgress(false);
                                },
                                error -> {
                                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                                }));
                    }
                }
            } else {
                super.bindModel(model);
            }
        }
    }

    private void onTextToSpeechClick() {
        if (this.getView() != null) {
            this.getView().textToSpeech();

            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getView().getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("TTS"));
        }
    }

    @CallSuper
    @Override
    protected void onBookmarkClick() {
        if (this.getView() != null && this.getModel() instanceof NewsItem) {
            final NewsItem item = (NewsItem)this.getModel();
            item.setBookmarked(!this.getModel().isBookmarked());

            this.manageDisposable(DetailsPresenter.updateItem(this.getView().getContext(), item)
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
                .subscribe(
                    items -> {
                    },
                    error -> {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                    }
                ));
        }

        if (this.getView() != null) this.getView().setIsBookmarked(this.getModel().isBookmarked());

        super.onBookmarkClick();
    }

    private void onViewOnWebClick() {
        if (this.getView() != null) {
            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getView().getContext())
                .eventLogger()
                .logEvent(new ShareEvent()
                    .setSource(this.getModel().getSource())
                    .setCategory(this.getModel().getCategory()));

            this.getView().viewOnWeb(this.getModel().getLink());
        }
    }

    private void onShareClick() {
        if (this.getView() != null) {
            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getView().getContext())
                .eventLogger()
                .logEvent(new ShareEvent()
                    .setSource(this.getModel().getSource())
                    .setCategory(this.getModel().getCategory()));

            this.getView().share(this.getModel().getLink());
        }
    }

    @CallSuper
    @Override
    protected void onImageClick(@NonNull final Image image) {
        if (this.getView() != null) this.getView().showImage(image.getUrl());

        super.onImageClick(image);
    }

    @CallSuper
    @Override
    protected void onEntityClick(@NonNull final String wikiLink) {
        if (this.getView() != null) this.getView().viewOnWeb(wikiLink);

        super.onEntityClick(wikiLink);
    }

    @CallSuper
    @Override
    public void onViewAttached(@NonNull final DetailsPresenter.View view, final boolean isFirstTimeAttachment) {
        final Flowable<Irrelevant> textToSpeechClicks = view.textToSpeechClicks();
        if (textToSpeechClicks != null) this.manageDisposable(textToSpeechClicks.subscribe(irrelevant -> this.onTextToSpeechClick()));

        final Flowable<Irrelevant> viewOnWebClicks = view.viewOnWebClicks();
        if (viewOnWebClicks != null) this.manageDisposable(viewOnWebClicks.subscribe(irrelevant -> this.onViewOnWebClick()));

        final Flowable<Irrelevant> shareClicks = view.shareClicks();
        if (shareClicks != null) this.manageDisposable(shareClicks.subscribe(irrelevant -> this.onShareClick()));

        super.onViewAttached(view, isFirstTimeAttachment);
    }

    private void updateItem(@NonNull final NewsItem item) {
        this.manageDisposable(DetailsPresenter.updateItem(this.getView().getContext(), item)
            .compose(RxUtils.applySingleBackgroundToMainSchedulers())
            .subscribe(
                items -> { },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }));
    }

    private static Single<List<NewsItem>> updateItem(@NonNull final Context context, @NonNull final NewsItem item) {
        item.setLastAccessedDate(new Date());

        return ItemManager.create(context)
            .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER))
            .flatMap(manager -> manager.putItems(Collections.singletonList(item))
                .compose(RxUtils.applySingleSchedulers(DataManager.SCHEDULER)));
    }

    private void analyze(@NonNull final Item model) {
        if (this.getView() != null && model.getDescription() != null) Single.defer(() -> Single.just(ComponentFactory.getInstance()
            .getLanguageComponent()
            .languageService()
            .analyze(model.getDescription())))
            .compose(RxUtils.applySingleBackgroundToMainSchedulers())
            .subscribe(
                response -> {
                    if (this.getView() != null) {
                        final List<Topic> topics = new ArrayList<>(response.getTopics());
                        Collections.sort(topics, LanguageService.TOPIC_COMPARATOR);

                        int count = 0;

                        for (final Topic topic : topics) {
                            if ((int)(topic.getScore() * Constants.PERCENT) == Constants.PERCENT || LanguageService.MAX_TOPICS > count++) {
                                Log.d(this.getClass().getSimpleName(), "Topic { label = " + topic.getLabel() + ", score = " + topic.getScore() + ", wikiLink = " + topic.getWikiLink() + " }");
                            }
                        }

                        final Set<String>  uniqueEntities = new ArraySet<>();
                        final List<Entity> entities       = new ArrayList<>(response.getEntities());

                        Collections.sort(entities, LanguageService.ENTITY_COMPARATOR);

                        for (final Entity entity : entities) {
                            if (entity.getConfidenceScore() >= 1 && entity.getWikiLink() != null) {
                                if (!uniqueEntities.contains(entity.getWikiLink())) {
                                    uniqueEntities.add(entity.getWikiLink());

                                    Log.d(this.getClass().getSimpleName(), "Entity { entityId = " + entity.getEntityId() + ", confidenceScore = " + entity.getConfidenceScore() + ", matchedText = " + entity.getMatchedText() + ", wikiLink = " + entity.getWikiLink() + " }");

                                    this.getView().addEntity(entity.getMatchedText(), entity.getWikiLink());
                                }
                            }
                        }
                    }
                },
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                });
    }
}
