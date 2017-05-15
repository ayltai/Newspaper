package com.github.ayltai.newspaper.item;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.ayltai.newspaper.Constants;

public abstract class BaseItemScreen extends FrameLayout {
    public BaseItemScreen(@NonNull final Context context) {
        super(context);
    }

    protected void trackStartVideoPlayback() {
        Answers.getInstance().logCustom(new CustomEvent(Constants.ANALYTICS_START_VIDEO_PLAYBACK);
        FirebaseAnalytics.getInstance(this.getContext()).logEvent(Constants.ANALYTICS_START_VIDEO_PLAYBACK, new Bundle());
    }

    protected void trackFullscreenVideoPlayback() {
        Answers.getInstance().logCustom(new CustomEvent(Constants.ANALYTICS_FULLSCREEN_VIDEO_PLAYBACK);
        FirebaseAnalytics.getInstance(this.getContext()).logEvent(Constants.ANALYTICS_FULLSCREEN_VIDEO_PLAYBACK, new Bundle());
    }
}
