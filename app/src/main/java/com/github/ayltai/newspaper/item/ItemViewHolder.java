package com.github.ayltai.newspaper.item;

import java.io.Closeable;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ImageUtils;
import com.github.ayltai.newspaper.util.ItemUtils;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding.view.RxView;
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

    //region Components

    private final View             itemView;
    private final TextView         title;
    private final TextView         description;
    private final TextView         source;
    private final TextView         publishDate;
    private final BigImageView     thumbnail;
    private final SimpleDraweeView draweeView;
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
        this.bookmark    = (ImageView)itemView.findViewById(R.id.bookmark);

        final View view = itemView.findViewById(R.id.thumbnail);

        if (view instanceof BigImageView) {
            this.thumbnail  = (BigImageView)view;
            this.draweeView = null;

            ImageUtils.configure((SubsamplingScaleImageView)this.thumbnail.getChildAt(0));
        } else if (view instanceof SimpleDraweeView) {
            this.thumbnail  = null;
            this.draweeView = (SimpleDraweeView)view;
        } else {
            this.thumbnail  = null;
            this.draweeView = null;
        }

        final SwipeHorizontalMenuLayout swipeHorizontalMenuLayout = (SwipeHorizontalMenuLayout)this.itemView;

        this.subscriptions.add(RxView.clicks(this.itemView).subscribe(v -> this.clicks.onNext(null), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));
        if (this.thumbnail != null) this.subscriptions.add(RxView.clicks(this.thumbnail).subscribe(v -> this.clicks.onNext(null), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

        if (this.bookmark != null) this.subscriptions.add(RxView.clicks(this.bookmark).subscribe(v -> {
            this.setIsBookmarked(!this.isBookmarked);

            swipeHorizontalMenuLayout.smoothCloseMenu();

            this.bookmarks.onNext(this.isBookmarked);
        }));

        final View share = itemView.findViewById(R.id.share);

        if (share != null) this.subscriptions.add(RxView.clicks(share).subscribe(v -> {
            swipeHorizontalMenuLayout.smoothCloseMenu();

            this.shares.onNext(null);
        }));
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
        if (this.thumbnail != null) ItemViewHolder.setImage(this.thumbnail, thumbnail, type);
        if (this.draweeView != null) ItemViewHolder.setImage(this.draweeView, thumbnail);
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

    private static void setImage(@NonNull final BigImageView imageView, @Nullable final String url, @Constants.ListViewType final int type) {
        if (TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);

            if (type == Constants.LIST_VIEW_TYPE_COZY) {
                imageView.showImage(Uri.parse(url), Uri.parse(ItemUtils.getOriginalMediaUrl(url)));
            } else {
                imageView.showImage(Uri.parse(url));
            }
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

    private static void setText(@NonNull final TextView textView, @Nullable final String value, final boolean removeHtml) {
        if (TextUtils.isEmpty(value)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(removeHtml ? ItemUtils.removeHtml(value) : value);
        }
    }
}
