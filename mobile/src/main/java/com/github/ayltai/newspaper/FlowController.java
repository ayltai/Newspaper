package com.github.ayltai.newspaper;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.util.TestUtils;
import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.ScreenPresenter;

import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;

final class FlowController {
    private static final String TAG = FlowController.class.getSimpleName();

    private final Map<Object, Pair<Presenter, Presenter.View>> cache = new ArrayMap<>();

    private final Activity activity;

    FlowController(@NonNull final Activity activity) {
        this.activity = activity;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    Context attachNewBase(@NonNull final Context newBase) {
        return Flow.configure(newBase, this.activity)
            .keyParceler(new KeyParceler() {
                @NonNull
                @Override
                public Parcelable toParcelable(@NonNull final Object key) {
                    return (Parcelable)key;
                }

                @NonNull
                @Override
                public Object toKey(@NonNull final Parcelable parcelable) {
                    return parcelable;
                }
            })
            .dispatcher(KeyDispatcher.configure(this.activity, (outgoingState, incomingState, direction, incomingContexts, callback) -> {
                if (outgoingState != null) outgoingState.save(((ViewGroup)this.activity.findViewById(android.R.id.content)).getChildAt(0));

                final Presenter      presenter;
                final Presenter.View view;

                final Pair<Presenter, Presenter.View> pair = this.cache.get(incomingState.getKey());

                if (pair == null) {
//                    if (incomingState.getKey() instanceof MainScreen.Key) {
//                        presenter = new MainPresenter();
//                        view      = new MainScreen(this.activity);
//
//                        this.cache.put(incomingState.getKey(), Pair.create(presenter, view));
//                    } else if (incomingState.getKey() instanceof RoomListScreen.Key) {
//                        presenter = new RoomListPresenter();
//                        view      = new RoomListScreen(this.activity);
//                    } else {
                        presenter = null;
                        view      = null;
//                    }
                } else {
                    presenter = pair.first;
                    view      = pair.second;
                }

                if (presenter != null) {
                    if (view.attachments() != null) view.attachments().subscribe(
                        isFirstTimeAttachment -> presenter.onViewAttached(view, isFirstTimeAttachment),
                        error -> {
                            if (TestUtils.isLoggable()) Log.e(FlowController.TAG, error.getMessage(), error);
                        }
                    );

                    if (view.detachments() != null) view.detachments().subscribe(
                        irrelevant -> presenter.onViewDetached(),
                        error -> {
                            if (TestUtils.isLoggable()) Log.e(FlowController.TAG, error.getMessage(), error);
                        }
                    );

                    this.dispatch((View)view, incomingState, callback);
                }
            }).build())
//            .defaultKey(MainScreen.KEY)
            .install();
    }

    /**
     * Attempts to handle back button pressed event.
     * @return {@code true} if back button pressed event is handled; otherwise, {@code false}.
     */
    boolean onBackPressed() {
        if (!Flow.get(this.activity).goBack()) {
            final View view = ((ViewGroup)this.activity.findViewById(android.R.id.content)).getChildAt(0);

            if (view instanceof ScreenPresenter.View) return ((ScreenPresenter.View)view).goBack();
        }

        return false;
    }

    void onDestroy() {
        this.cache.clear();
    }

    private void dispatch(@NonNull final View view, @NonNull final State incomingState, @NonNull final TraversalCallback callback) {
        incomingState.restore(view);

        this.activity.setContentView(view);

        callback.onTraversalCompleted();
    }
}
