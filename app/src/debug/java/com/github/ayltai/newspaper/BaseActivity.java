package com.github.ayltai.newspaper;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.util.TestUtils;

import jp.wasabeef.takt.Seat;
import jp.wasabeef.takt.Takt;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TestUtils.isRunningTest()) Takt.stock(this.getApplication()).seat(Seat.TOP_RIGHT).color(Color.WHITE).play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!TestUtils.isRunningTest()) {
            try {
                Takt.finish();
            } catch (final IllegalArgumentException e) {
                // Ignored
            }
        }
    }
}
