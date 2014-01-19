package com.ne4istb.test.geofences;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofencingService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    public static final String EXTRA_REQUEST_IDS = "requestId";
    public static final String EXTRA_GEOFENCE = "geofence";
    public static final String EXTRA_ACTION = "action";

    private List<Geofence> mGeofenceListsToAdd = new ArrayList<Geofence>();
    private List<String> mGeofenceListsToRemove = new ArrayList<String>();
    private LocationClient mLocationClient;
    private Action mAction;

    public static enum Action implements Serializable {ADD, REMOVE};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("GEO", "Location service started");

        mAction = (Action) intent.getSerializableExtra(EXTRA_ACTION);

        switch (mAction) {
            case ADD:
                MyGeofence newGeofence = (MyGeofence) intent.getSerializableExtra(EXTRA_GEOFENCE);
                mGeofenceListsToAdd.add(newGeofence.toGeofence());
                break;
            case REMOVE:
                mGeofenceListsToRemove = Arrays.asList(intent.getStringArrayExtra(EXTRA_REQUEST_IDS));
                break;
        }

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GEO", "Location client connected");

        switch (mAction) {
            case ADD:
                Log.d("GEO", "Location client adds geofence");
                mLocationClient.addGeofences(mGeofenceListsToAdd, getPendingIntent(), this);
                break;
            case REMOVE:
                Log.d("GEO", "Location client removes geofence");
                mLocationClient.removeGeofences(mGeofenceListsToRemove, this);
                break;
        }
    }

    private PendingIntent getPendingIntent() {
        Intent transitionService = new Intent(this, ReceiveTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, transitionService, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDisconnected() {
        Log.d("GEO", "Location client disconnected");
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        if (LocationStatusCodes.SUCCESS == i) {

            Log.d("GEO", "Geofences added " + strings);

            for (String geofenceId : strings)
                Toast.makeText(this, "Geofences added: " + geofenceId, Toast.LENGTH_SHORT).show();

            mLocationClient.disconnect();
            stopSelf();
        } else {
            Log.e("GEO", "Error while adding geofence: " + strings);
        }
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {
        if (LocationStatusCodes.SUCCESS == i) {
            Log.d("GEO", "Geofences removed" + strings);
            mLocationClient.disconnect();
            stopSelf();
        } else {
            Log.e("GEO", "Error while removing geofence: " + strings);
        }
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("GEO", "Location client connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("GEO", "Location service destroyed");
        super.onDestroy();
    }
}