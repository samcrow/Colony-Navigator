package org.samcrow.util;

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

	/**
	 * X offset: GPS longitude + offsetX = local X location
	 */
	private double offsetX;

	/**
	 * Y offset: GPS latitude + offsetY = local Y location
	 */
	private double offsetY;

	/**
	 * Rotation offset: Local coordinates rotated this many radians
	 * counter-clockwise from above = local coordinates for the next step
	 */
	private double rotation;

	/**
	 * Scale ratio: Local coordinates scaled by this much = final local
	 * coordinates
	 */
	private double scale;

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

		offsetX = point1.getX() - point1.getLongitude();

		offsetY = point1.getY() - point1.getLatitude();

		// Calculate the angles between the points in lat/lon and local
		// coordinates
		{
			double gpsXDelta = point2.getLongitude() - point1.getLongitude();
			double gpsYDelta = point2.getLatitude() - point1.getLatitude();
			/*
			 * 
			 * XDelta _______ | / | / YDelta | / | / | / angle |/________
			 */

			double gpsAngle = Math.atan2(gpsYDelta, gpsXDelta); // (y, x)

			double localXDelta = point2.getX() - point1.getX();
			double localYDelta = point2.getY() - point1.getY();

			double localAngle = Math.atan2(localYDelta, localXDelta);

			rotation = localAngle - gpsAngle;

			// Calculate the scale using the diagonal distance between the
			// points
			double gpsDistance = Math.sqrt(square(gpsXDelta)
					+ square(gpsYDelta));

			double localDistance = Math.sqrt(square(localXDelta)
					+ square(localYDelta));

			scale = localDistance / gpsDistance;

		}

	}

	/**
	 * Transform given GPS coordinates into local coordinates. The parameters
	 * are given as Double objects, so this function will modify them without
	 * returning anything.
	 * 
	 * @param longitude
	 *            The longitude (X-axis location)
	 * @param latitude
	 *            The latitude (Y-axis location);
	 */
	public void transform(Double longitude, Double latitude) {
		// Explicitly extract the primitive values from the objects
		double x = longitude.doubleValue();
		double y = latitude.doubleValue();

		x += offsetX;
		y += offsetY;

		// To manipulate the rotation, get the actual angle (counterclockwise
		// from above from facing east) of the vector from the origin to these
		// coordinates
		double angle = Math.atan2(y, x);
		// And also the distance of the vector

		double distance = Math.sqrt(square(x) + square(y));
		// Scale that vector distance
		distance *= scale;

		angle += rotation;

		// Calcualte new X and Y values with the modified rotation
		x = Math.cos(angle) * distance;
		y = Math.sin(angle) * distance;
	}

	/**
	 * Calculate the square of a numerical value
	 * 
	 * @param value
	 *            The value to square
	 * @return The squared value
	 */
	private static double square(double value) {
		return value * value;
	}
}
