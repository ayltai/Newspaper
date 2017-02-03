package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.setting.Settings;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

public final class ContextUtils {
    private ContextUtils() {
    }

    @Nullable
    public static Activity getActivity(@NonNull final Context context) {
        if (context instanceof Activity) return (Activity)context;
        if (context instanceof ContextWrapper) return getActivity(((ContextWrapper)context).getBaseContext());

        return null;
    }

    public static int getColor(@NonNull final Context context, @AttrRes final int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);

        return value.data;
    }

    public static int getResourceId(@NonNull final Context context, @AttrRes final int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);

        return value.resourceId;
    }

    public static void setAppTheme(@NonNull final Context context) {
        context.setTheme(Settings.isDarkTheme(context) ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }

    public static void setSettingsTheme(@NonNull final Context context) {
        context.setTheme(Settings.isDarkTheme(context) ? R.style.SettingsTheme_Dark : R.style.SettingsTheme_Light);
    }

    @SuppressFBWarnings({"NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS"})
    public static void showAbout(@NonNull final Context context) {
        new MaterialStyledDialog.Builder(context)
            .setStyle(Style.HEADER_WITH_ICON)
            .setHeaderColor(ContextUtils.getResourceId(context, R.attr.primaryColor))
            .setIcon(R.mipmap.ic_launcher)
            .setTitle(R.string.app_name)
            .setDescription(String.format(context.getString(R.string.app_version), BuildConfig.VERSION_NAME))
            .setPositiveText(android.R.string.ok)
            .setNegativeText(R.string.rate_app)
            .onNegative((dialog, which) -> {
                final String name = context.getPackageName();

                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + name)));
                } catch (final ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + name)));
                }
            })
            .withIconAnimation(true)
            .withDialogAnimation(true, Duration.NORMAL)
            .withDivider(true)
            .show();
    }
}
