package com.github.ayltai.newspaper.speech;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import com.google.auto.value.AutoValue;

import com.github.ayltai.newspaper.util.Irrelevant;
import com.github.ayltai.newspaper.util.RxUtils;
import com.github.ayltai.newspaper.util.DevUtils;

import io.reactivex.Single;
import rx.functions.Action1;

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
        public abstract SimpleTextToSpeech.Builder setOnInitError(@Nullable Action1<Integer> action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnNotSupported(@Nullable Action1<Irrelevant> action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnMissingData(@Nullable Action1<Irrelevant> action);

        @NonNull
        public abstract SimpleTextToSpeech.Builder setOnUtteranceCompleted(@Nullable Action1<Irrelevant> action);

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
    protected abstract Action1<Integer> getOnInitError();

    @Nullable
    protected abstract Action1<Irrelevant> getOnNotSupported();

    @Nullable
    protected abstract Action1<Irrelevant> getOnMissingData();

    @Nullable
    protected abstract Action1<Irrelevant> getOnUtteranceCompleted();

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

                    if (this.getOnNotSupported() != null) this.getOnNotSupported().call(Irrelevant.INSTANCE);
                }
            } else {
                if (DevUtils.isLoggable()) Log.w(this.getClass().getSimpleName(), "TTS initialization error " + status);

                if (this.getOnInitError() != null) this.getOnInitError().call(status);
            }

            if (!emitter.isDisposed()) emitter.onSuccess(Irrelevant.INSTANCE);
        }));
    }

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
                        if (SimpleTextToSpeech.this.getOnUtteranceCompleted() != null && Integer.parseInt(utteranceId) == SimpleTextToSpeech.this.utteranceId.get()) SimpleTextToSpeech.this.getOnUtteranceCompleted().call(Irrelevant.INSTANCE);
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

                if (this.getOnMissingData() != null) this.getOnMissingData().call(Irrelevant.INSTANCE);

                return true;
            }
        }

        return false;
    }

    //endregion
}
