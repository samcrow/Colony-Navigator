package org.samcrow.data;

import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.ShapeDrawable;

/**
 * Stores data for one colony
 * 
 * @author Sam Crow
 */
public class Colony extends ShapeDrawable {

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The colony's identifier
	 * @param x
	 *            The colony's X location in meters
	 * @param y
	 *            The colony's Y location in meters
	 */
	public Colony(int id, double x, double y, boolean active) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.active = active;
	}

	/** The colony's identifier */
	protected int id;

	/** The colony's X-coordinate in meters east of the southwest corner */
	protected double x;
	/** The colony's Y-coordinate in meters north of the southwest corner */
	protected double y;

	/** If the colony is currently active */
	protected volatile boolean active;

	/** If the colony has been visited by a human */
	protected volatile boolean visited;

	/**
	 * Get the colony's X-coordinate in meters east of the southwest corner
	 * 
	 * @return the colony's X-coordinate in meters east of the southwest corner
	 */
	public synchronized double getX() {
		return x;
	}

	/**
	 * Set the colony's X-coordinate in meters east of the southwest corner
	 * 
	 * @param x
	 *            The colony's X-coordinate location
	 */
	public synchronized void setX(double x) {
		this.x = x;
	}

	/**
	 * Get the colony's Y-coordinate in meters north of the southwest corner
	 * 
	 * @return the colony's Y-coordinate in meters north of the southwest corner
	 */
	public synchronized double getY() {
		return y;
	}

	/**
	 * Set the colony's Y-coordinate in meters north of the southwest corner
	 * 
	 * @param y
	 *            The colony's Y-coordinate location
	 */
	public synchronized void setY(double y) {
		this.y = y;
	}

	/**
	 * Get if the colony is active
	 * 
	 * @return if the colony is active
	 */
	public synchronized boolean isActive() {
		return active;
	}

	/**
	 * Set if the colony is active
	 * 
	 * @param active
	 *            if the colony is active
	 */
	public synchronized void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Get if the colony has been visited
	 * 
	 * @return if the colony has been visited
	 */
	public synchronized boolean isVisited() {
		return visited;
	}

	/**
	 * Set if this colony has been visited
	 * 
	 * @param visited
	 *            If the colony has been visited
	 */
	public synchronized void setVisited(boolean visited) {
		this.visited = visited;
	}

	/**
	 * Get the colony's ID
	 * 
	 * @return the ID
	 */
	public synchronized int getId() {
		return id;
	}

	/**
	 * Convert this colony into a {@link JSONObject}.
	 * 
	 * @return A JSON object representation of this colony
	 */
	public synchronized JSONObject toJSON() {
		JSONObject object = new JSONObject();

		try {
			for (Field field : getClass().getFields()) {
				// For each field of this class (id, x, y, active, etc.), add it
				// to the JSON data
				object.put(field.getName(), field.get(this));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Colony #" + id + " at (" + x + ", " + y + "), "
				+ (active ? "active" : "inactive");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + id;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Colony)) {
			return false;
		}
		Colony other = (Colony) obj;
		if (active != other.active) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

}
