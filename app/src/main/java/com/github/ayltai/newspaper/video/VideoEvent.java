package com.github.ayltai.newspaper.video;

public final class VideoEvent {
    private final boolean isPlaying;
    private final long    seekPosition;

    public VideoEvent(final boolean isPlaying, final long seekPosition) {
        this.isPlaying    = isPlaying;
        this.seekPosition = seekPosition;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public long getSeekPosition() {
        return this.seekPosition;
    }
}
