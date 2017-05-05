package com.github.ayltai.newspaper.list;

import java.io.Closeable;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ayltai.newspaper.Configs;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.model.Item;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.ayltai.newspaper.util.ContextUtils;

import flow.ClassKey;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

@SuppressLint("ViewConstructor")
public final class ListScreen extends FrameLayout implements ListPresenter.View, Closeable {
    public static final class Key extends ClassKey implements Parcelable {
        private final String category;

        public Key(@NonNull final String category) {
            this.category = category;
        }

        @NonNull
        public String getCategory() {
            return this.category;
        }

        //region Parcelable

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull final Parcel dest, final int flags) {
            dest.writeString(this.category);
        }

        protected Key(@NonNull final Parcel in) {
            this.category = in.readString();
        }

        public static final Creator<Key> CREATOR = new Creator<Key>() {
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

    private ListScreen.Key parentKey;
    private List<Item>     items;
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
    }

    @Override
    public void setItems(@NonNull final ListScreen.Key parentKey, @NonNull final List<Item> items) {
        if (this.hasAttached) {
            this.items = items;

            if (this.adapter != null) {
                // Detaches the adapter from RecyclerView before updating the adapter
                this.recyclerView.setAdapter(null);

                this.adapter.close();
                this.adapter = null;
            }

            this.adapter = new ListAdapter(this.getContext(), this.parentKey = parentKey, Settings.getListViewType(this.getContext()), this.items);

            this.setUpRecyclerView();

            if (this.items.isEmpty()) {
                this.empty.removeAllViews();

                LayoutInflater.from(this.getContext()).inflate(Constants.CATEGORY_BOOKMARK.equals(this.parentKey.category) ? R.layout.view_empty_bookmark : R.layout.view_empty_news, this.empty, true);

                this.recyclerView.setVisibility(View.GONE);
                this.empty.setVisibility(View.VISIBLE);
            } else {
                this.restoreItemPosition();

                this.recyclerView.setVisibility(View.VISIBLE);
                this.empty.setVisibility(View.GONE);
            }

            if (this.swipeRefreshLayout.isRefreshing()) this.swipeRefreshLayout.setRefreshing(false);
        } else {
            this.items = items;
        }
    }

    @NonNull
    @Override
    public Observable<Void> refreshes() {
        return this.refreshObservable;
    }

    @Override
    public void showUpdateIndicator() {
        final Snackbar snackbar = Snackbar.make(this, R.string.update_indicator, Constants.UPDATE_INDICATOR_DURATION)
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
            if (this.parentKey != null) this.setItems(this.parentKey, this.items);
        } else {
            this.hasAttached = true;

            final View                view          = LayoutInflater.from(this.getContext()).inflate(R.layout.screen_list, this, false);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());

            this.recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
            this.recyclerView.setLayoutManager(layoutManager);
            this.recyclerView.setAdapter(new DummyAdapter());

            this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                    if (ListScreen.this.parentKey != null) {
                        final int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                        Settings.setPosition(ListScreen.this.parentKey.category, position == RecyclerView.NO_POSITION ? layoutManager.findFirstVisibleItemPosition() : position);
                    }
                }
            });

            this.swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
            this.swipeRefreshLayout.setColorSchemeResources(ContextUtils.getResourceId(this.getContext(), R.attr.primaryColor));
            this.swipeRefreshLayout.setOnRefreshListener(() -> this.refreshSubject.onNext(null));

            this.empty = (ViewGroup)view.findViewById(R.id.empty);

            this.addView(view);
        }

        this.attachedToWindow.onNext(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (this.parentKey != null && !((Activity)this.getContext()).isFinishing()) Settings.setPosition(this.parentKey.getCategory(), ((LinearLayoutManager)this.recyclerView.getLayoutManager()).findFirstVisibleItemPosition());

        this.detachedFromWindow.onNext(null);
    }

    @Override
    public void close() {
        if (this.adapter != null) {
            this.adapter.close();
            this.adapter = null;
        }

        this.detachedFromWindow.onNext(null);
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

    private void restoreItemPosition() {
        if (this.resetPosition) {
            this.resetPosition = false;

            Settings.setPosition(this.parentKey.category, 0);
        } else {
            this.recyclerView.scrollToPosition(Settings.getPosition(this.parentKey.category));
        }
    }
}
