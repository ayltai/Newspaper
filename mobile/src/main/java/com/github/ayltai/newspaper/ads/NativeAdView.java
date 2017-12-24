package com.github.ayltai.newspaper.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.ayltai.newspaper.widget.BaseView;

public class NativeAdView extends BaseView implements NativeAdPresenter.View {
    public NativeAdView(@NonNull final Context context) {
        super(context);

        this.init();
    }

    @Override
    public void show() {
        this.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        this.setVisibility(View.GONE);
    }
}
