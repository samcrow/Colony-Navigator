package org.samcrow.util;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Transforms coordinates from GPS latitude/longitude into local colony
 * coordinates. <br />
 * To do this, this class takes the GPS coordinates adn:
 * <ol>
 * <li>Translates them</li>
 * <li>Rotates them around the local zero point</li>
 * <li>Scales them around the local zero point</li>
 * </ol>
 * 
 * @author Sam Crow
 */
public class CoordinateTransformer {

	private Matrix matrix;

	/**
	 * Constructor that uses the local position and latitude/longitude of 2
	 * points to calculate offsets, rotation, & scale
	 * 
	 * @param point1
	 *            One point to use as a reference. This is the point that is
	 *            used to calculate offsets.
	 * @param point2
	 *            Another point to use as a reference
	 */
	public CoordinateTransformer(MapPoint point1, MapPoint point2) {

		matrix = new Matrix();

		float[] sourcePoints = new float[] { (float) point1.getLongitude(), // x0
				(float) point1.getLatitude(), // y0
				(float) point2.getLongitude(), // x1
				(float) point2.getLatitude() // y1
		};

		float[] destPoints = new float[] { (float) point1.getX(),
				(float) point1.getY(), (float) point2.getX(),
				(float) point2.getY() };

		matrix.setPolyToPoly(sourcePoints, 0, destPoints, 0, 2);

		System.out.println(matrix);
	}

	/**
	 * Transform given GPS coordinates into local coordinates.
	 * 
	 * @param longitude
	 *            The longitude (X-axis location)
	 * @param latitude
	 *            The latitude (Y-axis location);
	 * @return A point with the transformed coordinates
	 */
	public PointF toLocal(double longitude, double latitude) {
		// Explicitly extract the primitive values from the objects
		double x = longitude;
		double y = latitude;
		float[] points = new float[] { (float) x, (float) y };

		matrix.mapPoints(points);

		return new PointF(points[0], points[1]);
	}

	/**
	 * Transform the given local coordinates into GPS coordinates
	 * 
	 * @param x
	 *            The X location
	 * @param y
	 *            The y location
	 * @return A point with the longitude mapped to x and the latitude mapped to
	 *         y
	 */
	public PointF toGps(float x, float y) {

		Matrix inverse = new Matrix();
		boolean success = matrix.invert(inverse);// Make inverse the inverse matrix of the main matrix

		if (!success) {
			throw new RuntimeException("Matrix could not be inverted");
		}

		float[] points = new float[] { x, y };

		inverse.mapPoints(points);

		return new PointF(points[0], points[1]);

	}
}
