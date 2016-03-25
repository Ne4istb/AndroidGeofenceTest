package com.ne4istb.test.geofences;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeofencingService extends Service implements
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public static final String EXTRA_REQUEST_IDS = "requestId";
	public static final String EXTRA_GEOFENCE = "geofence";
	public static final String EXTRA_ACTION = "action";

	private List<Geofence> mGeofenceListsToAdd = new ArrayList<>();
	private GoogleApiClient mGoogleApiClient;
	private Action mAction;

	public enum Action implements Serializable {ADD, REMOVE}


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
//                mGeofenceListsToRemove = Arrays.asList(intent.getStringArrayExtra(EXTRA_REQUEST_IDS));
				break;
		}


		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d("GEO", "Location client connected");
		switch (mAction) {
			case ADD:
				GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
				Log.d("GEO", "Location client adds geofence");
				builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
				builder.addGeofences(mGeofenceListsToAdd);
				LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, builder.build(), getPendingIntent())
						.setResultCallback(new ResultCallback<Status>() {
							@Override
							public void onResult(@NonNull Status status) {
								if (status.isSuccess()) {
									String msg = "Geofences added: " + status.getStatusMessage();
									Log.e("GEO", msg);
									Toast.makeText(GeofencingService.this, msg, Toast.LENGTH_SHORT)
											.show();
								}
								GeofencingService.this.onResult(status);
							}
						});
				break;
			case REMOVE:
				break;
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.e("GEO", "onConnectionSuspended i = " + i);
	}

	private PendingIntent getPendingIntent() {
		Intent transitionService = new Intent(this, ReceiveTransitionsIntentService.class);
		return PendingIntent.getService(this, 0, transitionService, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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

	public void onResult(@NonNull Status status) {
		Log.d("GEO", "Geofences onResult" + status.toString());
		if (status.isSuccess()) {
			mGoogleApiClient.disconnect();
			stopSelf();
		} else {
			String text = "Error while geofence: " + status.getStatusMessage();
			Log.e("GEO", text);
			Toast.makeText(GeofencingService.this, text, Toast.LENGTH_SHORT)
					.show();
		}

	}

}