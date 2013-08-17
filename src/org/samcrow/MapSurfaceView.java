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
	private static final MapPoint topLeft = new MapPoint(provider.getColonies().getById(962), 31.87265776, -109.04243);
	private static final MapPoint bottomRight = new MapPoint(provider.getColonies().getById(980), 31.87087500797029, -109.03870950670428);
	@SuppressWarnings("unused")
	private static final MapPoint bottomLeft = new MapPoint(provider.getColonies().getById(567), 31.871036, -109.042678);
	private static final MapPoint topRight = new MapPoint(provider.getColonies().getById(442), 31.872357, -109.0391114);

	private CoordinateTransformer transform = new CoordinateTransformer(
			topLeft, topRight, bottomRight/*, bottomLeft*/);
	
	/**
	 * Background shape alpha (transparency), 0-255
	 */
	private static final int BG_ALPHA = 100;
	/**
	 * Background color for non-focus, non-visited colonies
	 */
	private static final int BG_NORMAL_COLOR = Color.argb(BG_ALPHA / 2, 100, 100, 100); // gray
	/**
	 * Background color for focus colonies
	 */
	private static final int BG_FOCUS_COLOR = Color.argb(BG_ALPHA, 115, 140, 255); // blue
	/**
	 * Background color for visited colonies, both focus and non-focus
	 */
	private static final int BG_VISITED_COLOR = Color.argb(BG_ALPHA, 77, 240, 101); // green
	
	/**
	 * Background circle radius
	 */
	private static final int BG_RADIUS = 20;

	//Hack to make this accessible to the location listener
	public static MapSurfaceView instance;

	{
		instance = this;
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
	private static final Paint paint = new Paint();
	private Path triangle = new Path();
	static {
		paint.setAntiAlias(true);
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
							paint.setColor(Color.RED);
							canvas.drawCircle(points[0],
									points[1], 4, paint);

							//Draw some extra accoutrements around it

							//Pixels offset from the center of the circle to the tip of the triangle
							final short triOffset = 8;
							//Width of the triangle
							final short triWidth = 10;
							//Length of the triangle
							final short triLength = 20;

							triangle.reset();
							triangle.moveTo(points[0], points[1] - triOffset * scale);
							triangle.lineTo((float) (points[0] + (triWidth / 2.0) * scale), points[1] - (triOffset + triLength) * scale);
							triangle.lineTo((float) (points[0] - (triWidth / 2.0) * scale), points[1] - (triOffset + triLength) * scale);
							triangle.close();

							canvas.drawPath(triangle, paint);

							//Bottom triangle
							triangle.reset();
							triangle.moveTo(points[0], points[1] + triOffset * scale);
							triangle.lineTo((float) (points[0] + (triWidth / 2.0) * scale), points[1] + (triOffset + triLength) * scale);
							triangle.lineTo((float) (points[0] - (triWidth / 2.0) * scale), points[1] + (triOffset + triLength) * scale);
							triangle.close();

							canvas.drawPath(triangle, paint);

							//Left triangle
							triangle.reset();
							triangle.moveTo(points[0] - triOffset * scale, points[1]);
							triangle.lineTo(points[0] - (triOffset + triLength) * scale, (float) (points[1] + (triWidth / 2.0) * scale));
							triangle.lineTo(points[0] - (triOffset + triLength) * scale, (float) (points[1] - (triWidth / 2.0) * scale));
							triangle.close();

							canvas.drawPath(triangle, paint);

						}
						else {//Not selected, draw it as usual
							//Draw the colony in a different color if it has not been visited
							if(!colony.isVisited()) {
								if(colony.isFocusColony()) {
									//Draw a semitransparent circle
									paint.setColor(BG_FOCUS_COLOR);
									canvas.drawCircle(points[0], points[1], BG_RADIUS, paint);
								}
								else {
									//Draw a semitransparent circle
									paint.setColor(BG_NORMAL_COLOR);
									canvas.drawCircle(points[0], points[1], BG_RADIUS, paint);
								}
								
								paint.setColor(Color.BLACK);
							}
							else {
								//Draw a semitransparent circle
								paint.setColor(BG_VISITED_COLOR);
								canvas.drawCircle(points[0], points[1], BG_RADIUS, paint);
								
								//Draw the colony in a different color
								paint.setColor(Color.GREEN);
							}
							canvas.drawCircle(points[0],
									points[1], 2, paint);
						}

						paint.setColor(colonyLabelColor);
						paint.setTextSize(10 * scale);
						canvas.drawText(String.valueOf(colony.getId()),
								points[0] + 2 * scale, points[1] + 3 * scale,
								paint);
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
				canvas.drawCircle(locationPoint[0], locationPoint[1], 5 * scale, paint);
			}

			if(selectedColony != null) {
				//Have location and selected colony
				//Draw a line between them
				int oldColor = paint.getColor();
				paint.setColor(Color.GREEN);
				paint.setColor(oldColor);

				float[] selectedPoint = new float[] { (float) selectedColony.getX(), (float) selectedColony.getY() };
				displayTransform.mapPoints(selectedPoint);

				canvas.drawLine(locationPoint[0], locationPoint[1], selectedPoint[0], selectedPoint[1], paint);

			}
		}

		// Static map elements
		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(10);

		//Portal road
		float[] portalRoadA = new float[] { -100, -25 };
		float[] portalRoadB = new float[] { 1500, 265 };
		displayTransform.mapPoints(portalRoadA);
		displayTransform.mapPoints(portalRoadB);
		canvas.drawLine(portalRoadA[0], portalRoadA[1], portalRoadB[0], portalRoadB[1],
				paint);

		paint.setStrokeWidth(5);

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
		boolean changed = selectedColony != MapSurfaceView.selectedColony;
		MapSurfaceView.selectedColony = selectedColony;
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
}
