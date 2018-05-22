package com.github.ayltai.newspaper.language;

import java.util.Arrays;
import java.util.Comparator;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.github.ayltai.newspaper.BuildConfig;
import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Response;
import com.textrazor.annotations.Topic;

public final class LanguageService {
    public static final int MAX_TOPICS = 2;

    public static final Comparator<Topic> TOPIC_COMPARATOR = (lhs, rhs) -> {
        final double diff = rhs.getScore() - lhs.getScore();

        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    };

    public static final Comparator<Entity> ENTITY_COMPARATOR = (lhs, rhs) -> {
        final double diff = rhs.getConfidenceScore() - lhs.getConfidenceScore();

        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    };

    private final TextRazor client;

    LanguageService(final TextRazor client) {
        this.client = client;

        this.client.setCleanupMode("stripTags");
        this.client.setDownloadUserAgent(BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME);
        this.client.setAllowOverlap(false);
    }

    @WorkerThread
    @NonNull
    public Response analyze(@NonNull final String text) throws NetworkException, AnalysisException {
        this.client.setExtractors(Arrays.asList(Extractor.ENTITIES.toString(), Extractor.TOPICS.toString()));

        return this.client.analyze(text).getResponse();
    }
}
