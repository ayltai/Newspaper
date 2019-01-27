package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.github.ayltai.newspaper.util.Irrelevant;

import io.reactivex.Flowable;

public interface Presenter<V extends Presenter.View> {
    interface View {
        @Nonnull
        @NonNull
        Context getContext();

        @Nullable
        Activity getActivity();

        @Nullable
        LifecycleOwner getLifecycleOwner();

        @Nonnull
        @NonNull
        Flowable<Boolean> attaches();

        @Nonnull
        @NonNull
        Flowable<Irrelevant> detaches();

        @CallSuper
        void onAttachedToWindow();

        @CallSuper
        void onDetachedFromWindow();

        boolean handleBack();
    }

    interface Factory<P extends Presenter<V>, V extends Presenter.View> {
        boolean isSupported(@Nonnull @NonNull Object key);

        @Nonnull
        @NonNull
        P createPresenter();

        @Nonnull
        @NonNull
        V createView(@Nonnull @NonNull Context context);
    }

    @Nullable
    V getView();

    void onViewAttached(@Nonnull @NonNull V view, boolean isFirstAttachment);

    void onViewDetached();
}
