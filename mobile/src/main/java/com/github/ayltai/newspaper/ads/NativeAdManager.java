package com.github.ayltai.newspaper.ads;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;

import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.squareup.haha.trove.THashMap;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public abstract class NativeAdManager<T extends BaseNativeAd> implements Disposable, LifecycleObserver {
    private final Queue<T>       ads       = new ArrayDeque<>();
    private final Queue<Integer> sequences = new ArrayDeque<>();
    private final AtomicInteger  sequence  = new AtomicInteger(0);

    private final FlowableProcessor<NativeErrorCode> errors = PublishProcessor.create();

    private final Map<String, String> keywords = new THashMap<>();
    private final Context             context;
    private final String              adUnitId;

    protected MoPubAdRenderer<T> renderer;

    private T       ad;
    private boolean isDisposed;
    private int     prefetchSize;

    protected NativeAdManager(@NonNull final Context context, @NonNull final String adUnitId, @LayoutRes final int layoutId) {
        this.context  = context;
        this.adUnitId = adUnitId;

        this.init();
    }

    public int getPrefetchSize() {
        return Math.max(1, this.prefetchSize);
    }

    public void setPrefetchSize(final int prefetchSize) {
        this.prefetchSize = Math.max(1, prefetchSize);
    }

    @NonNull
    public Map<String, String> getKeywords() {
        return Collections.unmodifiableMap(this.keywords);
    }

    public void addKeyword(@NonNull final String key, @Nullable final String value) {
        this.keywords.put(key, value);
    }

    protected abstract EnumSet<RequestParameters.NativeAdAsset> getDesiredAssets();

    @Override
    public boolean isDisposed() {
        return this.isDisposed;
    }

    @NonNull
    public Flowable<NativeErrorCode> errors() {
        return this.errors;
    }

    @UiThread
    public void prefetch() {
        synchronized (this.sequences) {
            if (this.getPrefetchSize() - this.ads.size() - this.sequences.size() > 0) {
                final StringBuilder builder = new StringBuilder();

                if (this.keywords != null) {
                    for (final Map.Entry<String, String> keyword : this.keywords.entrySet()) {
                        if (builder.length() > 0) builder.append(',');
                        builder.append(keyword.getKey())
                            .append(':')
                            .append(keyword.getValue());
                    }
                }

                final MoPubNative moPubNative = new MoPubNative(context, this.adUnitId, new MoPubNative.MoPubNativeNetworkListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNativeLoad(@NonNull final NativeAd nativeAd) {
                        synchronized (NativeAdManager.this.sequences) {
                            if (!NativeAdManager.this.sequences.isEmpty()) NativeAdManager.this.sequences.poll();

                            NativeAdManager.this.ads.offer((T)nativeAd.getBaseNativeAd());
                        }
                    }

                    @Override
                    public void onNativeFail(@NonNull final NativeErrorCode errorCode) {
                        synchronized (NativeAdManager.this.sequences) {
                            if (!NativeAdManager.this.sequences.isEmpty()) NativeAdManager.this.sequences.poll();
                        }

                        NativeAdManager.this.errors.onNext(errorCode);
                    }
                });

                moPubNative.registerAdRenderer(this.renderer);

                for (int i = 0; i < this.getPrefetchSize() - this.ads.size() - this.sequences.size(); i++) {
                    this.sequences.offer(this.sequence.incrementAndGet());

                    moPubNative.makeRequest(new RequestParameters.Builder()
                        .keywords(builder.toString())
                        .desiredAssets(this.getDesiredAssets())
                        .build());
                }
            }
        }
    }

    public T getAd() {
        synchronized (this.ads) {
            if (!this.ads.isEmpty()) this.ad = this.ads.poll();
        }

        this.prefetch();

        return this.ad;
    }

    @UiThread
    public abstract void renderAdView(@NonNull final View view);

    public void init() {
        if (this.isDisposed) this.isDisposed = false;
    }

    @Override
    public void dispose() {
        if (!this.isDisposed) {
            this.isDisposed = true;

            synchronized (this.ads) {
                for (final T ad : this.ads) ad.destroy();

                this.ads.clear();
            }

            synchronized (this.sequences) {
                this.sequences.clear();
            }
        }
    }
}
