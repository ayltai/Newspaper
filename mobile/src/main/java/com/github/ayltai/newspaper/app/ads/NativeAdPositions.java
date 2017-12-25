package com.github.ayltai.newspaper.app.ads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public final class NativeAdPositions {
    private static final String COMMA      = ",";
    private static final String SEMI_COLON = ";";

    private final List<Integer> initialPositions = new ArrayList<>();
    private final int           interval;

    public NativeAdPositions(@Nullable final String positions) {
        if (TextUtils.isEmpty(positions)) {
            this.interval = 0;
        } else {
            if (positions.contains(NativeAdPositions.SEMI_COLON)) {
                final String[] tokens = positions.split(NativeAdPositions.SEMI_COLON);
                this.interval = Integer.parseInt(tokens[1]);

                if (tokens[0].contains(NativeAdPositions.COMMA)) {
                    final String[] initialPositions = tokens[0].split(NativeAdPositions.COMMA);
                    for (final String initialPosition : initialPositions) this.initialPositions.add(Integer.parseInt(initialPosition));
                } else {
                    this.initialPositions.add(Integer.parseInt(tokens[0]));
                }
            } else {
                this.interval = Integer.parseInt(positions);
            }
        }

        Collections.sort(this.initialPositions);
    }

    public boolean isValid(final int position) {
        if (this.initialPositions.isEmpty()) return this.interval != 0 && position % this.interval == 0;

        final int lastInitialPosition = this.initialPositions.get(this.initialPositions.size() - 1);

        if (position <= lastInitialPosition) return this.initialPositions.contains(position);

        return (position - lastInitialPosition) % this.interval == 0;
    }
}
