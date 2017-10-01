package com.github.ayltai.newspaper.app.widget;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Image;
import com.github.piasy.biv.view.BigImageView;

public final class ContentView extends ItemView {
    public static final int VIEW_TYPE = R.id.view_type_item_content;

    //region Components

    private final BigImageView image;
    private final TextView     title;
    private final TextView     description;

    //endregion

    public ContentView(@NonNull final Context context) {
        super(context);

        final View view = LayoutInflater.from(context).inflate(R.layout.view_news_compact_content, this, true);

        this.container   = view.findViewById(R.id.container);
        this.image       = view.findViewById(R.id.image);
        this.title       = view.findViewById(R.id.title);
        this.description = view.findViewById(R.id.description);

        this.image.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        this.image.getSSIV().setPanEnabled(false);
        this.image.getSSIV().setZoomEnabled(false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setTitle(@Nullable final CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(View.GONE);
        } else {
            this.title.setVisibility(View.VISIBLE);
            this.title.setText(Html.fromHtml(title.toString()));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDescription(@Nullable final CharSequence description) {
        if (TextUtils.isEmpty(description)) {
            this.description.setVisibility(View.GONE);
            this.title.setMaxLines(2);
        } else {
            this.description.setVisibility(View.VISIBLE);
            this.description.setText(Html.fromHtml(description.toString()));
            this.title.setMaxLines(1);
        }
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

    @CallSuper
    @Override
    protected void onAttachedToWindow() {
        this.image.getSSIV().setImage(ImageSource.resource(R.drawable.thumbnail_placeholder));

        super.onAttachedToWindow();
    }
}
