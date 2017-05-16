package com.github.ayltai.newspaper.video;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import com.github.ayltai.newspaper.BuildConfig;
import com.github.ayltai.newspaper.Constants;
import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.RxBus;
import com.github.ayltai.newspaper.util.LogUtils;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.disposables.CompositeDisposable;

public final class VideoActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();

    private SimpleExoPlayer videoPlayer;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.activity_video);

        this.videoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(null)));

        final SimpleExoPlayerView videoPlayerView = (SimpleExoPlayerView)this.findViewById(R.id.video);
        videoPlayerView.setPlayer(this.videoPlayer);

        final View videoFullScreen     = videoPlayerView.findViewById(R.id.exo_fullscreen);
        final View videoFullScreenExit = videoPlayerView.findViewById(R.id.exo_fullscreen_exit);

        videoFullScreen.setVisibility(View.GONE);
        videoFullScreenExit.setVisibility(View.VISIBLE);

        final String  videoUrl     = this.getIntent().getStringExtra(Constants.EXTRA_VIDEO_URL);
        final boolean isPlaying    = this.getIntent().getBooleanExtra(Constants.EXTRA_IS_PLAYING, false);
        final long    seekPosition = this.getIntent().getLongExtra(Constants.EXTRA_SEEK_POSITION, 0);

        this.disposables.add(RxView.clicks(videoFullScreenExit).subscribe(
            dummy -> {
                this.notifyCurrentPlaybackState();

                this.finish();
            },
            error -> LogUtils.getInstance().e(this.getClass().getSimpleName(), error.getMessage(), error)));

        this.videoPlayer.prepare(new ExtractorMediaSource(Uri.parse(videoUrl), new DefaultDataSourceFactory(this,Util.getUserAgent(this, BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME), null), new DefaultExtractorsFactory(), null, null));
        this.videoPlayer.seekTo(seekPosition);

        if (isPlaying) this.videoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.disposables.dispose();
        this.videoPlayer.release();
    }

    @Override
    public void onBackPressed() {
        this.notifyCurrentPlaybackState();

        super.onBackPressed();
    }

    private void notifyCurrentPlaybackState() {
        final boolean isPlaying    = this.videoPlayer.getPlaybackState() == ExoPlayer.STATE_READY && this.videoPlayer.getPlayWhenReady();
        final long    seekPosition = this.videoPlayer.getCurrentPosition();

        this.videoPlayer.setPlayWhenReady(false);

        RxBus.getInstance().send(new VideoEvent(isPlaying, seekPosition));
    }
}
