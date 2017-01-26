package com.github.ayltai.newspaper.list;

import java.io.Closeable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;

import flow.ClassKey;
import io.realm.FeedRealmProxy;
import io.realm.Realm;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

@SuppressLint("ViewConstructor")
public final class ListScreen extends FrameLayout implements ListPresenter.View, Closeable {
    public static final class Key extends ClassKey implements Parcelable {
        private final String url;

        public Key(@NonNull final String url) {
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        //region Parcelable

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull final Parcel dest, final int flags) {
            dest.writeString(this.url);
        }

        protected Key(@NonNull final Parcel in) {
            this.url = in.readString();
        }

        public static final Parcelable.Creator<ListScreen.Key> CREATOR = new Parcelable.Creator<ListScreen.Key>() {
            @NonNull
            @Override
            public ListScreen.Key createFromParcel(@NonNull final Parcel source) {
                return new ListScreen.Key(source);
            }

            @NonNull
            @Override
            public ListScreen.Key[] newArray(final int size) {
                return new ListScreen.Key[size];
            }
        };

        //endregion
    }

    //region Events

    private final BehaviorSubject<Void> attachedToWindow   = BehaviorSubject.create();
    private final BehaviorSubject<Void> detachedFromWindow = BehaviorSubject.create();

    private final PublishSubject<Void> refreshSubject    = PublishSubject.create();
    private final Observable<Void>     refreshObservable = this.refreshSubject.doOnNext(dummy -> this.resetPosition = true);

    //endregion

    //region Variables

    private final Subscriber<FeedRealmProxy> subscriber = new Subscriber<FeedRealmProxy>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable e) {
            LogUtils.getInstance().e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        @Override
        public void onNext(final FeedRealmProxy feed) {
            if (ListScreen.this.parentKey != null && Constants.SOURCE_BOOKMARK.equals(ListScreen.this.parentKey.url)) ListScreen.this.setItems(ListScreen.this.parentKey, feed);
        }
    };

    private final Realm realm;

    private ListScreen.Key parentKey;
    private Feed           feed;
    private boolean        hasAttached;
    private boolean        resetPosition;

    //endregion

    //region Components

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView       recyclerView;
    private ViewGroup          empty;
    private ListAdapter        adapter;

    //endregion

    public ListScreen(@NonNull final Context context) {
        super(context);

        this.realm = Realm.getDefaultInstance();

        //RxBus.getInstance().register(Feed.class, this.subscriber);
        RxBus.getInstance().register(FeedRealmProxy.class, this.subscriber);
    }

    @Override
    public void setItems(@NonNull final ListScreen.Key parentKey, @Nullable final Feed feed) {
        if (this.adapter != null) {
            this.adapter.close();
            this.adapter = null;
        }

        this.adapter = new ListAdapter(this.getContext(), this.parentKey = parentKey, Settings.getListViewType(this.getContext()), this.feed = feed);

        this.setUpRecyclerView();

        if (this.feed == null || this.feed.getItems().isEmpty()) {
            LayoutInflater.from(this.getContext()).inflate(Constants.SOURCE_BOOKMARK.equals(this.parentKey.url) ? R.layout.view_empty_bookmark : R.layout.view_empty_news, this.empty, true);

            this.recyclerView.setVisibility(View.GONE);
            this.empty.setVisibility(View.VISIBLE);
        } else {
            if (this.resetPosition) {
                this.resetPosition = false;

                Settings.setPosition(this.parentKey.url, 0);
            } else {
                this.recyclerView.scrollToPosition(Settings.getPosition(this.parentKey.url));
            }

            this.recyclerView.setVisibility(View.VISIBLE);
            this.empty.setVisibility(View.GONE);
        }

        if (this.swipeRefreshLayout.isRefreshing()) this.swipeRefreshLayout.setRefreshing(false);
    }

    @NonNull
    @Override
    public Observable<Void> refreshes() {
        return this.refreshObservable;
    }

    @Override
    public void showUpdateIndicator() {
        final Snackbar snackbar = Snackbar.make(this, R.string.update_indicator, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_refresh, view -> {
                this.swipeRefreshLayout.post(() -> {
                    this.swipeRefreshLayout.setRefreshing(true);
                    this.refreshSubject.onNext(null);
                });
            });

        ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(ContextUtils.getColor(this.getContext(), R.attr.textColorInverse));
        ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_action)).setTextColor(ContextUtils.getColor(this.getContext(), R.attr.accentColor));

        snackbar.show();
    }

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

        if (this.hasAttached) {
            this.setItems(this.parentKey, this.feed);
        } else {
            final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_list, this, false);

            this.recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            this.recyclerView.setAdapter(new ListScreen.DummyAdapter());

            this.swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
            this.swipeRefreshLayout.setColorSchemeResources(ContextUtils.getResourceId(this.getContext(), R.attr.primaryColor));
            this.swipeRefreshLayout.setOnRefreshListener(() -> this.refreshSubject.onNext(null));

            if (Configs.isScrollSnapEnabled()) new GravitySnapHelper(Gravity.TOP).attachToRecyclerView(this.recyclerView);

            this.empty = (ViewGroup)view.findViewById(R.id.empty);

            this.addView(view);

            this.hasAttached = true;
        }

        this.attachedToWindow.onNext(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (this.parentKey != null) Settings.setPosition(this.parentKey.getUrl(), ((LinearLayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition());

        this.detachedFromWindow.onNext(null);
    }

    @Override
    public void close() {
        RxBus.getInstance().unregister(FeedRealmProxy.class, this.subscriber);

        if (this.adapter != null) {
            this.adapter.close();
            this.adapter = null;
        }

        if (!this.realm.isClosed()) this.realm.close();
    }

    //endregion

    private void setUpRecyclerView() {
        if (Configs.isItemAnimationEnabled()) {
            final AnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(this.adapter);
            alphaAdapter.setFirstOnly(false);

            final AnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(alphaAdapter);
            scaleAdapter.setFirstOnly(false);

            this.recyclerView.setAdapter(scaleAdapter);
        } else {
            this.recyclerView.setAdapter(this.adapter);
        }

        this.empty.removeAllViews();
    }

    private static final class DummyAdapter extends RecyclerView.Adapter<ListScreen.DummyViewHolder> {
        @Override
        public int getItemCount() {
            return Constants.INIT_LOAD_ITEM_COUNT;
        }

        @NonNull
        @Override
        public ListScreen.DummyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            return new ListScreen.DummyViewHolder(LayoutInflater.from(parent.getContext()).inflate(Settings.getListViewType(parent.getContext()) == Constants.LIST_VIEW_TYPE_COMPACT ? R.layout.view_item_compact : R.layout.view_item_cozy, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ListScreen.DummyViewHolder holder, final int position) {
        }
    }

    private static final class DummyViewHolder extends RecyclerView.ViewHolder {
        DummyViewHolder(@NonNull final View itemView) {
            super(itemView);

            final int textColor = ContextUtils.getColor(itemView.getContext(), R.attr.textColorHint);

            final TextView title = (TextView)itemView.findViewById(R.id.title);
            title.setTextColor(textColor);
            title.setText("██████████");

            final TextView description = (TextView)itemView.findViewById(R.id.description);
            description.setTextColor(textColor);
            description.setText("████████████████████████████████████████████████████████████████████████████████████████████████████");

            final TextView source = (TextView)itemView.findViewById(R.id.source);
            source.setTextColor(textColor);
            source.setText("█████");

            final TextView publishDate = (TextView)itemView.findViewById(R.id.publishDate);
            publishDate.setTextColor(textColor);
            publishDate.setText("███████");
        }
    }
}
