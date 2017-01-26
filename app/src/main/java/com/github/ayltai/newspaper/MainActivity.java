package com.github.ayltai.newspaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import com.github.ayltai.newspaper.graphics.FaceDetectorFactory;
import com.github.ayltai.newspaper.net.ConnectivityChangeReceiver;
import com.github.ayltai.newspaper.util.ContextUtils;
import com.github.ayltai.newspaper.util.LogUtils;
import com.github.ayltai.newspaper.util.TestUtils;

import rx.Observable;
import rx.schedulers.Schedulers;

public final class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    //region Variables

    private FlowController             controller;
    private FirebaseRemoteConfig       config;
    private GoogleApiClient            client;
    private ConnectivityChangeReceiver receiver;
    private Snackbar                   snackbar;

    //endregion

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        ContextUtils.setAppTheme(this);

        super.onCreate(savedInstanceState);

        if (!TestUtils.isRunningTest()) {
            this.setUpRemoteConfig();

            this.client = new GoogleApiClient.Builder(this.getApplicationContext())
                .enableAutoManage(this, this)
                .addApiIfAvailable(AppInvite.API)
                .build();
        }

        this.setUpConnectivityChangeReceiver();

        this.onNewIntent(this.getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.controller.onDestroy();
        this.controller = null;

        FaceDetectorFactory.release();
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        this.controller = new FlowController(this);

        super.attachBaseContext(this.controller.attachNewBase(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.receiver.register();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.receiver.unregister();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!TestUtils.isRunningTest()) this.client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!TestUtils.isRunningTest()) this.client.disconnect();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getDataString() != null) {
            // TODO: Parse the data URL

            // TODO: Handle the deep link

            AppInvite.AppInviteApi.getInvitation(this.client, this, true)
                .setResultCallback(result -> {
                    if (result.getStatus().isSuccess()) {
                        final String deepLink = AppInviteReferral.getDeepLink(result.getInvitationIntent());

                        // TODO: Handle the deep link
                    }
                });
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_SETTINGS && resultCode == Activity.RESULT_OK) this.startActivity(this.getBaseContext().getPackageManager().getLaunchIntentForPackage(this.getBaseContext().getPackageName()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void onBackPressed() {
        if (!this.controller.onBackPressed()) super.onBackPressed();
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

    private void setUpConnectivityChangeReceiver() {
        this.snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.error_no_connection, Snackbar.LENGTH_INDEFINITE);
        ((TextView)this.snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(ContextUtils.getColor(this, R.attr.textColorInverse));

        this.receiver = new ConnectivityChangeReceiver(this) {
            @Override
            protected void onConnectivityChange(final boolean isConnected) {
                if (isConnected) {
                    if (MainActivity.this.snackbar.isShown()) MainActivity.this.snackbar.dismiss();
                } else {
                    if (!MainActivity.this.snackbar.isShown()) MainActivity.this.snackbar.show();
                }
            }
        };
    }

    private void setUpRemoteConfig() {
        Observable.<FirebaseRemoteConfig>create(subscriber -> subscriber.onNext(FirebaseRemoteConfig.getInstance()))
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(config -> {
                this.config = config;
                this.config.setDefaults(R.xml.config);

                this.applyRemoteConfig();

                this.config.fetch(Constants.REMOTE_CONFIG_CACHE_EXPIRATION)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            this.config.activateFetched();
                        } else {
                            LogUtils.getInstance().w(this.getClass().getSimpleName(), "Failed to fetch remote config");
                        }
                    });
            });
    }

    private void applyRemoteConfig() {
        Configs.apply(this.config);
    }

    private static void logConnectionError(@NonNull final ConnectionResult connectionResult) {
        LogUtils.getInstance().w(MainActivity.class.getName(), "onConnectionFailed: errorCode=" + connectionResult.getErrorCode() + ", errorMessage=" + connectionResult.getErrorMessage());
    }
}
