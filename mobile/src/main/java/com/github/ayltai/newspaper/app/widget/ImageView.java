package com.github.ayltai.newspaper.app.widget;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.widget.ItemView;
import com.github.ayltai.newspaper.data.model.Image;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.piasy.biv.view.BigImageView;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class ImageView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_item_image;

    private final FlowableProcessor<Irrelevant> clicks = PublishProcessor.create();

    //region Components

    private final View         container;
    private final BigImageView image;

    //endregion

    private Disposable disposable;

    public ImageView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_cozy_image, this, true);

        this.container = view.findViewById(R.id.container);
        this.image     = view.findViewById(R.id.image);
    }

    @Override
    public void setImages(@NonNull final List<Image> images) {
        if (images.isEmpty()) {
            this.image.setVisibility(View.GONE);
        } else {
            this.image.setVisibility(View.VISIBLE);
            this.image.showImage(Uri.parse(images.get(0).getUrl()));
        }
    }

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
