package org.samcrow.data.provider;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.samcrow.data.Colony;
import org.samcrow.data.io.CSVFileParser;
import org.samcrow.data.io.FileParser;

/**
 * Provides colonies from data stored on the memory card.
 * This class first looks for a CSV file named colonies.csv in the directory specified by {@link #kDir}.
 * It parses that data.
 * Then it looks for a JSON file named colonies.json in the same directory and parses that data.
 * In the event of any conflict between the two files, the version in colonies.json takes precedence.
 * 
 * When writing colony data, this implementation writes it to colonies.json. It does not modify colonies.csv.
 * 
 * @author Sam Crow
 */
public class MemoryCardDataProvider implements ColonyProvider {

	private Set<Colony> colonies = new HashSet<Colony>();

	/**
	 * The absolute path to the folder where data should be read and written.
	 * This must begin and end with a slash.
	 * The folders don't need to exist on the file system. This implementation
	 * will try to create them if necessary.
	 */
	private static final String kDir = "/mnt/extSdCard/";

	public MemoryCardDataProvider() {
		File dir = new File(kDir);
		//Create the directory if it doesn't already exist
		dir.mkdirs();

		File csvFile = new File(kDir+"colonies.csv");
		File jsonFile = new File(kDir+"colonies.json");

		//Verify that this application has permission to write each of the files
		if(csvFile.exists()) assert csvFile.canWrite();
		if(jsonFile.exists()) assert jsonFile.canWrite();

		//Case 1: Application hasn't been run before
		//colonies.csv exists, colonies.json does not
		if(csvFile.exists() && !jsonFile.exists()) {

			//Read the CSV and get the colonies into memeory
			FileParser<Colony> csvParser = new CSVFileParser(csvFile);
			colonies = csvParser.parse();
		}
	}


	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#getColonies()
	 */
	@Override
	public Set<Colony> getColonies() {
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
