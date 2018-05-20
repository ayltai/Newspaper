package com.github.ayltai.newspaper.app.widget;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.ComponentFactory;
import com.github.ayltai.newspaper.app.VideoActivity;
import com.github.ayltai.newspaper.app.config.AppConfig;
import com.github.ayltai.newspaper.app.config.ConfigComponent;
import com.github.ayltai.newspaper.app.config.UserConfig;
import com.github.ayltai.newspaper.app.data.model.Video;
import com.github.ayltai.newspaper.app.view.ItemPresenter;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.DeviceUtils;
import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.piasy.biv.view.BigImageView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class VideoView extends ItemView implements ItemPresenter.View, LifecycleObserver {
    private final FlowableProcessor<Irrelevant> videoClicks = PublishProcessor.create();

    //region Components

    @VisibleForTesting
    protected final View         playAction;
    @VisibleForTesting
    protected final BigImageView thumbnail;

    private final View         thumbnailContainer;

    private View            fullScreenAction;
    private PlayerView      playerView;
    private SimpleExoPlayer player;

    //endregion

    //region Configurations

    @Nullable private AppConfig  appConfig;
    @Nullable private UserConfig userConfig;

    //endregion

    private Video video;

    public VideoView(@NonNull final Context context) {
        super(context);

        final Activity activity = this.getActivity();
        if (activity != null) {
            final ConfigComponent component = ComponentFactory.getInstance().getConfigComponent(activity);

            this.appConfig  = component.appConfig();
            this.userConfig = component.userConfig();
        }

        this.thumbnailContainer = LayoutInflater.from(context).inflate(R.layout.widget_video_thumbnail, this, false);
        this.playAction         = this.thumbnailContainer.findViewById(R.id.play);

        this.thumbnail = this.thumbnailContainer.findViewById(R.id.image);
        this.thumbnail.getSSIV().setMaxScale(Constants.IMAGE_ZOOM_MAX);
        this.thumbnail.getSSIV().setPanEnabled(false);
        this.thumbnail.getSSIV().setZoomEnabled(false);

        this.addView(this.thumbnailContainer);
    }

    @Override
    public void setVideo(@Nullable final Video video) {
        this.video = video;

        if (video != null) {
            this.setUpThumbnail();
            this.setUpPlayer();
        }
    }

    @Nullable
    @Override
    public Flowable<Irrelevant> videoClicks() {
        return this.videoClicks;
    }

    //region Methods

    public void setUpThumbnail() {
        if (!DevUtils.isRunningUnitTest() && !TextUtils.isEmpty(this.video.getThumbnailUrl())) this.thumbnail.showImage(Uri.parse(this.video.getThumbnailUrl()));
    }

    public void setUpPlayer() {
        if (!VideoView.isYouTubeUrl(this.video.getVideoUrl())) {
            this.playerView = (PlayerView)LayoutInflater.from(this.getContext()).inflate(R.layout.widget_video_player, this, false);
            this.player     = ExoPlayerFactory.newSimpleInstance(this.getContext(), new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(null)));

            this.playerView.setPlayer(this.player);

            this.fullScreenAction = this.playerView.findViewById(R.id.exo_fullscreen);
            this.fullScreenAction.setVisibility(View.VISIBLE);

            final View fullScreenExitAction = this.playerView.findViewById(R.id.exo_fullscreen_exit);
            fullScreenExitAction.setVisibility(View.GONE);

            this.player.prepare(new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this.getContext(), null, new OkHttpDataSourceFactory(ComponentFactory.getInstance().getHttpComponent().httpClient(), Util.getUserAgent(this.getContext(), BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME), null))).createMediaSource(Uri.parse(this.video.getVideoUrl())));

            final Point                  size   = DeviceUtils.getScreenSize(this.getContext());
            final ViewGroup.LayoutParams params = this.playerView.getLayoutParams();
            params.width  = size.x - 2 * this.getContext().getResources().getDimensionPixelSize(R.dimen.space16);
            params.height = (int)Math.round(size.x / Constants.VIDEO_ASPECT_RATIO);
            this.playerView.setLayoutParams(params);

            this.addView(this.playerView);
            this.bringChildToFront(this.thumbnailContainer);

            if (this.userConfig != null && this.userConfig.isAutoPlayEnabled() || this.appConfig != null && this.appConfig.isVideoPlaying()) this.startPlayer();
        }
    }

    public void startPlayer() {
        if (VideoView.isYouTubeUrl(this.video.getVideoUrl())) {
            this.getContext().startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(this.video.getVideoUrl())), this.getContext().getText(R.string.view_via)));
        } else {
            if (this.playerView != null) {
                this.playerView.setVisibility(View.VISIBLE);
                this.playerView.findViewById(R.id.exo_playback_control_view).setVisibility(View.VISIBLE);
            }

            this.thumbnailContainer.setVisibility(View.GONE);

            if (this.appConfig != null && this.player != null) {
                if (this.appConfig.getVideoSeekPosition() > 0) this.player.seekTo(this.appConfig.getVideoSeekPosition());
                this.player.setPlayWhenReady(true);

                this.manageDisposable(this.appConfig.videoSeekPositionChanges().subscribe(seekPosition -> {
                    this.playerView.setVisibility(View.VISIBLE);
                    this.thumbnailContainer.setVisibility(View.GONE);

                    this.player.seekTo(seekPosition);

                    if (this.appConfig.isVideoPlaying()) this.player.setPlayWhenReady(true);
                }));
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void releasePlayer() {
        if (this.player != null) this.player.release();
        if (this.playerView != null) this.removeView(this.playerView);

        this.player     = null;
        this.playerView = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void dispose() {
        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    //endregion

    //region Lifecycle

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        if (!this.isFirstTimeAttachment && this.video != null) this.setUpPlayer();

        this.playAction.setOnClickListener(view -> {
            this.startPlayer();

            this.videoClicks.onNext(Irrelevant.INSTANCE);
        });

        this.thumbnail.setOnClickListener(irrelevant -> {
            this.startPlayer();

            this.videoClicks.onNext(Irrelevant.INSTANCE);
        });


        if (this.fullScreenAction != null) this.fullScreenAction.setOnClickListener(view -> {
            final boolean isPlaying    = this.player.getPlaybackState() == Player.STATE_READY && this.player.getPlayWhenReady();
            final long    seekPosition = this.player.getCurrentPosition();

            this.player.setPlayWhenReady(false);

            this.getContext().startActivity(VideoActivity.createIntent(this.getContext(), this.video.getVideoUrl(), isPlaying, seekPosition));
        });

        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) {
            owner.getLifecycle().addObserver(this);
        }

        super.onAttachedToWindow();
    }

    @CallSuper
    @Override
    public void onDetachedFromWindow() {
        this.thumbnail.setOnClickListener(null);

        this.releasePlayer();

        super.onDetachedFromWindow();
    }

    //endregion

    private static boolean isYouTubeUrl(@NonNull final String url) {
        return url.startsWith("https://www.youtube.com/");
    }
}
