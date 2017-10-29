package com.github.ayltai.newspaper.app.view;

import java.util.concurrent.TimeUnit;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.app.data.model.FeaturedItem;
import com.github.ayltai.newspaper.app.widget.FeaturedView;
import com.github.ayltai.newspaper.media.FrescoImageLoader;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import io.reactivex.Observable;

public class FeaturedPresenter extends ItemPresenter<FeaturedView> {
    @Override
    public void onViewAttached(@NonNull final FeaturedView view, final boolean isFirstTimeAttachment) {
        this.manageDisposable(Observable.interval(Constants.FEATURED_IMAGE_ROTATION, TimeUnit.SECONDS)
            .compose(RxUtils.applyObservableBackgroundToMainSchedulers())
            .subscribe(time -> {
                if (this.getModel() instanceof FeaturedItem) {
                    ((FeaturedItem)this.getModel()).next();

                    if (!this.getModel().getImages().isEmpty()) FrescoImageLoader.loadImage(this.getModel().getImages().get(0).getUrl())
                        .compose(RxUtils.applyMaybeBackgroundToMainSchedulers())
                        .subscribe(
                            bitmap -> {
                                view.setImages(this.getModel().getImages());
                                view.setTitle(this.getModel().getTitle());
                            },
                            error -> {
                                if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), error);
                            }
                        );

                    this.bindModel(this.getModel());
                }
            }));

        super.onViewAttached(view, isFirstTimeAttachment);
    }
}
