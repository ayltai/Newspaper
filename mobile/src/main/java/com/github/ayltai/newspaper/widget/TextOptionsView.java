package com.github.ayltai.newspaper.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.view.OptionsPresenter;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class TextOptionsView extends BaseView implements OptionsPresenter.View {
    private final FlowableProcessor<Integer> optionChanges = PublishProcessor.create();

    private ViewGroup container;

    public TextOptionsView(@NonNull final Context context) {
        super(context);

        this.init();
    }

    @Override
    public void addOption(@NonNull final CharSequence text, final boolean selected) {
        final TextView view = (TextView)LayoutInflater.from(this.getContext()).inflate(R.layout.view_text_option, this.container, false);
        view.setText(text);
        view.setSelected(selected);

        this.subscribeToView(view);

        this.container.addView(view);
    }

    @NonNull
    @Override
    public Flowable<Integer> optionsChanges() {
        return this.optionChanges;
    }

    @Override
    public void onAttachedToWindow() {
        for (int i = 0; i < this.container.getChildCount(); i++) this.subscribeToView(this.container.getChildAt(i));

        super.onAttachedToWindow();
    }

    @Override
    protected void init() {
        super.init();

        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.widget_flow_layout, this, true);
        this.container = view.findViewById(R.id.flowLayout);
    }

    private void subscribeToView(@NonNull final View view) {
        view.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());

            this.optionChanges.onNext(this.container.indexOfChild(v));
        });
    }
}
