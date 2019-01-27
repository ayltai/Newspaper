package com.github.ayltai.newspaper;

import androidx.annotation.IntDef;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public static final int THEME_LIGHT       = 0;
    public static final int THEME_DARK        = 1;
    public static final int STYLE_COMFORTABLE = 0;
    public static final int STYLE_COMPACT     = 1;

    @IntDef({ Constants.THEME_LIGHT, Constants.THEME_DARK })
    public @interface Theme {}

    @IntDef({ Constants.STYLE_COMFORTABLE, Constants.STYLE_COMPACT })
    public @interface Style {}
}
