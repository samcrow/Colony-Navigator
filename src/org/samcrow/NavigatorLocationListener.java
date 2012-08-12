package org.samcrow;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Handles location updates from the system
 * 
 * @author Sam Crow
 */
public class NavigatorLocationListener implements LocationListener {

	/** The last measured location of the device */
	protected static volatile Location currentLocation;

	/**
	 * Get the last known location.
	 * 
	 * @return the location. May be null.
	 */
	public static synchronized Location getLocation() {
		return currentLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onLocationChanged(android.location.
	 * Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;

		ColonyNavigatorActivity.latitudeField.setText(String.valueOf(location
				.getLatitude()));
		ColonyNavigatorActivity.longitudeField.setText(String.valueOf(location
				.getLongitude()));

		if(MapSurfaceView.instance != null) {
			MapSurfaceView.instance.postInvalidate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String,
	 * int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
