package com.github.ayltai.newspaper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ayltai.newspaper.config.DaggerConfigsComponent;
import com.github.ayltai.newspaper.data.DataManager;
import com.github.ayltai.newspaper.data.SourceManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public final class SplashActivity extends AppCompatActivity {
    private Disposable disposable;

    @CallSuper
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(DaggerConfigsComponent.builder().build().userConfigs().getTheme() == Constants.THEME_DARK ? R.style.SplashTheme_Dark : R.style.SplashTheme_Light);

        this.disposable = SourceManager.create(this)
            .map(SourceManager::get)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(DataManager.SCHEDULER)
            .subscribe(irrelevant -> {
                this.startActivity(new Intent(this, MainActivity.class));
                this.finish();
            });
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();

        super.onDestroy();
    }
}
