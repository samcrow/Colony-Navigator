package org.samcrow;

import static org.samcrow.ColonyNavigatorActivity.provider;

import java.util.Set;

import org.samcrow.data.Colony;
import org.samcrow.util.CoordinateTransformer;
import org.samcrow.util.MapPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
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

	// Colony location reference points

	public static final double colony415Latitude = 31.870901;
	public static final double colony415Longitude = -109.044345;
	public static final MapPoint colony415 = getColony415();

	public static final double colony928Latitude = 31.873036;
	public static final double colony928Longitude = -109.038751;
	public static final MapPoint colony928 = getColony928();

	private CoordinateTransformer transform = new CoordinateTransformer(
			colony415, colony928);

	{
		float averageLongitude = (float) ((colony415Longitude + colony928Longitude) / 2.0);
		float averageLatitude = (float) ((colony415Latitude + colony928Latitude) / 2.0);

		PointF center = transform.toLocal(averageLongitude, averageLatitude);

		System.out.println("Colony 415 (" + colony415.getX() + ", "
				+ colony415.getY() + ")");
		System.out.println("Colony 928 (" + colony928.getX() + ", "
				+ colony928.getY() + ")");
		System.out.println("Lat/lon center to local (" + center.x + ", "
				+ center.y + ")");
	}

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

	private Rect colonyBounds = getColonyMapBounds();

	/**
	 * Transformation matrix used to transform points from colony coordinates to screen coordinates
	 */
	private Matrix displayTransform = new Matrix();

	/**
	 * A reference to the colony that's currently selected
	 */
	public static Colony selectedColony;

	private static final int colonyLabelColor = Color.rgb(200, 0, 200);

	/**
	 * A paint object that enables antialiasing
	 */
	private static final Paint kAntiAliasPaint = new Paint();
	static {
		kAntiAliasPaint.setAntiAlias(true);
	}

	/* Static import colonies from ColonyNavigatoActivity */

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

		displayTransform.reset();
		//Rotate to convert to true north
		displayTransform.postRotate(-10);
		//Fix to make the map appear in the center initially
		displayTransform.postTranslate(0, colonyBounds.height());
		displayTransform.postScale(scale, -scale);

		/*
		 * When relativeX and relativeY are zero, the origin is centered
		 * on the bottom left corner of the screen ((0, getHeight()) ins screen coordinates)
		 * Map shifts left (viewport right), relativeX goes to negative infinity
		 * Map shifts down (viewport up), relativeY goes to positive infinity
		 */
		displayTransform.postTranslate(relativeX, relativeY);

		// Clear the screen
		canvas.drawColor(Color.WHITE);

		Set<Colony> colonies = provider.getColonies();

		Location location = NavigatorLocationListener.getLocation();
		if (colonies != null) {

			synchronized (colonies) {
				for (Colony colony : colonies) {

					float[] points = new float[] {
							(float) colony.getX(),
							(float) colony.getY()
					};

					displayTransform.mapPoints(points);

					if(inWindow(points)) {

						if(colony == selectedColony) {
							//Draw the colony in red with a larger circle
							kAntiAliasPaint.setColor(Color.RED);
							canvas.drawCircle(points[0],
									points[1], 4, kAntiAliasPaint);

							//Draw some extra accoutrements around it

							//Pixels offset from the center of the circle to the tip of the triangle
							final short triOffset = 8;
							//Width of the triangle
							final short triWidth = 10;
							//Length of the triangle
							final short triLength = 20;

							//Top triangle
							Path triangle = new Path();
							triangle.moveTo(points[0], points[1] - triOffset * scale);
							triangle.lineTo((float) (points[0] + (triWidth / 2.0) * scale), points[1] - (triOffset + triLength) * scale);
							triangle.lineTo((float) (points[0] - (triWidth / 2.0) * scale), points[1] - (triOffset + triLength) * scale);
							triangle.close();

							canvas.drawPath(triangle, kAntiAliasPaint);

							//Bottom triangle
							triangle = new Path();
							triangle.moveTo(points[0], points[1] + triOffset * scale);
							triangle.lineTo((float) (points[0] + (triWidth / 2.0) * scale), points[1] + (triOffset + triLength) * scale);
							triangle.lineTo((float) (points[0] - (triWidth / 2.0) * scale), points[1] + (triOffset + triLength) * scale);
							triangle.close();

							canvas.drawPath(triangle, kAntiAliasPaint);

							//Left triangle
							triangle = new Path();
							triangle.moveTo(points[0] - triOffset * scale, points[1]);
							triangle.lineTo(points[0] - (triOffset + triLength) * scale, (float) (points[1] + (triWidth / 2.0) * scale));
							triangle.lineTo(points[0] - (triOffset + triLength) * scale, (float) (points[1] - (triWidth / 2.0) * scale));
							triangle.close();

							canvas.drawPath(triangle, kAntiAliasPaint);

						}
						else {//Not selected, draw it as usual
							kAntiAliasPaint.setColor(Color.BLACK);
							canvas.drawCircle(points[0],
									points[1], 2, kAntiAliasPaint);
						}

						kAntiAliasPaint.setColor(colonyLabelColor);
						kAntiAliasPaint.setTextSize(10 * scale);
						canvas.drawText(String.valueOf(colony.getId()),
								points[0] + 2 * scale, points[1] + 3 * scale,
								kAntiAliasPaint);
					}
				}
			}
		}
		if (location != null) {
			//Get the location in colony coordinates
			PointF pointColonyCoords = transform.toLocal(location.getLongitude(), location.getLatitude());

			float[] locationPoint = new float[] { pointColonyCoords.x, pointColonyCoords.y };

			displayTransform.mapPoints(locationPoint);

			if(inWindow(locationPoint)) {
				//Draw a circle at the user's current location
				canvas.drawCircle(locationPoint[0], locationPoint[1], 5 * scale, kAntiAliasPaint);
			}

			if(selectedColony != null) {
				//Have location and selected colony
				//Draw a line between them
				int oldColor = kAntiAliasPaint.getColor();
				kAntiAliasPaint.setColor(Color.GREEN);
				kAntiAliasPaint.setColor(oldColor);

				float[] selectedPoint = new float[] { (float) selectedColony.getX(), (float) selectedColony.getY() };
				displayTransform.mapPoints(selectedPoint);

				canvas.drawLine(locationPoint[0], locationPoint[1], selectedPoint[0], selectedPoint[1], kAntiAliasPaint);

			}
		}

		// Static map elements
		kAntiAliasPaint.setColor(Color.BLUE);
		kAntiAliasPaint.setStrokeWidth(10);

		//Portal road
		float[] portalRoadA = new float[] { -100, -25 };
		float[] portalRoadB = new float[] { 1500, 265 };
		displayTransform.mapPoints(portalRoadA);
		displayTransform.mapPoints(portalRoadB);
		canvas.drawLine(portalRoadA[0], portalRoadA[1], portalRoadB[0], portalRoadB[1],
				kAntiAliasPaint);

		kAntiAliasPaint.setStrokeWidth(5);

		//
		//		canvas.drawLine(1350, transformY(250), 1230, transformY(1000),
		//				kAntiAliasPaint); // Wrangler
		//		// Road
		//
		//		kAntiAliasPaint.setColor(Color.BLACK);
		//		canvas.drawLine(0, transformY(5), -150, transformY(900),
		//				kAntiAliasPaint);// West boundary
	}

	/**
	 * Get a rectangle representing the bounds of the known colonies. The
	 * returned rectangle will have bottom and left sides at zero. The top and
	 * right sides will be expanded to fit all the colonies.
	 * 
	 * @return the rectangle.
	 */
	private Rect getColonyMapBounds() {
		Set<Colony> colonies = provider.getColonies();

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

	/**
	 * Check if a given set of points, in local window coordinates, are inside the window
	 * @param points An array with 0 => x and 1 -> y
	 * @return True if the point is in the window, otherwise false
	 */
	private boolean inWindow(float[] points) {
		return points[0] >= 0 && points[0] <= getWidth() && points[1] >= 0 && points[1] <= getHeight();
	}

	/**
	 * Get the selected colony
	 * @return the selected colony
	 */
	public synchronized Colony getSelectedColony() {
		return selectedColony;
	}

	/**
	 * Set the selected colony
	 * @param selectedColony the colony to set
	 */
	public synchronized void setSelectedColony(Colony selectedColony) {
		boolean changed = selectedColony != this.selectedColony;
		this.selectedColony = selectedColony;
		if(changed) {
			invalidate();
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

			float deltaX = (event.getX() - dragStartX) / (scale * 25);
			float deltaY = (event.getY() - dragStartY) / (scale * 25);

			relativeX += deltaX;
			relativeY += deltaY;

			invalidate();
		}
		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		//		float scaleFactor = detector.getScaleFactor();
		//		if(scaleFactor > 1) {
		scale *= detector.getScaleFactor();
		invalidate();
		//		}
		//		else {
		//			//Trying to scale down (zoom out)
		//			//Don't allow it if the colony bounds are within and smaller than the window
		//			if(!new Rect(0, 0, getWidth(), getHeight()).contains(colonyBounds)) {
		//				scale *= detector.getScaleFactor();
		//				invalidate();
		//			}
		//			else {
		//				System.out.println("Trying zoom out too much. Preventing that.");
		//			}
		//		}
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	private static final MapPoint getColony415() {

		for (Colony colony :  provider.getColonies()) {
			if (colony.getId() == 415) {
				return new MapPoint(colony, colony415Latitude,
						colony415Longitude);
			}
		}

		return null;
	}

	private static final MapPoint getColony928() {

		for (Colony colony :  provider.getColonies()) {
			if (colony.getId() == 928) {
				return new MapPoint(colony, colony928Latitude,
						colony928Longitude);
			}
		}

		return null;
	}

	/**
	 * Set the view to be centered on a colony
	 * @param colony The colony to center the view on
	 */
	public void centerViewOn(Colony colony) {
		float[] point = new float[] { (float) colony.getX(), (float) colony.getY() };

		//Get the in-view coordinates of the colony
		displayTransform.mapPoints(point);
		//point is now the location of the selected colony on the screen in pixels from the top left corner


		relativeX = (float) (getWidth() / 2.0) - point[0];

		relativeY = -(point[1] - (getWidth() / 2f));

		invalidate();
	}
}
