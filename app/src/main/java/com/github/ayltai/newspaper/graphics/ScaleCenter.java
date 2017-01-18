package com.github.ayltai.newspaper.graphics;

import android.graphics.PointF;
import android.support.annotation.NonNull;

public final class ScaleCenter {
    public final float  scale;
    public final PointF center;

    public ScaleCenter(final float scale, @NonNull final PointF center) {
        this.scale  = scale;
        this.center = center;
    }
}
