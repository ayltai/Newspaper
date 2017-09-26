package com.github.ayltai.newspaper.app.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.R;

public final class OptionsView extends BottomSheetDialog {
    //region Constructors

    public OptionsView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public OptionsView(@NonNull final Context context, @StyleRes final int theme) {
        super(context, theme);
        this.init();
    }

    public OptionsView(@NonNull final Context context, final boolean cancelable, final OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.init();
    }

    //endregion

    @SuppressLint("InflateParams")
    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_options, null);

        final ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new OptionsAdapter(this.getContext()));

        final TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        this.setContentView(view);
    }
}
