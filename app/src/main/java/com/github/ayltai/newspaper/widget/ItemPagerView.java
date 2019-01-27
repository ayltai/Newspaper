package com.github.ayltai.newspaper.widget;

import java.util.List;

import javax.annotation.Nonnull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.view.ItemPagerPresenter;
import com.google.android.material.tabs.TabLayout;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class ItemPagerView extends BaseView implements ItemPagerPresenter.View, TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
    private final FlowableProcessor<Irrelevant> refreshActions  = PublishProcessor.create();
    private final FlowableProcessor<String>     categoryChanges = PublishProcessor.create();

    private ProgressBar progressBar;
    private ViewGroup   container;
    private TabLayout   tabLayout;
    private ViewPager   viewPager;

    public ItemPagerView(@Nonnull @NonNull final @lombok.NonNull Context context) {
        super(context);

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_item_pager, this, false);

        this.progressBar = view.findViewById(R.id.progressBar);
        this.container   = view.findViewById(R.id.container);
        this.tabLayout   = view.findViewById(R.id.tabLayout);
        this.viewPager   = view.findViewById(R.id.viewPager);

        this.addView(view);
        this.updateLayout(BaseView.LAYOUT_SCREEN);
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<Irrelevant> refreshActions() {
        return this.refreshActions;
    }

    @Nonnull
    @NonNull
    @Override
    public Flowable<String> categoryChanges() {
        return this.categoryChanges;
    }

    @Override
    public void refresh() {
        //
    }

    @Override
    public void setCategories(@Nonnull @NonNull @lombok.NonNull final List<String> categories) {
        for (final String category : categories) this.tabLayout.addTab(this.tabLayout.newTab().setText(category));

        this.viewPager.setAdapter(new ItemPagerAdapter(categories));
        this.tabLayout.setupWithViewPager(this.viewPager);

        this.container.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.tabLayout.addOnTabSelectedListener(this);
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        this.tabLayout.removeOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(@Nonnull @NonNull @lombok.NonNull final TabLayout.Tab tab) {
        this.categoryChanges.onNext(tab.getText().toString());
    }

    @Override
    public void onTabUnselected(@Nonnull @NonNull @lombok.NonNull final TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(@Nonnull @NonNull @lombok.NonNull final TabLayout.Tab tab) {
    }
}
