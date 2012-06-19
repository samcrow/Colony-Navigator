package org.samcrow;

import org.samcrow.data.Server;

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

		Server server = new Server("127.0.0.1", 7510);

	}
}