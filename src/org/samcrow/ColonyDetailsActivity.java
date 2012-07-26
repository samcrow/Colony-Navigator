package org.samcrow;

import org.samcrow.stanford.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.colony_details);

		cancelButton = (Button) findViewById(R.id.cancelButton);
		okButton = (Button) findViewById(R.id.okButton);

		visitedCheckbox = (CheckBox) findViewById(R.id.visitedCheckBox);
		activeCheckbox = (CheckBox) findViewById(R.id.activeCheckBox);

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				System.out.println("Clicked");

				// Stop this activity/intent
				stopService(getIntent());

			}

		});

		setContentView(R.layout.colony_details);
	}
}
