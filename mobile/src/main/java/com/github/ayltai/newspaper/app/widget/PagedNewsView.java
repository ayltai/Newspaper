package com.github.ayltai.newspaper.app.widget;

import java.util.Collections;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.analytics.ClickEvent;
import com.github.ayltai.newspaper.analytics.SearchEvent;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.MainActivity;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.view.BaseNewsView;
import com.github.ayltai.newspaper.widget.BaseView;
import com.github.ayltai.newspaper.widget.ListView;
import com.jakewharton.rxbinding2.support.v4.view.RxViewPager;

public class PagedNewsView extends BaseView implements BaseNewsView {
    private UserConfig       userConfig;
    private ViewPager        viewPager;
    private PagedNewsAdapter adapter;

    public PagedNewsView(@NonNull final Context context) {
        super(context);
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        if (this.isFirstTimeAttachment) {
            this.adapter = new PagedNewsAdapter(this.getContext());

            final LifecycleOwner lifecycleOwner = this.getLifecycleOwner();
            if (lifecycleOwner != null) lifecycleOwner.getLifecycle().addObserver(this.adapter);

            this.viewPager.setAdapter(this.adapter);
        }

        this.manageDisposable(RxViewPager.pageSelections(this.viewPager).subscribe(index -> {
            this.adapter.setCurrentPosition(index);

            ComponentFactory.getInstance()
                .getAnalyticsComponent(this.getContext())
                .eventLogger()
                .logEvent(new ClickEvent()
                    .setElementName("Page Selection"));
        }));

        super.onAttachedToWindow();
    }

    //region Methods

    @Override
    public void up() {
        final ListView view = this.adapter.getItem(this.viewPager.getCurrentItem());
        if (view != null) view.up();
    }

    @Override
    public void refresh() {
        final ListView view = this.adapter.getItem(this.viewPager.getCurrentItem());
        if (view != null) view.refresh();
    }

    @Override
    public void clear() {
    }

    public void settings() {
        final OptionsView view = new OptionsView(this.getContext(), this.userConfig != null && this.userConfig.getTheme() == Constants.THEME_LIGHT ? R.style.AppDialogThemeLight : R.style.AppDialogThemeDark);

        this.manageDisposable(view.cancelClicks().subscribe(irrelevant -> view.dismiss()));

        this.manageDisposable(view.okClicks().subscribe(irrelevant -> {
            view.dismiss();

            final Activity activity = this.getActivity();
            if (activity != null) activity.finish();

            this.getContext().startActivity(new Intent(this.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }));

        view.show();
    }

    @Override
    public void search(@Nullable final CharSequence newText) {
        if (this.adapter != null) this.adapter.getFilter().filter(newText);

        if (!TextUtils.isEmpty(newText)) ComponentFactory.getInstance()
            .getAnalyticsComponent(this.getContext())
            .eventLogger()
            .logEvent(new SearchEvent()
                .setQuery(newText.toString())
                .setCategory((this.userConfig == null ? Collections.<String>emptyList() : this.userConfig.getCategories()).get(this.viewPager.getCurrentItem()))
                .setScreenName(this.getClass().getSimpleName()));

    }

    @Override
    protected void init() {
        super.init();

        final Activity activity = this.getActivity();
        if (activity != null) this.userConfig = ComponentFactory.getInstance()
            .getConfigComponent(activity)
            .userConfig();

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_news_pager, this, true);

        this.viewPager = view.findViewById(R.id.viewPager);

        final TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(this.viewPager, true);
    }

    //endregion
}
