package org.samcrow;

import org.samcrow.data.Colony;
import org.samcrow.data.provider.ColonyProvider;
import org.samcrow.data.provider.MemoryCardDataProvider;
import org.samcrow.stanford.R;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ColonyNavigatorActivity extends Activity {

	public static final ColonyProvider provider = new MemoryCardDataProvider();

	private MapSurfaceView mapView;

	public static TextView longitudeField;

	public static TextView latitudeField;

	private EditText colonyField;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mapView = (MapSurfaceView) findViewById(R.id.mapView);
		latitudeField = (TextView) findViewById(R.id.latitudeField);
		longitudeField = (TextView) findViewById(R.id.longitudeField);

		colonyField = (EditText) findViewById(R.id.colonyField);
		colonyField.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_GO) {
					highlightColony();
					return true;
				}
				return false;
			}

		});


		Button okButton = (Button) findViewById(R.id.colonyOkButton);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				highlightColony();
			}
		});

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

	/**
	 * Get the colony number from the colony field and highlight the colony corresponding to the number entered
	 */
	protected void highlightColony() {

		String numberString = colonyField.getText().toString();

		if(numberString.length() > 0) {

			int id = Integer.valueOf(numberString);

			for(Colony colony : provider.getColonies()) {
				if(colony.getId() == id) {
					//Found the colony
					mapView.setSelectedColony(colony);
					mapView.centerViewOn(colony);

					//Hide the keyboard
					InputMethodManager imm = (InputMethodManager)getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(colonyField.getWindowToken(), 0);

					return;
				}
			}

			//Colony not found
			Toast.makeText(colonyField.getContext(), "Colony #"+numberString+" could not be found.", Toast.LENGTH_LONG).show();
		}

	}

	public synchronized MapSurfaceView getMapView() {
		return mapView;
	}
}