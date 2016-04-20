package com.ne4istb.test.geofences;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {

	public static final String TRANSITION_INTENT_SERVICE = "TransitionsService";

	public ReceiveTransitionsIntentService() {
		super(TRANSITION_INTENT_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if (geofencingEvent.hasError()) {
			Log.e(TRANSITION_INTENT_SERVICE, "Location Services error: " + geofencingEvent.getErrorCode());
			return;
		}

		int transitionType = geofencingEvent.getGeofenceTransition();

		List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

		for (Geofence geofence : triggeredGeofences) {
			Log.d("GEO", "onHandle:" + geofence.getRequestId());
			processGeofence(geofence, transitionType);
		}
	}

	private void processGeofence(Geofence geofence, int transitionType) {

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());

		PendingIntent openActivityIntetnt = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		int id = Integer.parseInt(geofence.getRequestId());

		String transitionTypeString = getTransitionTypeString(transitionType);
		notificationBuilder
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Geofence id: " + id)
				.setContentText("Transition type: " + transitionTypeString)
				.setVibrate(new long[]{500, 500})
				.setContentIntent(openActivityIntetnt)
				.setAutoCancel(true);

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(transitionType * 100 + id, notificationBuilder.build());

		Log.d("GEO", String.format("notification built:%d %s", id, transitionTypeString));
	}

	private String getTransitionTypeString(int transitionType) {
		switch (transitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return "enter";
			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return "exit";
			case Geofence.GEOFENCE_TRANSITION_DWELL:
				return "dwell";
			default:
				return "unknown";
		}
	}

	private void removeGeofences(List<String> requestIds) {
		Intent intent = new Intent(getApplicationContext(), GeofencingService.class);

		String[] ids = new String[0];
		intent.putExtra(GeofencingService.EXTRA_REQUEST_IDS, requestIds.toArray(ids));
		intent.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.REMOVE);

		startService(intent);
	}
}