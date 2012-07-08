package org.samcrow.net;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.samcrow.data.Colony;

/**
 * Handles callbacks to notify other classes when the set of known colonies
 * changes.<br />
 * {@link ServerConnection} extends this class to allow it to use these
 * callbacks.
 * 
 * @author Sam Crow
 */
public class ColonyChangeCallbackManager {

	/** The known colonies */
	protected volatile List<Colony> colonies = new LinkedList<Colony>();

	/** The things that will be notified */
	private Set<ColonyChangeListener> listeners = new HashSet<ColonyChangeListener>();

	/**
	 * Get a reference to the current list of colonies.
	 * 
	 * @return The list of colonies.
	 */
	public List<Colony> getColonies() {
		return colonies;
	}

	/**
	 * Send a signal to all registered listeners that the list of colonies has
	 * changed. Each one will receive the new set of colonies.
	 */
	protected final void fireCallback() {
		for (ColonyChangeListener listener : listeners) {
			listener.coloniesChanged(colonies);
		}
	}

	/**
	 * Add an object to be notified when the set of colonies changes.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public final void addColonyChangeListener(ColonyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener. The listener will no longer receive notification when
	 * the set of colonies changes.
	 * 
	 * @param listener
	 *            The listener to remove
	 * @return True if the listener was removed successfully, false if the
	 *         listener was not previously added.
	 */
	public final boolean removeColonyChangeListener(
			ColonyChangeListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * An interface for a class that receives notification when the set of known
	 * colonies changes
	 * 
	 * @author Sam Crow
	 */
	public static interface ColonyChangeListener {

		/**
		 * Called when the list of colonies changes
		 * 
		 * @param colonies
		 *            The updated list of colonies
		 */
		public void coloniesChanged(List<Colony> colonies);
	}
}
