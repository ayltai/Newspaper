package com.github.ayltai.newspaper.item;

import java.io.Closeable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.model.Image;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.IntentUtils;
import com.github.ayltai.newspaper.util.ItemUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.widget.FaceCenteredImageView;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding.view.RxView;

import flow.Flow;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public final class ItemViewHolder extends RecyclerView.ViewHolder implements ItemPresenter.View, Closeable {
    //region Events

    private final PublishSubject<Void>    clicks    = PublishSubject.create();
    private final PublishSubject<Boolean> bookmarks = PublishSubject.create();
    private final PublishSubject<Void>    shares    = PublishSubject.create();

    //endregion

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    //region Components

    private final View                  itemView;
    private final TextView              title;
    private final TextView              description;
    private final TextView              source;
    private final TextView              publishDate;
    private final FaceCenteredImageView thumbnail;
    private final SimpleDraweeView      draweeView;

    //endregion

    public ItemViewHolder(@NonNull final View itemView) {
        super(itemView);

        this.itemView    = itemView;
        this.title       = (TextView)itemView.findViewById(R.id.title);
        this.description = (TextView)itemView.findViewById(R.id.description);
        this.source      = (TextView)itemView.findViewById(R.id.source);
        this.publishDate = (TextView)itemView.findViewById(R.id.publishDate);

        final View view = itemView.findViewById(R.id.thumbnail);

        if (view instanceof FaceCenteredImageView) {
            this.thumbnail  = (FaceCenteredImageView)view;
            this.draweeView = null;

            final Activity activity = ContextUtils.getActivity(itemView.getContext());

            if (activity != null) {
                final DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                this.thumbnail.setScreenWidth(metrics.widthPixels);
            }

            ImageUtils.configure((SubsamplingScaleImageView)this.thumbnail.getChildAt(0));
        } else if (view instanceof SimpleDraweeView) {
            this.thumbnail  = null;
            this.draweeView = (SimpleDraweeView)view;
        } else {
            this.thumbnail  = null;
            this.draweeView = null;
        }

        this.subscriptions.add(RxView.clicks(this.itemView)
            .subscribe(
                v -> this.clicks.onNext(null),
                error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        if (this.thumbnail != null) this.subscriptions.add(RxView.clicks(this.thumbnail)
            .subscribe(
                v -> this.clicks.onNext(null),
                error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final String title) {
        ItemViewHolder.setText(this.title, title, true, true);
    }

    @Override
    public void setDescription(@Nullable final String description) {
        ItemViewHolder.setText(this.description, description, true, true);
    }

    @Override
    public void setSource(@Nullable final String source) {
        ItemViewHolder.setText(this.source, source, true, false);
    }

    @Override
    public void setLink(@Nullable final String link) {
    }

    @Override
    public void setPublishDate(final long publishDate) {
        ItemViewHolder.setText(this.publishDate, publishDate == 0 ? null : DateUtils.getTimeAgo(this.itemView.getContext(), publishDate), false, false);
    }

    @Override
    public void setThumbnail(@Nullable final String thumbnail, @Constants.ListViewType final int type) {
        if (this.thumbnail != null) ItemViewHolder.setImage(this.thumbnail, thumbnail, type);
        if (this.draweeView != null) ItemViewHolder.setImage(this.draweeView, thumbnail);
    }

    @Override
    public void setThumbnails(@NonNull final List<Image> images) {
        // Ignored
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        // Ignored
    }

    @NonNull
    @Override
    public Context getContext() {
        return this.itemView.getContext();
    }

    //endregion

    //region Actions

    @NonNull
    @Override
    public Observable<Void> clicks() {
        return this.clicks;
    }

    @Nullable
    @Override
    public Observable<Integer> zooms() {
        return null;
    }

    @Nullable
    @Override
    public Observable<Boolean> bookmarks() {
        return this.bookmarks;
    }

    @Nullable
    @Override
    public Observable<Void> shares() {
        return this.shares;
    }

    @Override
    public void showItem(@NonNull final ListScreen.Key parentKey, @NonNull final Item item) {
        Flow.get(this.itemView).set(new ItemScreen.Key(parentKey, item));
    }

    @Override
    public void showMedia(@NonNull final String url) {
    }

    @Override
    public void share(@NonNull final String url) {
        IntentUtils.share(this.itemView.getContext(), url);
    }

    //endregion

    //region Lifecycle

    @Nullable
    @Override
    public Observable<Void> attachments() {
        return null;
    }

    @Nullable
    @Override
    public Observable<Void> detachments() {
        return null;
    }

    @Override
    public void close() {
        this.subscriptions.unsubscribe();
    }

    //endregion

    private static void setImage(@NonNull final BigImageView imageView, @Nullable final String url, @Constants.ListViewType final int type) {
        if (TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);

            imageView.showImage(Uri.parse(url));
        }
    }

    private static void setImage(@NonNull final SimpleDraweeView draweeView, @Nullable final String url) {
        if (TextUtils.isEmpty(url)) {
            draweeView.setVisibility(View.GONE);
        } else {
            draweeView.setVisibility(View.VISIBLE);
            draweeView.setImageURI(url);
        }
    }

    private static void setText(@NonNull final TextView textView, @Nullable final String value, final boolean removeImages, final boolean removeHtml) {
        if (TextUtils.isEmpty(value)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);

            final String text = removeHtml ? ItemUtils.removeHtml(value) : value;
            textView.setText(removeImages ? ItemUtils.removeImages(text) : text);
        }
    }
}
