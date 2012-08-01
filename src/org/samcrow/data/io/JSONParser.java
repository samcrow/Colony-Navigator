package org.samcrow.data.io;

import org.json.JSONException;
import org.json.JSONObject;
import org.samcrow.data.Colony;

/**
 * Parses and encodes JSON
 * @author samcrow
 */
public class JSONParser implements Parser<Colony> {

	@Override
	public Colony parseOne(String oneString) {
		Colony colony = new Colony();

		try {
			colony.fromJSON(new JSONObject(oneString));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return colony;
	}

	@Override
	public String encodeOne(Colony value) {
		return value.toJSON().toString();
	}

}
