package com.github.ayltai.newspaper;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.ayltai.newspaper.util.TestUtils;

import jp.wasabeef.takt.Seat;
import jp.wasabeef.takt.Takt;

public abstract class BaseActivity extends AppCompatActivity {
    //region Variables

    private static int     referenceCount;
    private static boolean pendingCreation;

    //endregion

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!TestUtils.isRunningInstrumentalTest()) {
            if (BaseActivity.referenceCount > 0) {
                BaseActivity.stopTakt();

                BaseActivity.pendingCreation = true;
            } else {
                this.startTakt();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (!TestUtils.isRunningInstrumentalTest()) {
            if (BaseActivity.pendingCreation) {
                BaseActivity.pendingCreation = false;

                this.startTakt();
            } else {
                BaseActivity.stopTakt();
            }
        }

        super.onDestroy();
    }

    private void startTakt() {
        Takt.stock(this.getApplication()).seat(Seat.TOP_RIGHT).color(Color.WHITE).play();

        BaseActivity.referenceCount++;
    }

    private static void stopTakt() {
        try {
            Takt.finish();
        } catch (final IllegalArgumentException | NullPointerException e) {
            // Ignored
        }

        BaseActivity.referenceCount--;
    }
}
