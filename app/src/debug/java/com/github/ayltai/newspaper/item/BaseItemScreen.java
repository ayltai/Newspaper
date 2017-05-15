package com.github.ayltai.newspaper.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

public abstract class BaseItemScreen extends FrameLayout {
    public BaseItemScreen(@NonNull final Context context) {
        super(context);
    }

    protected void trackStartVideoPlayback() {
    }

    protected void trackFullscreenVideoPlayback() {
    }
}
