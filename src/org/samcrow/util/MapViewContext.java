package org.samcrow.util;

import org.samcrow.MapSurfaceView;
import org.samcrow.stanford.R;

import android.app.Activity;

/**
 * A singleton class that stores a MapSurfaceView accessible across the
 * application.
 * 
 * @author Sam Crow
 */
public final class MapViewContext {

	private static MapSurfaceView view;

	/**
	 * Initialize the view in a context. This should only be called once when
	 * the application is initialized.
	 * 
	 * @param context
	 *            The context to create the view in
	 */
	public static void init(Activity context) {
		view = (MapSurfaceView) context.findViewById(R.id.mapSurfaceView1);
	}

	/**
	 * Get the view. May be null.
	 * 
	 * @return the view
	 */
	public static MapSurfaceView get() {
		return view;
	}
}
