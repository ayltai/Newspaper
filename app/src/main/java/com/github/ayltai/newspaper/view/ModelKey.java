package com.github.ayltai.newspaper.view;

import android.os.Parcelable;

import flow.ClassKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ModelKey<M> extends ClassKey implements Parcelable {
    public abstract M getModel();
}
