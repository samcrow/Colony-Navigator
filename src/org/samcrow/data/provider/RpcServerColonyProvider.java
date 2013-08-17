package org.samcrow.data.provider;

import org.samcrow.data.Colony;
import org.samcrow.data.ColonySet;

/**
 * Connects to the JSON RPC server to get colony information. Also stores information on the memory card for backup.
 * @author Sam Crow
 *
 */
public class RpcServerColonyProvider implements ColonyProvider {

	private ColonySet colonies = new ColonySet();
	
	private ColonyProvider cardProvider = new MemoryCardDataProvider();
	
	/**
	 * 
	 */
	public RpcServerColonyProvider() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#getColonies()
	 */
	@Override
	public ColonySet getColonies() {
		return colonies;
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColonies()
	 */
	@Override
	public void updateColonies() throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColony(org.samcrow.data.Colony)
	 */
	@Override
	public void updateColony(Colony colony)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

}
