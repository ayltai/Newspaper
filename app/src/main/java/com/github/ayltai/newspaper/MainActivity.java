package com.github.ayltai.newspaper;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import com.github.ayltai.newspaper.item.ItemPresenter;
import com.github.ayltai.newspaper.item.ItemScreen;
import com.github.ayltai.newspaper.list.ListScreen;
import com.github.ayltai.newspaper.main.MainPresenter;
import com.github.ayltai.newspaper.main.MainScreen;
import com.github.ayltai.newspaper.setting.Settings;
import com.rohitarya.fresco.facedetection.processor.core.FrescoFaceDetector;

import flow.Flow;
import flow.KeyDispatcher;
import flow.KeyParceler;
import flow.State;
import flow.TraversalCallback;
import io.realm.Realm;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final Map<Class<?>, Presenter.View>  screens    = new HashMap<>();
    private final Map<Presenter.View, Presenter> presenters = new HashMap<>();

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    //region Variables

    private FirebaseRemoteConfig config;
    private GoogleApiClient      client;
    private FirebaseAnalytics    analytics;
    private Realm                realm;

    //endregion

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setUpRemoteConfig();

        FrescoFaceDetector.initialize(this);

        this.realm     = Realm.getDefaultInstance();
        this.analytics = FirebaseAnalytics.getInstance(this);

        this.client = new GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApiIfAvailable(AppInvite.API)
            .build();

        this.onNewIntent(this.getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.subscriptions.hasSubscriptions()) this.subscriptions.unsubscribe();

        for (final Presenter.View view : this.screens.values()) {
            if (view instanceof Closeable) {
                try {
                    ((Closeable)view).close();
                } catch (final IOException e) {
                    Log.e(this.getClass().getName(), e.getMessage(), e);
                }
            }
        }

        FrescoFaceDetector.releaseDetector();

        if (!this.realm.isClosed()) this.realm.close();
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(Flow.configure(newBase, this)
            .keyParceler(new KeyParceler() {
                @NonNull
                @Override
                public Parcelable toParcelable(@NonNull final Object key) {
                    return (Parcelable)key;
                }

                @NonNull
                @Override
                public Object toKey(@NonNull final Parcelable parcelable) {
                    return parcelable;
                }
            })
            .dispatcher(KeyDispatcher.configure(this, (outgoingState, incomingState, direction, incomingContexts, callback) -> {
                if (outgoingState != null)
                    outgoingState.save(((ViewGroup)this.findViewById(android.R.id.content)).getChildAt(0));

                final Presenter.View view;
                final Presenter presenter;

                if (this.screens.containsKey(incomingState.getKey().getClass())) {
                    view = this.screens.get(incomingState.getKey().getClass());
                    presenter = this.presenters.get(view);
                } else {
                    if (incomingState.getKey() instanceof ItemScreen.Key) {
                        view = new ItemScreen(this);
                        presenter = new ItemPresenter(this.realm);

                        this.subscriptions.add(view.attachments().subscribe(dummy -> presenter.onViewAttached(view), error -> Log.e(this.getClass().getName(), error.getMessage(), error)));
                        this.subscriptions.add(view.detachments().subscribe(dummy -> presenter.onViewDetached(), error -> Log.e(this.getClass().getName(), error.getMessage(), error)));
                    } else {
                        view = new MainScreen(this, this.realm);
                        presenter = new MainPresenter();
                    }

                    this.screens.put(incomingState.getKey().getClass(), view);
                    this.presenters.put(view, presenter);
                }

                if (incomingState.getKey() instanceof ItemScreen.Key) {
                    final ItemScreen.Key key = incomingState.getKey();

                    ((ItemPresenter)presenter).bind((ListScreen.Key)key.getParentKey(), key.getItem(), Settings.getListViewType(this));
                }

                this.dispatch((View)view, incomingState, callback);
            }).build())
            .defaultKey(Constants.KEY_SCREEN_MAIN)
            .install());
    }

    @Override
    public void onStart() {
        super.onStart();

        this.client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        this.client.disconnect();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getDataString() != null) {
            // TODO: Parse the data URL

            // TODO: Handle the deep link. For example, open the linked content

            AppInvite.AppInviteApi.getInvitation(this.client, this, true)
                .setResultCallback(result -> {
                    if (result.getStatus().isSuccess()) {
                        String deepLink = AppInviteReferral.getDeepLink(result.getInvitationIntent());

                        // TODO: Handle the deep link. For example, open the linked content
                    }
                });
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_SETTINGS && resultCode == Activity.RESULT_OK)
            this.startActivity(this.getBaseContext().getPackageManager().getLaunchIntentForPackage(this.getBaseContext().getPackageName()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void onBackPressed() {
        if (!Flow.get(this).goBack()) {
            final View view = ((ViewGroup)this.findViewById(android.R.id.content)).getChildAt(0);

            if (view instanceof MainScreen) {
                if (!((MainScreen)view).goBack()) super.onBackPressed();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Constants.REQUEST_FIREBASE);
            } catch (final IntentSender.SendIntentException e) {
                MainActivity.logConnectionError(connectionResult);
            }
        } else {
            MainActivity.logConnectionError(connectionResult);
        }
    }

    private void setUpRemoteConfig() {
        Observable.<FirebaseRemoteConfig>create(subscriber -> subscriber.onNext(FirebaseRemoteConfig.getInstance()))
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(config -> {
                this.config = config;
                // TODO: Applies default config
                // this.config.setDefaults(R.xml.config);

                this.applyRemoteConfig();

                this.config.fetch(Constants.REMOTE_CONFIG_CACHE_EXPIRATION)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            this.config.activateFetched();
                        } else {
                            FirebaseCrash.logcat(Log.WARN, this.getClass().getName(), "Failed to fetch remote config");
                        }
                    });
            });
    }

    private void applyRemoteConfig() {
        // TODO: Applies remote config
    }

    private void dispatch(@NonNull final View view, @NonNull final State incomingState, @NonNull final TraversalCallback callback) {
        incomingState.restore(view);

        this.setContentView(view);

        if (incomingState.getKey() instanceof ItemScreen.Key) {
            view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in));
            view.animate();
        }

        callback.onTraversalCompleted();
    }

    private static void logConnectionError(@NonNull final ConnectionResult connectionResult) {
        FirebaseCrash.logcat(Log.WARN, MainActivity.class.getName(), "onConnectionFailed: errorCode=" + connectionResult.getErrorCode() + ", errorMessage=" + connectionResult.getErrorMessage());
    }
}
