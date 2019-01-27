package com.github.ayltai.newspaper.config;

import com.github.ayltai.newspaper.Constants;

public interface RemoteConfigs {
    @Constants.Theme
    int getTheme();

    @Constants.Style
    int getStyle();
}
