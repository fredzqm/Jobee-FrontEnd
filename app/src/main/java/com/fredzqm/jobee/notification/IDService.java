package com.fredzqm.jobee.notification;

import android.util.Log;

import com.fredzqm.jobee.login.LoginActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class IDService extends FirebaseInstanceIdService {
    private static final String TAG = "InstIDService";

    public IDService() {
    }

    @Override
    public void onTokenRefresh() {
        updateToken();
    }

    public static void updateToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        Notifier.getReference().child(LoginActivity.getUserID()).setValue(refreshedToken);
    }

}
