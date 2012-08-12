package org.samcrow.help;

import org.samcrow.stanford.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * An activity that displays a help screen
 * @author Sam Crow
 */
public class HelpActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.help);

	}

}
