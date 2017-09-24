package com.github.ayltai.newspaper.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.view.TextOptionsPresenter;
import com.jakewharton.rxbinding2.view.RxView;
import com.nex3z.flowlayout.FlowLayout;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class TextOptionsView extends ObservableView implements TextOptionsPresenter.View {
    private final FlowableProcessor<Integer> selects = PublishProcessor.create();

    private FlowLayout flowLayout;

    //region Constructors

    public TextOptionsView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public TextOptionsView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public TextOptionsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public TextOptionsView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion

    @Override
    public void addText(@NonNull final CharSequence text, final boolean selected) {
        final TextView view = (TextView)LayoutInflater.from(this.getContext()).inflate(R.layout.view_text_option, this.flowLayout, false);
        view.setText(text);
        view.setSelected(selected);

        this.subscribeToView(view);

        this.flowLayout.addView(view);
    }

    @NonNull
    protected List<View> getTextOptions() {
        final List<View> views = new ArrayList<>(this.flowLayout.getChildCount());

        for (int i = 0; i < this.flowLayout.getChildCount(); i++) views.add(this.flowLayout.getChildAt(i));

        return views;
    }

    @NonNull
    @Override
    public Flowable<Integer> selects() {
        return this.selects;
    }

    protected void onSelect(@NonNull final View view) {
    }

    @Override
    protected void onAttachedToWindow() {
        for (int i = 0; i < this.flowLayout.getChildCount(); i++) this.subscribeToView(this.flowLayout.getChildAt(i));

        super.onAttachedToWindow();
    }

    private void init() {
        final View view = LayoutInflater.from(this.getContext()).inflate(R.layout.view_flow_layout, this, true);
        this.flowLayout = view.findViewById(R.id.flowLayout);
    }

    private void subscribeToView(@NonNull final View view) {
        this.manageDisposable(RxView.clicks(view).subscribe(irrelevant -> {
            this.onSelect(view);

            this.selects.onNext(this.flowLayout.indexOfChild(view));
        }));
    }
}
