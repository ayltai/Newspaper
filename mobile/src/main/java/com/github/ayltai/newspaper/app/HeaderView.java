package com.github.ayltai.newspaper.app;

import java.util.Date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.util.DateUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class HeaderView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_item_header;

    private final FlowableProcessor<Irrelevant> clicks = PublishProcessor.create();

    //region Components

    private final View             container;
    private final SimpleDraweeView avatar;
    private final TextView         source;
    private final TextView         publishDate;

    //endregion

    private Disposable disposable;

    public HeaderView(@NonNull final Context context) {
        super(context);

        this.container   = LayoutInflater.from(context).inflate(R.layout.view_news_cozy_header, this, true);
        this.avatar      = this.container.findViewById(R.id.avatar);
        this.source      = this.container.findViewById(R.id.source);
        this.publishDate = this.container.findViewById(R.id.publish_date);
    }

    //region Properties

    @Override
    public void setAvatar(@Nullable final String avatarUri) {
        if (TextUtils.isEmpty(avatarUri)) {
            this.avatar.setImageResource(0); // TODO
        } else {
            this.avatar.setImageURI(avatarUri);
        }
    }

    @Override
    public void setSource(@Nullable final CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            this.source.setVisibility(View.GONE);
        } else {
            this.source.setVisibility(View.VISIBLE);
            this.source.setText(source);
        }
    }

    @Override
    public void setPublishDate(@Nullable final Date date) {
        if (date == null) {
            this.publishDate.setVisibility(View.GONE);
        } else {
            this.publishDate.setVisibility(View.VISIBLE);
            this.publishDate.setText(DateUtils.getTimeAgo(this.getContext(), date.getTime()));
        }
    }

    //endregion

    @Nullable
    @Override
    public Flowable<Irrelevant> clicks() {
        return this.clicks;
    }

    @Override
    protected void onAttachedToWindow() {
        this.disposable = RxView.clicks(this.container).subscribe(irrelevant -> this.clicks.onNext(Irrelevant.INSTANCE));

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this.disposable != null && this.disposable.isDisposed()) {
            this.disposable.dispose();
            this.disposable = null;
        }

        super.onDetachedFromWindow();
    }
}
