package com.github.ayltai.newspaper.view;

import javax.annotation.Nonnull;

import android.graphics.Point;

import androidx.annotation.NonNull;

public interface Locatable {
    @Nonnull
    @NonNull
    Point getLocation();
}
