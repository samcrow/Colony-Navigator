package org.samcrow.data;

import java.util.LinkedList;
import java.util.List;

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

		String[] parts = input.split(";");

		for (String colonyString : parts) {
			// colonyString should be in the format
			// "{colony number, x, y, isActive (true or false)}"
			try {
				// Check that everything about this string is right

				// Regular expressions yay!
				// This one matches { (one or more non-comma characters) , (...)
				// }
				assert (colonyString.matches("\\{[^,]+,[^,]+,[^,]+,[^,]+\\}"));

				// Remove the { from the beginning and the } from the end
				colonyString = colonyString.substring(1);
				colonyString = colonyString.substring(0,
						colonyString.length() - 1);
				colonyString = colonyString.trim();

				// Another regular expression! Yay!
				// This one matches a comma with zero or more whitespace
				// characters on either side
				String[] colonyParts = colonyString.split("[\\s]*,[\\s]*");
				assert (colonyParts.length == 4);

				int colonyNumber = Integer.valueOf(colonyParts[0]);
				double x = Double.valueOf(colonyParts[1]);
				double y = Double.valueOf(colonyParts[2]);
				boolean active = colonyParts[3].equalsIgnoreCase("true") ? true
						: false;

				colonies.add(new Colony(colonyNumber, x, y, active));

			} catch (Throwable e) {// If anything goes wrong
				continue;// Stop working on this colony string. Go on to the
							// next one.
			}

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
