package com.example.manage.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.manage.R;

public class NetworkStateChecker {

    private final Context context;
    private final LinearLayout networkErrorLayout;
    private final ConnectivityManager.NetworkCallback networkCallback;

    public NetworkStateChecker(Context context, @NonNull View view) {
        this.context = context;
        this.networkErrorLayout = view.findViewById(R.id.ll_network_error_layout);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                // Network is available, hide network error layout.
                networkErrorLayout.post(() -> networkErrorLayout.setVisibility(View.GONE));
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // Network is lost, show network error layout.
                networkErrorLayout.post(() -> networkErrorLayout.setVisibility(View.VISIBLE));
            }
        };
        registerNetworkCallback();
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    public void unregisterNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}
