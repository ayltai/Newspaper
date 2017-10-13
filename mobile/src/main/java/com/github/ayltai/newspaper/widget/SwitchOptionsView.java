package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.view.OptionsPresenter;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class SwitchOptionsView extends ObservableView implements OptionsPresenter.View {
    private final FlowableProcessor<Integer> optionChanges = PublishProcessor.create();

    private ViewGroup container;

    public SwitchOptionsView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    @Override
    public void addOption(@NonNull final CharSequence text, final boolean selected) {
        final SwitchCompat view = (SwitchCompat)LayoutInflater.from(this.getContext()).inflate(R.layout.view_switch_option, this.container, false);
        view.setText(text);
        view.setChecked(selected);

        this.subscribeToView(view);

        this.container.addView(view);
    }

    @NonNull
    @Override
    public Flowable<Integer> optionsChanges() {
        return this.optionChanges;
    }

    protected void onSelect(@NonNull final SwitchCompat view) {
    }

    @Override
    protected void onAttachedToWindow() {
        for (int i = 0; i < this.container.getChildCount(); i++) this.subscribeToView((SwitchCompat)this.container.getChildAt(i));

        super.onAttachedToWindow();
    }

    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_linear_layout, this, true);
        this.container = view.findViewById(R.id.linearLayout);
    }

    private void subscribeToView(@NonNull final SwitchCompat view) {
        this.manageDisposable(RxCompoundButton.checkedChanges(view).subscribe(selected -> {
            this.onSelect(view);

            this.optionChanges.onNext(this.container.indexOfChild(view));
        }));
    }
}
