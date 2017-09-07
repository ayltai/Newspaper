package com.github.ayltai.newspaper;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public class LifecycleActivity extends AppCompatActivity implements LifecycleRegistryOwner {
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return this.lifecycleRegistry;
    }
}
