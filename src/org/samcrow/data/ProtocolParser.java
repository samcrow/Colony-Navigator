package org.samcrow.data;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A utility class for converting between strings sent over the network and data
 * objects
 * 
 * @author Sam Crow
 */
public class ProtocolParser {
	private ProtocolParser() {
	}// Don't allow construction

	/**
	 * Convert a string representation of a set of colonies into a list of
	 * Colony objects in memory. This method will ignore individual malformed
	 * colony entries in the input string. Any malformedness in the string will
	 * simply result in the returned list of colonies being shorter than
	 * expected or empty.
	 * <p>
	 * The following format is expected:
	 * </p>
	 * 
	 * <pre>
	 *  {colony number, x, y, isActive (true or false)};{the same for other colonies ... }
	 * </pre>
	 * 
	 * @param input
	 *            The string to parse
	 * @return The colonies
	 */
	public static List<Colony> stringToColonies(String input) {

		List<Colony> colonies = new LinkedList<Colony>();

		try {
			JSONObject json = new JSONObject(input);

			JSONArray array = json.getJSONArray("colonies");

			for (int i = 0, max = array.length(); i < max; i++) {
				JSONObject colonyJson = array.optJSONObject(i);
				Colony colony = new Colony();

				colony.setActive(colonyJson.optBoolean("active"));
				colony.setVisited(colonyJson.optBoolean("visited"));
				colony.setId(colonyJson.optInt("id", 0));
				colony.setX(colonyJson.optDouble("x", 0));
				colony.setY(colonyJson.optDouble("y", 0));

				colonies.add(colony);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return colonies;
	}

	/**
	 * Convert a colony object into a string representation
	 * 
	 * @param colony
	 *            the object to convert
	 * @return The converted string representation
	 */
	public static String colonyToString(Colony colony) {
		return "{" + colony.getId() + "," + colony.getX() + "," + colony.getY()
				+ "," + (colony.isActive() ? "true" : "false") + "}";
	}
}
