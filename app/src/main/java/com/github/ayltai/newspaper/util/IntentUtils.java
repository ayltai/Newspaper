package com.github.ayltai.newspaper.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.R;

public final class IntentUtils {
    private IntentUtils() {
    }

    public static void share(@NonNull final Context context, @NonNull final String text) {
        context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain"), context.getText(R.string.share_to)));
    }
}
