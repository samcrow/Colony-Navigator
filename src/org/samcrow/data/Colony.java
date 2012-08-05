package org.samcrow.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores data for one colony
 * 
 * @author Sam Crow
 */
public class Colony implements JSONSerializable {

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

	public Colony() {
		this(0, 0, 0, false);
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
	 * The date/time that this colony was modified.
	 * If it was not modified by Colony Navigator since
	 * it was imported from the CSV file, this should be null.
	 * Otherwise, it should be the date when the user last
	 * modified the data using the Colony Navigator application.
	 */
	protected Date modified = null;

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
		updateModifiedDate();
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
		updateModifiedDate();
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
		updateModifiedDate();
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
		updateModifiedDate();
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
	 * Update the modified date/time and set it to now.
	 * Every method that sets a field should call this method.
	 */
	protected final void updateModifiedDate() {
		modified = new Date();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.samcrow.data.JSONSerializable#toJSON()
	 */
	@Override
	public synchronized JSONObject toJSON() {
		JSONObject object = new JSONObject();

		try {

			object.put("id", id);
			object.put("x", x);
			object.put("y", y);
			object.put("active", active);
			object.put("visited", visited);

			//Visited date/time: Should be JSON's NULL if null, or formatted
			//using DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)

			if(modified == null) {
				object.put("modified", JSONObject.NULL);
			} else {
				object.put("modified", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(modified));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return object;
	}

	@Override
	public void fromJSON(JSONObject json) {
		id = json.optInt("id", id);
		x = json.optDouble("x", x);
		y = json.optDouble("y", y);
		active = json.optBoolean("active", active);
		visited = json.optBoolean("visited", visited);

		Object modifiedObject = json.opt("modified");
		if(modifiedObject == null || JSONObject.NULL.equals(modifiedObject)) {
			//Modified time specified as null; make it so
			modified = null;
		} else if(modifiedObject instanceof String) {
			try {
				//Modified time given; parse it
				modified = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).parse((String) modifiedObject);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Colony #" + id + " at (" + x + ", " + y + "), "
				+ (active ? "active" : "inactive")+", "+(visited ? "visited" : "not visited");
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

	/**
	 * Set this colony's ID
	 * 
	 * @param id
	 *            the ID to set
	 */
	public void setId(int id) {
		this.id = id;

	}

}
