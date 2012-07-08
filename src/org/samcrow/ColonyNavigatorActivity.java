package org.samcrow;

import org.samcrow.net.ColonyChangeCallbackManager;
import org.samcrow.net.ServerConnection;
import org.samcrow.util.MapViewContext;

import android.app.Activity;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;

public class ColonyNavigatorActivity extends Activity {

	private MapSurfaceView mapView;

	public static ColonyChangeCallbackManager server;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MapViewContext.init(this);
		mapView = MapViewContext.get();
		setContentView(mapView);

		server = new ServerConnection(this, "10.1.77.135", 7510);

		Criteria gpsCriteria = new Criteria();
		gpsCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		gpsCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		gpsCriteria.setBearingRequired(true);
		gpsCriteria.setAltitudeRequired(false);
		gpsCriteria.setSpeedAccuracy(Criteria.NO_REQUIREMENT);

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(100, 0, gpsCriteria,
				new NavigatorLocationListener(), null);
	}

	public synchronized MapSurfaceView getMapView() {
		return mapView;
	}
}