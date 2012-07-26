package org.samcrow;

import java.util.Set;

import org.samcrow.data.Colony;
import org.samcrow.data.HardCodedColonies;

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
public class MapSurfaceView extends View implements OnScaleGestureListener {

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
	 * A paint object that enables antialiasing
	 */
	private static final Paint kAntiAliasPaint = new Paint();
	static {
		kAntiAliasPaint.setAntiAlias(true);
	}

	/**
	 * The boundaries of the colonies
	 */
	private Rect colonyBounds = getColonyMapBounds();

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

		canvas.translate(relativeX / 30f * scale, relativeY / 30f * scale);
		canvas.scale(scale, scale, (canvas.getWidth()) / 2f,
				(canvas.getHeight()) / 2f);

		// Clear the screen
		canvas.drawColor(Color.WHITE);

		kAntiAliasPaint.setColor(Color.RED);
		canvas.drawRect(getColonyMapBounds(), kAntiAliasPaint);
		kAntiAliasPaint.setColor(Color.BLACK);

		Set<Colony> colonies = HardCodedColonies.colonies;

		Location location = NavigatorLocationListener.getLocation();
		if (colonies != null) {

			synchronized (colonies) {
				for (Colony colony : colonies) {
					kAntiAliasPaint.setColor(Color.BLACK);
					canvas.drawCircle((float) colony.getX(),
							(float) colony.getY(), 2, kAntiAliasPaint);

					kAntiAliasPaint.setColor(Color.MAGENTA);
					canvas.drawText(String.valueOf(colony.getId()),
							(float) colony.getX(), (float) colony.getY(),
							kAntiAliasPaint);
				}
			}
		}
		if (location != null) {
			canvas.drawText("Latitude " + location.getLatitude()
					+ " Longitude " + location.getLongitude(), 0, 100,
					kAntiAliasPaint);
		}

		// Static map elements
		kAntiAliasPaint.setColor(Color.BLUE);
		kAntiAliasPaint.setStrokeWidth(10);
		canvas.drawLine(-100, -25, 1500, 265, kAntiAliasPaint); // Portal
																// Road /
																// NM 533
		kAntiAliasPaint.setStrokeWidth(5);
		canvas.drawLine(1350, 250, 1230, 1000, kAntiAliasPaint); // Wrangler
																	// Road

		kAntiAliasPaint.setColor(Color.BLACK);
		canvas.drawLine(0, 5, -150, 900, kAntiAliasPaint);// West boundary
	}

	/**
	 * Transform a Y coordinate to vertically flip everything
	 * 
	 * @param y
	 * @return
	 */
	private double transformYCoordinate(double y) {

		// TODO

		return y;
	}

	/**
	 * Get a rectangle representing the bounds of the known colonies. The
	 * returned rectangle will have bottom and left sides at zero. The top and
	 * right sides will be expanded to fit all the colonies.
	 * 
	 * @return the rectangle.
	 */
	private Rect getColonyMapBounds() {
		Set<Colony> colonies = HardCodedColonies.colonies;

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
}
