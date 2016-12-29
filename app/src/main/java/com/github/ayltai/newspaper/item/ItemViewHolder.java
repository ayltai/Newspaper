package com.github.ayltai.newspaper.item;

import java.io.Closeable;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ItemUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.rohitarya.fresco.facedetection.processor.FaceCenterCrop;
import com.tubb.smrv.SwipeHorizontalMenuLayout;

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
    private final int                   screenWidth;

    //region Components

    private final View             itemView;
    private final TextView         title;
    private final TextView         description;
    private final TextView         source;
    private final TextView         publishDate;
    private final SimpleDraweeView thumbnail;
    private final ImageView        bookmark;

    //endregion

    private boolean isBookmarked;

    public ItemViewHolder(@NonNull final View itemView) {
        super(itemView);

        this.itemView    = itemView;
        this.title       = (TextView)itemView.findViewById(R.id.title);
        this.description = (TextView)itemView.findViewById(R.id.description);
        this.source      = (TextView)itemView.findViewById(R.id.source);
        this.publishDate = (TextView)itemView.findViewById(R.id.publishDate);
        this.thumbnail   = (SimpleDraweeView)itemView.findViewById(R.id.thumbnail);
        this.bookmark    = (ImageView)itemView.findViewById(R.id.bookmark);

        final SwipeHorizontalMenuLayout swipeHorizontalMenuLayout = (SwipeHorizontalMenuLayout)this.itemView;

        this.subscriptions.add(RxView.clicks(this.itemView).subscribe(view -> this.clicks.onNext(null), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

        if (this.bookmark != null) this.subscriptions.add(RxView.clicks(this.bookmark).subscribe(view -> {
            this.setIsBookmarked(!this.isBookmarked);

            swipeHorizontalMenuLayout.smoothCloseMenu();

            this.bookmarks.onNext(this.isBookmarked);
        }));

        final View share = itemView.findViewById(R.id.share);

        if (share != null) this.subscriptions.add(RxView.clicks(share).subscribe(view -> {
            swipeHorizontalMenuLayout.smoothCloseMenu();

            this.shares.onNext(null);
        }));

        final DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)this.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        this.screenWidth = metrics.widthPixels;
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final String title) {
        ItemViewHolder.setText(this.title, title, true);
    }

    @Override
    public void setDescription(@Nullable final String description) {
        ItemViewHolder.setText(this.description, description, true);
    }

    @Override
    public void setSource(@Nullable final String source) {
        ItemViewHolder.setText(this.source, source, true);
    }

    @Override
    public void setLink(@Nullable final String link) {
    }

    @Override
    public void setPublishDate(final long publishDate) {
        ItemViewHolder.setText(this.publishDate, publishDate == 0 ? null : DateUtils.getTimeAgo(this.itemView.getContext(), publishDate), false);
    }

    @Override
    public void setThumbnail(@Nullable final String thumbnail, @Constants.ListViewType final int type) {
        ItemViewHolder.setImage(this.thumbnail, thumbnail, type, this.screenWidth);
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        this.isBookmarked = isBookmarked;

        this.bookmark.setImageResource(this.isBookmarked ? R.drawable.ic_bookmark_white_24px : R.drawable.ic_bookmark_border_white_24px);
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
    public Observable<Void> zooms() {
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
    public void showOriginalMedia(@NonNull final String url) {
    }

    @Override
    public void share(@NonNull final String url) {
        this.itemView.getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), this.itemView.getContext().getText(R.string.share_to)));
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

    private static void setImage(@NonNull final SimpleDraweeView draweeView, @Nullable final String url, @Constants.ListViewType final int type, final int screenWidth) {
        if (TextUtils.isEmpty(url)) {
            draweeView.setVisibility(View.GONE);
        } else {
            draweeView.setVisibility(View.VISIBLE);

            if (type == Constants.LIST_VIEW_TYPE_COZY) {
                draweeView.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.parse(ItemUtils.getOriginalMediaUrl(url)))
                        .setPostprocessor(new FaceCenterCrop(screenWidth, draweeView.getResources().getDimensionPixelSize(R.dimen.thumbnail_cozy)))
                        .build())
                    .setOldController(draweeView.getController())
                    .build());
            } else {
                draweeView.setImageURI(url);
            }
        }
    }

    private static void setText(@NonNull final TextView textView, @Nullable final String value, final boolean removeHtml) {
        if (TextUtils.isEmpty(value)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(removeHtml ? ItemUtils.removeHtml(value) : value);
        }
    }
}
