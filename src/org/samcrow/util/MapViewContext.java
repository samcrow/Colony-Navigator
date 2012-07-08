package org.samcrow.util;

import org.samcrow.MapSurfaceView;

import android.content.Context;

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
	public static void init(Context context) {
		view = new MapSurfaceView(context);
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
