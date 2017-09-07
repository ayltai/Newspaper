package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.github.ayltai.newspaper.util.ViewUtils;

public class PartView extends FrameLayout {
    public PartView(@NonNull final Context context) {
        super(context);

        this.setLayoutParams(ViewUtils.createWrapContentLayoutParams());
    }
}
