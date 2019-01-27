package com.github.ayltai.newspaper.config;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import com.github.ayltai.newspaper.Constants;

public interface UserConfigs {
    int getConnectTimeout();

    int getReadTimeout();

    int getWriteTimeout();

    @Constants.Theme
    int getTheme();

    void setTheme(@Constants.Theme int theme);

    @Constants.Style
    int getStyle();

    void setStyle(@Constants.Style int style);

    @Nonnull
    @NonNull
    List<String> getSourceNames();

    void setSourceNames(@Nonnull @NonNull List<String> sourceNames);

    @Nonnull
    @NonNull
    List<String> getCategoryNames();

    void setCategoryNames(@Nonnull @NonNull List<String> categoryNames);
}
