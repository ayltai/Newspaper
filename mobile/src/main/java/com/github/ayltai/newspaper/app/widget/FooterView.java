package com.github.ayltai.newspaper.app.widget;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class FooterView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_item_footer;

    private final FlowableProcessor<Irrelevant> clicks = PublishProcessor.create();

    //region Components

    private final View     container;
    private final TextView title;
    private final TextView description;

    //endregion

    private Disposable disposable;

    public FooterView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_cozy_footer, this, true);

        this.container   = view.findViewById(R.id.container);
        this.title       = view.findViewById(R.id.title);
        this.description = view.findViewById(R.id.description);
    }

    //region Properties

    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(title);
        }
    }

    @Override
    public void setDescription(@Nullable final CharSequence description) {
        if (TextUtils.isEmpty(description)) {
            this.description.setVisibility(View.GONE);
        } else {
            this.description.setVisibility(View.VISIBLE);
            this.description.setText(description);
        }
    }

    //endregion

    @NonNull
    @Override
    public Flowable<Irrelevant> clicks() {
        return this.clicks;
    }

    //region Lifecycle

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        this.disposable = RxView.clicks(this.container).subscribe(irrelevant -> this.clicks.onNext(Irrelevant.INSTANCE));

        super.onAttachedToWindow();
    }

    @CallSuper
    @Override
    protected void onDetachedFromWindow() {
        if (this.disposable != null && this.disposable.isDisposed()) {
            this.disposable.dispose();
            this.disposable = null;
        }

        super.onDetachedFromWindow();
    }

    //endregion
}
