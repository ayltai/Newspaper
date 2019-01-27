package com.github.ayltai.newspaper.widget;

import javax.annotation.Nonnull;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.config.DaggerConfigsComponent;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class RoundedBottomSheetDialog extends BottomSheetDialog {
    public RoundedBottomSheetDialog(@Nonnull @NonNull @lombok.NonNull final Context context) {
        super(context, DaggerConfigsComponent.builder().build().userConfigs().getTheme() == Constants.THEME_DARK ? R.style.BottomSheetDialogThemeDark : R.style.BottomSheetDialogThemeLight);
    }
}
