package org.samcrow.data;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * A set of colonies, with additional methods
 * for accessing colonies by various criteria
 * @author Sam Crow
 */
public class ColonySet extends LinkedHashSet<Colony> {


	public ColonySet() {
		super();
	}

	public ColonySet(Collection<? extends Colony> collection) {
		super(collection);
	}

	public ColonySet(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	public ColonySet(int capacity) {
		super(capacity);
	}

	private static final long serialVersionUID = -4336476707706103141L;

	/**
	 * Get a colony with a given ID in this set
	 * @param id The ID of the colony to find
	 * @return The colony with that ID, or null if no such colony was found
	 */
	public Colony getById(int id) {
		for(Colony colony : this) {
			if(colony.getId() == id) {
				return colony;
			}
		}

		return null;
	}

}
