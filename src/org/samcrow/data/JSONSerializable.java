package org.samcrow.data;

import org.json.JSONObject;

public interface JSONSerializable {

	/**
	 * Serialize this object into a {@link JSONObject}.
	 * 
	 * @return A JSON object representation of this object
	 */
	public abstract JSONObject toJSON();

	/**
	 * Set all the parameters of this object using data from a given JSON object
	 * 
	 * @param json
	 *            The object to get data from
	 */
	public void fromJSON(JSONObject json);
}