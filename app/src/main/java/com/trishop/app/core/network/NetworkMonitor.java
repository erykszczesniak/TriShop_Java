package com.trishop.app.core.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

/** Connectivity signal for the offline banner, like the iOS NWPathMonitor. */
@Singleton
public class NetworkMonitor {

    private final Flowable<Boolean> isOnline;

    @Inject
    public NetworkMonitor(@ApplicationContext Context context) {
        isOnline = Flowable.<Boolean>create(emitter -> {
            ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);

            emitter.onNext(isCurrentlyOnline(manager));

            ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    emitter.onNext(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    emitter.onNext(isCurrentlyOnline(manager));
                }
            };
            manager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(),
                    callback);
            emitter.setCancellable(() -> manager.unregisterNetworkCallback(callback));
        }, BackpressureStrategy.LATEST).distinctUntilChanged();
    }

    @NonNull
    public Flowable<Boolean> isOnline() {
        return isOnline;
    }

    private static boolean isCurrentlyOnline(ConnectivityManager manager) {
        Network network = manager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = manager.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
