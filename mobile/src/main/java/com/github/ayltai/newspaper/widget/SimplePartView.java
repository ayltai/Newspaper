package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

public class SimplePartView extends PartView {
    public SimplePartView(@NonNull final Context context, @LayoutRes final int layoutId) {
        super(context);

        LayoutInflater.from(context).inflate(layoutId, this, true);
    }
}
