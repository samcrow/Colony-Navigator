package org.samcrow;

import org.samcrow.stanford.R;
import org.samcrow.util.MapViewContext;

import android.app.Activity;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;

public class ColonyNavigatorActivity extends Activity {

	private MapSurfaceView mapView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MapViewContext.init(this);
		setContentView(R.layout.main);

		Criteria gpsCriteria = new Criteria();
		gpsCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		gpsCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		gpsCriteria.setBearingRequired(true);
		gpsCriteria.setAltitudeRequired(false);
		gpsCriteria.setSpeedAccuracy(Criteria.NO_REQUIREMENT);

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(1000, 0, gpsCriteria,
				new NavigatorLocationListener(), null);
	}

	public synchronized MapSurfaceView getMapView() {
		return mapView;
	}
}