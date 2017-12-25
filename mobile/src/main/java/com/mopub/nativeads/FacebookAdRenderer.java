package com.mopub.nativeads;

import java.util.WeakHashMap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.MediaView;
import com.mopub.common.Preconditions;

public final class FacebookAdRenderer implements MoPubAdRenderer<FacebookVideoEnabledNativeAd> {
    private final WeakHashMap<View, ViewHolder> viewHolders = new WeakHashMap<>();
    private final ViewBinder                    viewBinder;

    public FacebookAdRenderer(@NonNull final ViewBinder viewBinder) {
        this.viewBinder = viewBinder;
    }

    @NonNull
    @Override
    public View createAdView(@NonNull final Context context, @Nullable final ViewGroup parent) {
        final View adView = LayoutInflater.from(context).inflate(this.viewBinder.layoutId, parent, false);

        final View mainImage = adView.findViewById(this.viewBinder.mainImageId);
        if (mainImage == null) return adView;

        final ViewGroup.LayoutParams mainImageParams = mainImage.getLayoutParams();
        final MediaView.LayoutParams mediaViewParams = new MediaView.LayoutParams(mainImageParams.width, mainImageParams.height);

        if (mainImageParams instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mainImageParams;
            mediaViewParams.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin);
        }

        if (mainImageParams instanceof RelativeLayout.LayoutParams) {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mainImageParams;
            final int[]                       rules  = params.getRules();

            for (int i = 0; i < rules.length; i++) mediaViewParams.addRule(i, rules[i]);

            mainImage.setVisibility(View.INVISIBLE);
        } else {
            mainImage.setVisibility(View.GONE);
        }

        final ViewGroup mainImageParent = (ViewGroup)mainImage.getParent();
        mainImageParent.addView(new MediaView(context), mainImageParent.indexOfChild(mainImage) + 1, mediaViewParams);

        return adView;
    }

    @Override
    public void renderAdView(@NonNull final View view, @NonNull final FacebookVideoEnabledNativeAd nativeAd) {
        ViewHolder viewHolder = viewHolders.get(view);
        if (viewHolder == null) {
            viewHolder = ViewHolder.fromViewBinder(view, this.viewBinder);
            this.viewHolders.put(view, viewHolder);
        }

        this.update(viewHolder, nativeAd);
        NativeRendererHelper.updateExtras(viewHolder.getMainView(), this.viewBinder.extras, nativeAd.getExtras());

        if (viewHolder.getMainView() != null) viewHolder.getMainView().setVisibility(View.VISIBLE);
    }

    @Override
    public boolean supports(@NonNull final BaseNativeAd nativeAd) {
        Preconditions.checkNotNull(nativeAd);
        return nativeAd instanceof FacebookVideoEnabledNativeAd;
    }

    private void update(@NonNull final ViewHolder viewHolder, @NonNull final FacebookVideoEnabledNativeAd nativeAd) {
        final ImageView mainImageView = viewHolder.getMainImageView();

        NativeRendererHelper.addTextView(viewHolder.getTitleView(), nativeAd.getTitle());
        NativeRendererHelper.addTextView(viewHolder.getTextView(), nativeAd.getText());
        NativeRendererHelper.addTextView(viewHolder.getCallToActionView(), nativeAd.getCallToAction());
        NativeImageHelper.loadImageView(nativeAd.getMainImageUrl(), mainImageView);
        NativeImageHelper.loadImageView(nativeAd.getIconImageUrl(), viewHolder.getIconImageView());
        NativeRendererHelper.addPrivacyInformationIcon(viewHolder.getPrivacyInformationIconImageView(), nativeAd.getPrivacyInformationIconImageUrl(), nativeAd.getPrivacyInformationIconClickThroughUrl());

        final MediaView mediaView = viewHolder.mediaView;
        if (mediaView != null && mainImageView != null) {
            nativeAd.updateMediaView(mediaView);

            mediaView.setVisibility(View.VISIBLE);
            mainImageView.setVisibility(viewHolder.isMainImageViewInRelativeView ? View.INVISIBLE : View.GONE);
        }
    }

    private static final class ViewHolder {
        private final StaticNativeViewHolder viewHolder;
        private final MediaView              mediaView;
        private final boolean                isMainImageViewInRelativeView;

        private ViewHolder(final StaticNativeViewHolder staticNativeViewHolder, final MediaView mediaView, final boolean mainImageViewInRelativeView) {
            this.viewHolder                    = staticNativeViewHolder;
            this.mediaView                     = mediaView;
            this.isMainImageViewInRelativeView = mainImageViewInRelativeView;
        }

        private static ViewHolder fromViewBinder(final View view, final ViewBinder viewBinder) {
            final StaticNativeViewHolder viewHolder = StaticNativeViewHolder.fromViewBinder(view, viewBinder);
            final View                   mainImage  = viewHolder.mainImageView;

            boolean   mainImageViewInRelativeView = false;
            MediaView mediaView                   = null;

            if (mainImage != null) {
                final ViewGroup mainImageParent = (ViewGroup)mainImage.getParent();
                if (mainImageParent instanceof RelativeLayout) mainImageViewInRelativeView = true;

                final View viewAfterImageView = mainImageParent.getChildAt(mainImageParent.indexOfChild(mainImage) + 1);
                if (viewAfterImageView instanceof MediaView) mediaView = (MediaView)viewAfterImageView;
            }

            return new ViewHolder(viewHolder, mediaView, mainImageViewInRelativeView);
        }

        private View getMainView() {
            return this.viewHolder.mainView;
        }

        private TextView getTitleView() {
            return this.viewHolder.titleView;
        }

        private TextView getTextView() {
            return this.viewHolder.textView;
        }

        private TextView getCallToActionView() {
            return this.viewHolder.callToActionView;
        }

        private ImageView getMainImageView() {
            return this.viewHolder.mainImageView;
        }

        private ImageView getIconImageView() {
            return this.viewHolder.iconImageView;
        }

        private ImageView getPrivacyInformationIconImageView() {
            return this.viewHolder.privacyInformationIconImageView;
        }
    }
}
