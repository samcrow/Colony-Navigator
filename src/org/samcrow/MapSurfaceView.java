package org.samcrow;

import java.util.List;

import org.samcrow.data.Colony;
import org.samcrow.net.ColonyChangeCallbackManager.ColonyChangeListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

/**
 * @author Sam Crow Handles drawing the map
 */
public class MapSurfaceView extends View implements OnScaleGestureListener,
		ColonyChangeListener {

	/**
	 * The current scale in the range (0, infinity), centered on 1, to display
	 * the map at
	 */
	private float scale = 1;
	/** The thing that detects pinch gestures for scaling the map */
	private ScaleGestureDetector scaleDetector;

	/** The X location in pixels at which the current drag gesture started */
	private float dragStartX = 0;
	/** The Y location in pixels at which the current drag gesture started */
	private float dragStartY = 0;

	/**
	 * The X location in pixels that the map should be offset from the default
	 * position
	 */
	private float relativeX = 0;
	/**
	 * The Y location in pixels that the map should be offset from the default
	 * position
	 */
	private float relativeY = 0;

	/**
	 * @param context
	 */
	public MapSurfaceView(Context context) {
		super(context);
		initScale(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MapSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initScale(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MapSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initScale(context);
	}

	/**
	 * Set up the scale listeners
	 */
	private void initScale(Context context) {
		scaleDetector = new ScaleGestureDetector(context, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.setDensity(300);// See if this makes it cleaner

		// This has to be at the beginning, before anything else is added.
		canvas.rotate(10);// Rotate 10 degrees to correct for the colony
							// coordinates being based on magnetic north
		canvas.translate(relativeX / 30f * scale, relativeY / 30f * scale);
		canvas.scale(scale, -scale, (canvas.getWidth()) / 2f,
				(canvas.getHeight()) / 2f);

		Paint antiAliasPaint = new Paint();
		antiAliasPaint.setAntiAlias(true);

		// Clear the screen
		canvas.drawColor(Color.WHITE);

		// antiAliasPaint.setColor(Color.RED);
		// canvas.drawRect(getColonyMapBounds(), antiAliasPaint);
		// antiAliasPaint.setColor(Color.BLACK);

		List<Colony> colonies = ColonyNavigatorActivity.server.getColonies();
		Location location = NavigatorLocationListener.getLocation();
		if (colonies != null) {

			synchronized (colonies) {
				for (Colony colony : colonies) {
					canvas.drawCircle((float) colony.getX(),
							(float) colony.getY(), 5, antiAliasPaint);
				}
			}
		}
		if (location != null) {
			canvas.drawText("Latitude " + location.getLatitude()
					+ " Longitude " + location.getLongitude(), 0, 100,
					antiAliasPaint);
		}

		// Static map elements
		antiAliasPaint.setColor(Color.BLUE);
		antiAliasPaint.setStrokeWidth(10);
		canvas.drawLine(-100, -25, 1500, 265, antiAliasPaint); // Portal Road /
																// NM 533
		antiAliasPaint.setStrokeWidth(5);
		canvas.drawLine(1350, 250, 1230, 1000, antiAliasPaint); // Wrangler Road

		antiAliasPaint.setColor(Color.BLACK);
		canvas.drawLine(0, 5, -150, 900, antiAliasPaint);// West boundary

	}

	/**
	 * Get a rectangle representing the bounds of the known colonies. The
	 * returned rectangle will have bottom and left sides at zero. The top and
	 * right sides will be expanded to fit all the colonies. If the server
	 * connection does not have a valid list of colonies, this method will
	 * return a rectangle with all values at zero.
	 * 
	 * @return the rectangle.
	 */
	private Rect getColonyMapBounds() {
		List<Colony> colonies = ColonyNavigatorActivity.server.getColonies();

		if (colonies == null) {
			// No valid colonies: Return a rect with everything zero.
			return new Rect();
		} else {
			// Colonies obtained

			// Find the most north colony
			double furthestNorth = 0;
			double furthestSouth = 400;// Initial, high, value
			double furthestEast = 0;
			double furthestWest = 400;// Initial, high, value
			for (Colony colony : colonies) {
				double colonyY = colony.getY();
				if (colonyY > furthestNorth) {
					furthestNorth = colonyY;
				}
				if (colonyY < furthestSouth) {
					furthestSouth = colonyY;
				}

				double colonyX = colony.getX();
				if (colonyX > furthestEast) {
					furthestEast = colonyX;
				}

				if (colonyX < furthestWest) {
					furthestWest = colonyX;
				}
			}

			return new Rect((int) Math.round(furthestWest),
					(int) Math.round(furthestNorth),
					(int) Math.round(furthestEast),
					(int) Math.round(furthestSouth));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scaleDetector.onTouchEvent(event);
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			dragStartX = event.getX();
			dragStartY = event.getY();
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP
				|| event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			relativeX += event.getX() - dragStartX;
			relativeY += event.getY() - dragStartY;

			postInvalidate();
		}
		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		scale *= detector.getScaleFactor();
		postInvalidate();
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	/**
	 * Called when the list of colonies is changed
	 * 
	 * @param colonies
	 *            The updated list of colonies
	 */
	@Override
	public void coloniesChanged(List<Colony> colonies) {
		postInvalidate();// Post because another thread is probably calling
							// this.
	}
}
