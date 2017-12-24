package com.github.ayltai.newspaper.ads;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ayltai.newspaper.view.Presenter;
import com.github.ayltai.newspaper.view.binding.BindingPresenter;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.ViewBinder;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class NativeAdPresenter<M, V extends NativeAdView> extends BindingPresenter<M, V> implements LifecycleObserver {
    public interface View extends Presenter.View {
        void show();

        void hide();
    }

    public static final class Builder<M, V extends NativeAdView> {
        private final Context context;
        private final String  adUnitId;

        private int                 layoutId;
        private int                 titleTextId;
        private int                 descriptionTextId;
        private int                 actionTextId;
        private int                 mainImageId;
        private int                 iconImageId;
        private int                 privacyImageId;
        private int                 prefetch;
        private Map<String, String> keywords;

        public Builder(@NonNull final Context context, @NonNull final String adUnitId) {
            this.context  = context;
            this.adUnitId = adUnitId;
        }

        @NonNull
        public NativeAdPresenter.Builder layoutId(@LayoutRes final int layoutId) {
            this.layoutId = layoutId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder titleTextId(@IdRes final int titleTextId) {
            this.titleTextId = titleTextId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder descriptionTextId(@IdRes final int descriptionTextId) {
            this.descriptionTextId = descriptionTextId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder actionTextId(@IdRes final int actionTextId) {
            this.actionTextId = actionTextId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder mainImageId(@IdRes final int mainImageId) {
            this.mainImageId = mainImageId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder iconImageId(@IdRes final int iconImageId) {
            this.iconImageId = iconImageId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder privacyImageId(@IdRes final int privacyImageId) {
            this.privacyImageId = privacyImageId;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder prefetch(final int prefetch) {
            this.prefetch = prefetch;

            return this;
        }

        @NonNull
        public NativeAdPresenter.Builder keywords(@Nullable final Map<String, String> keywords) {
            this.keywords = keywords;

            return this;
        }

        @NonNull
        public NativeAdPresenter<M, V> build() {
            final NativeAdPresenter<M, V> presenter = new NativeAdPresenter<>(this.context, this.adUnitId, this.layoutId, this.titleTextId, this.descriptionTextId, this.actionTextId, this.mainImageId, this.iconImageId, this.privacyImageId);

            presenter.setPrefetch(this.prefetch);
            presenter.setKeywords(this.keywords);

            return presenter;
        }
    }

    private static final Queue<StaticNativeAd> ADS         = new ArrayDeque<>();
    private static final AtomicInteger         PREFETCHING = new AtomicInteger(0);

    private static boolean isDisposed;

    private final FlowableProcessor<NativeErrorCode> errors = PublishProcessor.create();

    private final MoPubAdRenderer<StaticNativeAd> renderer;
    private final MoPubNative                     moPubNative;

    private int                 prefetch;
    private Map<String, String> keywords;

    private NativeAdPresenter(@NonNull final Context context, @NonNull final String adUnitId, @LayoutRes final int layoutId, @IdRes final int titleTextId, @IdRes final int descriptionTextId, @IdRes final int actionTextId, @IdRes final int mainImageId, @IdRes final int iconImageId, @IdRes final int privacyImageId) {
        this.renderer = new MoPubStaticNativeAdRenderer(new ViewBinder.Builder(layoutId)
            .titleId(titleTextId)
            .textId(descriptionTextId)
            .callToActionId(actionTextId)
            .mainImageId(mainImageId)
            .iconImageId(iconImageId)
            .privacyInformationIconImageId(privacyImageId)
            .build());

        this.moPubNative = new MoPubNative(context, adUnitId, new MoPubNative.MoPubNativeNetworkListener() {
                @Override
                public void onNativeLoad(@NonNull final NativeAd nativeAd) {
                    synchronized (NativeAdPresenter.ADS) {
                        NativeAdPresenter.ADS.offer((StaticNativeAd)nativeAd.getBaseNativeAd());

                        NativeAdPresenter.PREFETCHING.decrementAndGet();
                    }
                }

                @Override
                public void onNativeFail(@NonNull final NativeErrorCode errorCode) {
                    NativeAdPresenter.PREFETCHING.decrementAndGet();

                    NativeAdPresenter.this.errors.onNext(errorCode);
                }
            });

        this.moPubNative.registerAdRenderer(this.renderer);
    }

    public int getPrefetch() {
        return this.prefetch;
    }

    public void setPrefetch(final int prefetch) {
        this.prefetch = Math.max(1, prefetch);
    }

    @Nullable
    public Map<String, String> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(@Nullable final Map<String, String> keywords) {
        this.keywords = keywords;
    }

    @NonNull
    public Flowable<NativeErrorCode> errors() {
        return this.errors;
    }

    public void prefetch() {
        final int diff = this.prefetch - NativeAdPresenter.ADS.size();

        if (diff > 0) {
            final StringBuilder builder = new StringBuilder();

            if (this.keywords != null) {
                for (final Map.Entry<String, String> keyword : this.keywords.entrySet()) {
                    if (builder.length() > 0) builder.append(',');
                    builder.append(keyword.getKey())
                        .append(':')
                        .append(keyword.getValue());
                }
            }

            for (int i = 0; i < diff; i++) {
                if (NativeAdPresenter.PREFETCHING.get() < this.prefetch) {
                    NativeAdPresenter.PREFETCHING.incrementAndGet();

                    this.moPubNative.makeRequest(new RequestParameters.Builder()
                        .keywords(builder.toString())
                        .desiredAssets(EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT, RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT, RequestParameters.NativeAdAsset.MAIN_IMAGE, RequestParameters.NativeAdAsset.ICON_IMAGE))
                        .build());
                }
            }
        }
    }

    public void refresh() {
        final NativeAdView   view = this.getView();
        final StaticNativeAd ad;

        synchronized (NativeAdPresenter.ADS) {
            ad = NativeAdPresenter.ADS.isEmpty() ? null : view == null ? null : NativeAdPresenter.ADS.poll();
        }

        if (ad == null) {
            if (view != null) view.hide();
        } else {
            ad.prepare(view);

            this.renderer.renderAdView(view, ad);

            view.show();
        }

        this.prefetch();
    }

    @Override
    public boolean isDisposed() {
        return NativeAdPresenter.isDisposed;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        NativeAdPresenter.isDisposed = true;

        synchronized (NativeAdPresenter.ADS) {
            for (final StaticNativeAd ad : NativeAdPresenter.ADS) ad.destroy();

            NativeAdPresenter.ADS.clear();
        }

        this.moPubNative.destroy();
    }

    @Override
    public void onViewAttached(@NonNull final V view, final boolean isFirstTimeAttachment) {
        super.onViewAttached(view, isFirstTimeAttachment);

        this.refresh();
    }
}
