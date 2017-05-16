package com.github.ayltai.newspaper.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

public abstract class ConnectivityChangeReceiver extends BroadcastReceiver {
    private final Context context;

    private boolean isConnected;

    public ConnectivityChangeReceiver(@NonNull final Context context) {
        this.context = context;

        this.updateConnectivityStatus();
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.updateConnectivityStatus();
    }

    public void register() {
        this.context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void unregister() {
        if (this.context != null) this.context.unregisterReceiver(this);
    }

    protected abstract void onConnectivityChange(boolean isConnected);

    private void updateConnectivityStatus() {
        final NetworkInfo info        = ((ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        final boolean     isConnected = info != null && info.isConnected();

        if (isConnected != this.isConnected) {
            this.isConnected = isConnected;

            this.onConnectivityChange(this.isConnected);
        }
    }
}
