package com.github.ayltai.newspaper.util;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

@AutoValue
public abstract class SimpleTextToSpeech {
    @AutoValue.Builder
    public abstract static class Builder {
        private final List<Locale> locales = new ArrayList<>();

        @NonNull
        public final SimpleTextToSpeech.Builder addLocale(@NonNull final Locale locale) {
            this.locales.add(locale);

            return this;
        }

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnInitError(@Nullable Function<Integer, Irrelevant> action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnNotSupported(@Nullable Action action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnMissingData(@Nullable Action action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnUtteranceCompleted(@Nullable Action action);

        @NonNull
        abstract SimpleTextToSpeech internalBuild();

        public final Single<SimpleTextToSpeech> build(@NonNull final Activity activity) {
            final SimpleTextToSpeech tts = this.internalBuild();
            tts.setActivity(activity);
            tts.setLocales(this.locales);

            return tts.init()
                .compose(RxUtils.applySingleBackgroundSchedulers())
                .map(irrelevant -> tts);
        }
    }

    private final Map<Locale, Integer> availabilities = new ArrayMap<>();
    private final AtomicInteger        utteranceId    = new AtomicInteger(0);

    private Activity     activity;
    private List<Locale> locales;
    private TextToSpeech tts;

    public static SimpleTextToSpeech.Builder builder() {
        return new AutoValue_SimpleTextToSpeech.Builder();
    }

    //region Properties

    @Nullable
    protected abstract Function<Integer, Irrelevant> getOnInitError();

    @Nullable
    protected abstract Action getOnNotSupported();

    @Nullable
    protected abstract Action getOnMissingData();

    @Nullable
    protected abstract Action getOnUtteranceCompleted();

    private void setActivity(@NonNull final Activity activity) {
        this.activity = activity;
    }

    private void setLocales(@NonNull final List<Locale> locales) {
        this.locales = locales;
    }

    //endregion

    //region Methods

    public void setText(@NonNull final CharSequence text) {
        if (this.tts != null) this.tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(this.utteranceId.incrementAndGet()));
    }

    public void addText(@NonNull final CharSequence text) {
        if (this.tts != null) this.tts.speak(text, TextToSpeech.QUEUE_ADD, null, String.valueOf(this.utteranceId.incrementAndGet()));
    }

    public void shutdown() {
        if (this.tts != null) this.tts.shutdown();
    }

    private Single<Irrelevant> init() {
        return Single.create(emitter -> this.tts = new TextToSpeech(this.activity, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (!this.initIfLocaleIsAvailable() && !this.initIfLocaleIsMissing()) {
                    if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS locale not supported");

                    if (this.getOnNotSupported() != null) {
                        try {
                            this.getOnNotSupported().run();
                        } catch (final Exception e) {
                            if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                        }
                    }
                }
            } else {
                if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS initialization error " + status);

                if (this.getOnInitError() != null) {
                    try {
                        this.getOnInitError().apply(status);
                    } catch (final Exception e) {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
        }));
    }

    @SuppressWarnings("deprecated")
    private boolean initIfLocaleIsAvailable() {
        for (final Locale locale : this.locales) {
            final int availability = this.tts.isLanguageAvailable(locale);
            this.availabilities.put(locale, availability);

            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                this.tts.setLanguage(locale);

                this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(final String utteranceId) {
                    }

                    @Override
                    public void onDone(final String utteranceId) {
                        if (SimpleTextToSpeech.this.getOnUtteranceCompleted() != null && Integer.parseInt(utteranceId) == SimpleTextToSpeech.this.utteranceId.get()) {
                            try {
                                SimpleTextToSpeech.this.getOnUtteranceCompleted().run();
                            } catch (final Exception e) {
                                if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                            }
                        }
                    }

                    @Override
                    public void onError(final String utteranceId) {
                        if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS utterance error for utterance ID " + utteranceId);
                    }

                    @Override
                    public void onError(final String utteranceId, final int errorCode) {
                        if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS utterance error " + errorCode + " for utterance ID " + utteranceId);
                    }
                });

                return true;
            }
        }

        return false;
    }

    private boolean initIfLocaleIsMissing() {
        for (final Locale locale : this.locales) {
            final int availability;

            if (this.availabilities.containsKey(locale)) {
                availability = this.availabilities.get(locale);
            } else {
                availability = this.tts.isLanguageAvailable(locale);
                this.availabilities.put(locale, availability);
            }

            if (availability == TextToSpeech.LANG_MISSING_DATA) {
                if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS missing locale data");

                this.activity.startActivity(new Intent().setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));

                if (this.getOnMissingData() != null) {
                    try {
                        this.getOnMissingData().run();
                    } catch (final Exception e) {
                        if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);

                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    //endregion
}
