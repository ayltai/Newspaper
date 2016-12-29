package com.github.ayltai.newspaper.list;

import java.io.Closeable;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.data.Feed;
import com.github.ayltai.newspaper.setting.Settings;

import flow.ClassKey;
import io.realm.Realm;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

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

    private final PublishSubject<Void> refreshes = PublishSubject.create();

    //endregion

    //region Variables

    private final Realm realm;

    private ListScreen.Key parentKey;
    private Feed           feed;
    private int            position = RecyclerView.NO_POSITION;
    private boolean        hasAttached;

    //endregion

    //region Components

    private WaveSwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView           recyclerView;
    private ViewGroup              empty;
    private ListAdapter            adapter;

    //endregion

    public ListScreen(@NonNull final Context context, @NonNull final Realm realm) {
        super(context);

        this.realm = realm;
    }

    @Override
    public void setItems(@NonNull final ListScreen.Key parentKey, @NonNull final Feed feed) {
        this.parentKey = parentKey;
        this.feed      = feed;

        this.recyclerView.setAdapter(this.adapter = new ListAdapter(this.getContext(), parentKey, Settings.getListViewType(this.getContext()), this.feed, this.realm));
        this.empty.removeAllViews();

        if (this.feed.getItems().isEmpty()) {
            LayoutInflater.from(this.getContext()).inflate(Constants.SOURCE_BOOKMARK.equals(this.parentKey.url) ? R.layout.view_empty_bookmark : R.layout.view_empty_news, this.empty, true);
        } else {
            this.recyclerView.scrollToPosition(Settings.getPosition(this.parentKey.url));
        }

        if (this.swipeRefreshLayout.isRefreshing()) this.swipeRefreshLayout.setRefreshing(false);
    }

    @NonNull
    @Override
    public Observable<Void> refreshes() {
        return this.refreshes;
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

        if (this.hasAttached && this.position > -1 && this.recyclerView.getAdapter() != null) {
            if (Constants.SOURCE_BOOKMARK.equals(this.parentKey.url)) {
                this.setItems(this.parentKey, this.feed);
            } else {
                if (this.recyclerView.getAdapter().getItemCount() > 0) this.recyclerView.scrollToPosition(this.position);
            }
        } else {
            final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_list, this, false);

            this.recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            this.recyclerView.setAdapter(new ListScreen.DummyAdapter());

            this.swipeRefreshLayout = (WaveSwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
            this.swipeRefreshLayout.setWaveColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
            this.swipeRefreshLayout.setColorSchemeResources(R.color.indicator);
            this.swipeRefreshLayout.setOnRefreshListener(() -> this.refreshes.onNext(null));

            this.empty = (ViewGroup)view.findViewById(R.id.empty);

            this.addView(view);

            this.hasAttached = true;
        }

        this.attachedToWindow.onNext(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.position = ((LinearLayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Settings.setPosition(this.parentKey.url, this.position);

        this.detachedFromWindow.onNext(null);
    }

    @Override
    public void close() {
        if (this.adapter != null) this.adapter.close();
    }

    //endregion

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

            final TextView title = (TextView)itemView.findViewById(R.id.title);
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.textColorHint));
            title.setText("██████████");

            final TextView description = (TextView)itemView.findViewById(R.id.description);
            description.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.textColorHint));
            description.setText("████████████████████████████████████████████████████████████████████████████████████████████████████");

            final TextView source = (TextView)itemView.findViewById(R.id.source);
            source.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.textColorHint));
            source.setText("█████");

            final TextView publishDate = (TextView)itemView.findViewById(R.id.publishDate);
            publishDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.textColorHint));
            publishDate.setText("███████");
        }
    }
}
