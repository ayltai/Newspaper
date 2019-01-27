package com.github.ayltai.newspaper.util;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    @Nonnull
    @NonNull
    public String join(@Nonnull @NonNull @lombok.NonNull final Iterable<String> tokens, @Nonnull @NonNull @lombok.NonNull final String delimiter) {
        final StringBuilder builder = new StringBuilder();

        for (final String token : tokens) {
            if (builder.length() > 0) builder.append(delimiter);
            builder.append(token);
        }

        return builder.toString();
    }
}
