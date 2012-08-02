package org.samcrow;

import org.samcrow.data.Colony;
import org.samcrow.stanford.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * A secondary activity used to view and enter details on a certain colony
 * 
 * @author samcrow
 */
public class ColonyDetailsActivity extends Activity {

	private Button cancelButton;

	private Button okButton;

	private CheckBox visitedCheckbox;

	private CheckBox activeCheckbox;

	/**
	 * A reference to the colony that is being edited
	 */
	private Colony colony;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Not the best way to give this activity a reference to the colony, but it works
		colony = MapSurfaceView.selectedColony;

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		setContentView(R.layout.colony_details);

		TextView title = (TextView) findViewById(R.id.colonyNumberText);
		title.setText("Colony "+colony.getId());

		cancelButton = (Button) findViewById(R.id.cancelButton);
		okButton = (Button) findViewById(R.id.okButton);

		visitedCheckbox = (CheckBox) findViewById(R.id.visitedCheckBox);
		activeCheckbox = (CheckBox) findViewById(R.id.activeCheckBox);

		//Update the checkboxes with data for the selected colony
		visitedCheckbox.setChecked(colony.isVisited());
		activeCheckbox.setChecked(colony.isActive());

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Stop this activity/intent and return to the map view
				finish();
			}
		});

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				colony.setVisited(visitedCheckbox.isChecked());
				colony.setActive(activeCheckbox.isChecked());
				ColonyNavigatorActivity.provider.updateColony(colony);

				//Return to the main view
				finish();
			}
		});

	}
}
