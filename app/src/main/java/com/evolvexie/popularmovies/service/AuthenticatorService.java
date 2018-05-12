package com.evolvexie.popularmovies.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evolvexie.popularmovies.model.Authenticator;

public class AuthenticatorService extends Service {

    private static final String TAG = AuthenticatorService.class.getSimpleName();
    private Authenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        Log.i(TAG, "AuthenticatorService created");
        mAuthenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "AuthenticatorService onBind");
        return mAuthenticator.getIBinder();
    }
}
