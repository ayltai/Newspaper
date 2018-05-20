package com.github.ayltai.newspaper.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.config.AppConfig;
import com.github.ayltai.newspaper.util.RxUtils;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;

public final class VideoActivity extends AppCompatActivity {
    //region Constants

    @VisibleForTesting
    public static final String EXTRA_VIDEO_URL     = "extra-video-url";
    @VisibleForTesting
    public static final String EXTRA_IS_PLAYING    = "extra-is-playing";
    @VisibleForTesting
    public static final String EXTRA_SEEK_POSITION = "extra-seek-position";

    //endregion

    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    private SimpleExoPlayer videoPlayer;

    @NonNull
    public static Intent createIntent(@NonNull final Context context, @NonNull final String videoUrl, final boolean isPlaying, final long seekPosition) {
        return new Intent(context, VideoActivity.class)
            .putExtra(VideoActivity.EXTRA_VIDEO_URL, videoUrl)
            .putExtra(VideoActivity.EXTRA_IS_PLAYING, isPlaying)
            .putExtra(VideoActivity.EXTRA_SEEK_POSITION, seekPosition);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.activity_video);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.videoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(null)));

        final PlayerView videoPlayerView = this.findViewById(R.id.video);
        videoPlayerView.setPlayer(this.videoPlayer);

        final View videoFullScreen     = videoPlayerView.findViewById(R.id.exo_fullscreen);
        final View videoFullScreenExit = videoPlayerView.findViewById(R.id.exo_fullscreen_exit);

        videoFullScreen.setVisibility(View.GONE);
        videoFullScreenExit.setVisibility(View.VISIBLE);

        final String  videoUrl     = this.getIntent().getStringExtra(VideoActivity.EXTRA_VIDEO_URL);
        final boolean isPlaying    = this.getIntent().getBooleanExtra(VideoActivity.EXTRA_IS_PLAYING, false);
        final long    seekPosition = this.getIntent().getLongExtra(VideoActivity.EXTRA_SEEK_POSITION, 0);

        videoFullScreenExit.setOnClickListener(view -> {
            this.notifyCurrentPlaybackState();
            this.finish();
        });

        this.videoPlayer.prepare(new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, Util.getUserAgent(this, BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME), null)).createMediaSource(Uri.parse(videoUrl)));
        this.videoPlayer.seekTo(seekPosition);

        this.findViewById(R.id.exo_playback_control_view).setVisibility(View.VISIBLE);

        if (isPlaying) this.videoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RxUtils.resetDisposables(this.disposables);

        this.videoPlayer.release();
    }

    @Override
    public void onBackPressed() {
        this.notifyCurrentPlaybackState();

        super.onBackPressed();
    }

    private void notifyCurrentPlaybackState() {
        final boolean isPlaying    = this.videoPlayer.getPlaybackState() == Player.STATE_READY && this.videoPlayer.getPlayWhenReady();
        final long    seekPosition = this.videoPlayer.getCurrentPosition();

        this.videoPlayer.setPlayWhenReady(false);

        final AppConfig appConfig = ComponentFactory.getInstance()
            .getConfigComponent(this)
            .appConfig();

        appConfig.setVideoPlaying(isPlaying);
        appConfig.setVideoSeekPosition(seekPosition);
    }
}
