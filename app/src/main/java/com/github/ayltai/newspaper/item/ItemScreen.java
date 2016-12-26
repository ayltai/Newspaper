package com.github.ayltai.newspaper.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.rss.Item;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.ItemUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.rohitarya.fresco.facedetection.processor.FaceCenterCrop;
import com.stfalcon.frescoimageviewer.ImageViewer;

import flow.ClassKey;
import flow.Flow;
import flow.TreeKey;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import xyz.hanks.library.SmallBang;

public final class ItemScreen extends FrameLayout implements ItemPresenter.View {
    public static final class Key extends ClassKey implements TreeKey, Parcelable {
        private final ListScreen.Key parentKey;
        private final Item           item;

        Key(@NonNull final ListScreen.Key parentKey, @NonNull final Item item) {
            this.parentKey = parentKey;
            this.item      = item;
        }

        @NonNull
        @Override
        public Object getParentKey() {
            return this.parentKey;
        }

        @NonNull
        public Item getItem() {
            return this.item;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            final ItemScreen.Key that = (ItemScreen.Key)o;
            return this.item.getGuid() == null ? that.item.getGuid() == null : this.item.getGuid().equals(that.item.getGuid());

        }

        @Override
        public int hashCode() {
            final int hashCode = super.hashCode();

            if (this.item.getGuid() == null) return hashCode;

            return 31 * hashCode + this.item.getGuid().hashCode();
        }

        //region Parcelable

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull final Parcel dest, final int flags) {
            dest.writeParcelable(this.parentKey, flags);
            dest.writeParcelable(this.item, flags);
        }

        protected Key(@NonNull final Parcel in) {
            this.parentKey = in.readParcelable(ListScreen.Key.class.getClassLoader());
            this.item      = in.readParcelable(Item.class.getClassLoader());
        }

        public static final Parcelable.Creator<ItemScreen.Key> CREATOR = new Parcelable.Creator<ItemScreen.Key>() {
            @NonNull
            @Override
            public ItemScreen.Key createFromParcel(@NonNull final Parcel source) {
                return new ItemScreen.Key(source);
            }

            @NonNull
            @Override
            public ItemScreen.Key[] newArray(final int size) {
                return new ItemScreen.Key[size];
            }
        };

        //endregion
    }

    //region Events

    private final BehaviorSubject<Void> attachedToWindow   = BehaviorSubject.create();
    private final BehaviorSubject<Void> detachedFromWindow = BehaviorSubject.create();

    private final PublishSubject<Void>    zooms     = PublishSubject.create();
    private final PublishSubject<Boolean> bookmarks = PublishSubject.create();

    //endregion

    //region Variables

    private CompositeSubscription subscriptions;
    private String                link;
    private boolean               isBookmarked;
    private boolean               hasAttached;
    private int                   screenWidth;

    //endregion

    //region Components

    private AppBarLayout     appBarLayout;
    private TextView         toolbarTitle;
    private ImageView bookmark;
    private View             share;
    private TextView         title;
    private TextView         description;
    private TextView         source;
    private TextView         publishDate;
    private SimpleDraweeView thumbnail;
    private SmallBang        smallBang;

    //endregion

    public ItemScreen(@NonNull final Context context) {
        super(context);
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final String title) {
        if (this.hasAttached) {
            if (TextUtils.isEmpty(title)) {
                this.title.setVisibility(View.GONE);
                this.toolbarTitle.setText(this.getResources().getText(R.string.app_name));
            } else {
                this.title.setVisibility(View.VISIBLE);
                this.title.setText(ItemUtils.removeHtml(title));
                this.toolbarTitle.setText(this.title.getText());
            }
        }
    }

    @Override
    public void setDescription(@Nullable final String description) {
        if (this.hasAttached) {
            if (TextUtils.isEmpty(description)) {
                this.description.setVisibility(View.GONE);
            } else {
                this.description.setVisibility(View.VISIBLE);
                this.description.setText(ItemUtils.removeHtml(description));
            }
        }
    }

    @Override
    public void setSource(@Nullable final String source) {
        if (this.hasAttached) {
            if (TextUtils.isEmpty(source)) {
                this.source.setVisibility(View.GONE);
            } else {
                this.source.setVisibility(View.VISIBLE);
                this.source.setText(ItemUtils.removeHtml(source));
            }
        }
    }

    @Override
    public void setLink(@Nullable final String link) {
        if (BuildConfig.DEBUG) Log.d(this.getClass().getName(), "link = " + link);

        this.link = link;
    }

    @Override
    public void setPublishDate(final long publishDate) {
        if (this.hasAttached) {
            if (publishDate == 0) {
                this.publishDate.setVisibility(View.GONE);
            } else {
                this.publishDate.setVisibility(View.VISIBLE);
                this.publishDate.setText(DateUtils.getTimeAgo(this.getContext(), publishDate));
            }
        }
    }

    @Override
    public void setThumbnail(@Nullable final String thumbnail, @Constants.ListViewType final int type) {
        if (this.hasAttached) {
            if (TextUtils.isEmpty(thumbnail)) {
                this.appBarLayout.setExpanded(false, false);
            } else {
                this.appBarLayout.setExpanded(true, false);

                this.thumbnail.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.parse(ItemUtils.getOriginalMediaUrl(thumbnail)))
                        .setPostprocessor(new FaceCenterCrop(this.screenWidth, this.getResources().getDimensionPixelSize(R.dimen.thumbnail_cozy)))
                        .build())
                    .setOldController(this.thumbnail.getController())
                    .build());
            }
        }
    }

    @Override
    public void setIsBookmarked(final boolean isBookmarked) {
        this.isBookmarked = isBookmarked;

        this.bookmark.setImageResource(this.isBookmarked ? R.drawable.ic_bookmark_white_24px : R.drawable.ic_bookmark_border_white_24px);
    }

    //endregion

    //region Actions

    @Nullable
    @Override
    public Observable<Void> clicks() {
        return null;
    }

    @Nullable
    @Override
    public Observable<Void> zooms() {
        return this.zooms;
    }

    @Nullable
    @Override
    public Observable<Boolean> bookmarks() {
        return this.bookmarks;
    }

    @Override
    public void showItem(@NonNull final ListScreen.Key parentKey, @NonNull final Item item) {
    }

    @Override
    public void showOriginalMedia(@NonNull final String url) {
        new ImageViewer.Builder(this.getContext(), new String[] { url }).show();
    }

    //endregion

    //region Lifecycle

    @NonNull
    @Override
    public Observable<Void> attachments() {
        return this.attachedToWindow;
    }

    @NonNull
    @Override
    public Observable<Void> detachments() {
        return this.detachedFromWindow;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.smallBang = SmallBang.attach2Window((Activity)this.getContext());

        final DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        this.screenWidth = metrics.widthPixels;

        if (!this.hasAttached) {
            final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_item, this, false);

            this.appBarLayout = (AppBarLayout)view.findViewById(R.id.appBarLayout);
            this.toolbarTitle = (TextView)view.findViewById(R.id.toolbar_title);
            this.bookmark     = (ImageView)view.findViewById(R.id.bookmark);
            this.share        = view.findViewById(R.id.share);
            this.title        = (TextView)view.findViewById(R.id.title);
            this.description  = (TextView)view.findViewById(R.id.description);
            this.source       = (TextView)view.findViewById(R.id.source);
            this.publishDate  = (TextView)view.findViewById(R.id.publishDate);
            this.thumbnail    = (SimpleDraweeView)view.findViewById(R.id.thumbnail);

            final Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
            toolbar.setNavigationOnClickListener(v -> Flow.get(v).goBack());

            this.addView(view);

            this.hasAttached = true;
        }

        this.attachEvents();

        this.attachedToWindow.onNext(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.smallBang = null;

        if (this.subscriptions != null && this.subscriptions.hasSubscriptions()) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        this.detachedFromWindow.onNext(null);
    }

    //endregion

    private void attachEvents() {
        if (this.subscriptions == null) {
            this.subscriptions = new CompositeSubscription();

            this.subscriptions.add(RxView.clicks(this.thumbnail).subscribe(dummy -> this.zooms.onNext(null), error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

            this.subscriptions.add(RxView.clicks(this.share).subscribe(dummy -> {
                if (!TextUtils.isEmpty(this.link)) this.getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(this.link)), this.getContext().getText(R.string.share_to)));
            }, error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));

            this.subscriptions.add(RxView.clicks(this.bookmark).subscribe(dummy -> {
                this.isBookmarked = !this.isBookmarked;

                this.bookmark.setImageResource(this.isBookmarked ? R.drawable.ic_bookmark_white_24px : R.drawable.ic_bookmark_border_white_24px);

                if (this.smallBang != null && this.isBookmarked) this.smallBang.bang(this.bookmark);

                this.bookmarks.onNext(this.isBookmarked);
            }, error -> FirebaseCrash.logcat(Log.ERROR, this.getClass().getName(), error.getMessage())));
        }
    }
}
