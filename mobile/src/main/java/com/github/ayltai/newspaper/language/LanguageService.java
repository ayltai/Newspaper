package com.github.ayltai.newspaper.language;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1beta2.model.AnalyzeSentimentRequest;
import com.google.api.services.language.v1beta2.model.ClassificationCategory;
import com.google.api.services.language.v1beta2.model.ClassifyTextRequest;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Sentiment;

import com.github.ayltai.newspaper.util.TestUtils;

public final class LanguageService {
    private static final String DOCUMENT_TYPE = "PLAIN_TEXT";

    private final CloudNaturalLanguage api;

    LanguageService(@NonNull final GoogleCredential credential) {
        this.api = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).build();
    }

    @WorkerThread
    @NonNull
    public List<Entity> analyzeEntities(@NonNull final String text) {
        try {
            return this.api
                .documents()
                .analyzeEntities(new AnalyzeEntitiesRequest().setDocument(LanguageService.createDocument(text)))
                .execute()
                .getEntities();
        } catch (final IOException e) {
            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @WorkerThread
    @Nullable
    public Sentiment analyzeSentiment(@NonNull final String text) {
        try {
            return this.api
                .documents()
                .analyzeSentiment(new AnalyzeSentimentRequest().setDocument(LanguageService.createDocument(text)))
                .execute()
                .getDocumentSentiment();
        } catch (final IOException e) {
            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        return null;
    }

    @WorkerThread
    @Nullable
    public List<ClassificationCategory> classifyText(@NonNull final String text) {
        try {
            return this.api
                .documents()
                .classifyText(new ClassifyTextRequest().setDocument(LanguageService.createDocument(text)))
                .execute()
                .getCategories();
        } catch (final IOException e) {
            if (TestUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @NonNull
    private static Document createDocument(@NonNull final String text) {
        return new Document().setContent(text).setType(LanguageService.DOCUMENT_TYPE);
    }
}
