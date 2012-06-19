package org.samcrow;

import android.app.Activity;
import android.os.Bundle;

public class ColonyNavigatorActivity extends Activity {

	private MapSurfaceView mapView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mapView = new MapSurfaceView(this);
		setContentView(mapView);
		
	}
}