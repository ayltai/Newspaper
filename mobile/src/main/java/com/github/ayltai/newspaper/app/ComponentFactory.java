package com.github.ayltai.newspaper.app;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.analytics.AnalyticsComponent;
import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.app.config.ConfigComponent;
import com.github.ayltai.newspaper.app.config.ConfigModule;
import com.github.ayltai.newspaper.app.config.DaggerConfigComponent;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;

public final class ComponentFactory {
    private static ComponentFactory instance;

    private ConfigComponent    configComponent;
    private DataComponent      dataComponent;
    private ImageComponent     imageComponent;
    private AnalyticsComponent analyticsComponent;

    public static ComponentFactory getInstance() {
        if (ComponentFactory.instance == null) ComponentFactory.init();

        return ComponentFactory.instance;
    }

    public static void init() {
        ComponentFactory.instance = new ComponentFactory();
    }

    public ConfigComponent getConfigComponent(@NonNull final Activity activity) {
        if (this.configComponent == null) this.configComponent = DaggerConfigComponent.builder()
            .configModule(new ConfigModule(activity))
            .build();

        return this.configComponent;
    }

    public DataComponent getDataComponent(@NonNull final Context context) {
        if (this.dataComponent == null) this.dataComponent = DaggerDataComponent.builder()
            .dataModule(new DataModule(context))
            .build();

        return this.dataComponent;
    }

    public ImageComponent getImageComponent(@NonNull final Context context) {
        if (this.imageComponent == null) this.imageComponent = DaggerImageComponent.builder()
            .imageModule(new ImageModule(context))
            .build();

        return this.imageComponent;
    }

    public AnalyticsComponent getAnalyticsComponent(@NonNull final Context context) {
        if (this.analyticsComponent == null) this.analyticsComponent = DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(context))
            .build();

        return this.analyticsComponent;
    }
}
