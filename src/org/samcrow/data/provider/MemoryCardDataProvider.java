package org.samcrow.data.provider;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.samcrow.data.Colony;
import org.samcrow.data.io.CSVFileParser;
import org.samcrow.data.io.FileParser;
import org.samcrow.data.io.JSONFileParser;

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

	/**
	 * The name, including the file extension, of the CSV file to use
	 */
	private static final String kCsvFileName = "colonies.csv";

	/**
	 * The name, including the file extension, of the JSON file to use
	 */
	private static final String kJsonFileName = "colonies.json";

	public MemoryCardDataProvider() {
		File dir = new File(kDir);
		//Create the directory if it doesn't already exist
		dir.mkdirs();

		File csvFile = new File(kDir+kCsvFileName);
		File jsonFile = new File(kDir+kJsonFileName);

		//Verify that this application has permission to write each of the files
		if(csvFile.exists()) assert csvFile.canWrite();
		if(jsonFile.exists()) assert jsonFile.canWrite();

		//Case 1: Application hasn't been run before
		//colonies.csv exists, colonies.json does not
		if(csvFile.exists() && !jsonFile.exists()) {

			//Read the CSV and get the colonies into memory
			FileParser<Colony> csvParser = new CSVFileParser(csvFile);
			colonies = csvParser.parse();

			//Write the JSON file from memory
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			jsonParser.write(colonies);
		}

		//Case 2: both files exist
		else if(csvFile.exists() && jsonFile.exists()) {


			FileParser<Colony> csvParser = new CSVFileParser(csvFile);
			Set<Colony> csvColonies = csvParser.parse();

			//Write the JSON file from memory
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			Set<Colony> jsonColonies = jsonParser.parse();

			//Put into memory the colonies from the CSV updated with colonies from the JSON file
			colonies = extend(csvColonies, jsonColonies);

			//Write the JSON file from memory
			jsonParser.write(colonies);
		}

		//Cases 3: CSV doesn't exist, JSON does
		else if(!csvFile.exists() && jsonFile.exists()) {
			//Use the JSON file
			FileParser<Colony> jsonParser = new JSONFileParser(jsonFile);
			colonies = jsonParser.parse();
		}

		else {
			String message = "Neither "+csvFile.getAbsolutePath()+" or "+jsonFile.getAbsolutePath()+" exists! Failed to get colonies from the memory card.";
			System.err.println(message);
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
		new FileWriteTask().start();
	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColony(org.samcrow.data.Colony)
	 */
	@Override
	public void updateColony(Colony colony)
			throws UnsupportedOperationException {
		new FileWriteTask().start();

	}

	/**
	 * Extend a set of colonies to reflect changes from a supplemental set
	 * This is based on jQuery's <code>jQuery.extend()</code> function.
	 * 
	 * This method will create and return a new set with the following:
	 * <ul>
	 * <li>Every colony in supplement but not base included as-is</li>
	 * <li>Every colony in base but not supplement included as-is</li>
	 * <li>For every colony in both sets, the copy from base will be ignored
	 * and the copy from supplement will be used</li>
	 * </ul>
	 * Colonies are considered equal if their IDs as returned by {@link Colony#getId()}
	 * are the same.
	 * 
	 * @param base The base set of colonies
	 * @param supplement The supplement set of colonies
	 * @return A new set of colonies reflecting the changes to base made by supplement.
	 * This set will contain references to the same colony objects referred to by
	 * the input sets.
	 */
	private Set<Colony> extend(Set<Colony> base, Set<Colony> supplement) {
		//Note: base and supplement contain references to different colony objects with the same IDs

		Set<Colony> finalSet = new HashSet<Colony>();

		for(Colony colony : base) {
			int id = colony.getId();

			Colony supplementColony = findColonyById(supplement, id);
			if(supplementColony != null) {
				//It's in the supplement set, so just use the version from the supplement
				finalSet.add(supplementColony);
			}
			else {
				//This colony isn't in the supplement. Use the version from the base.
				finalSet.add(colony);
			}
		}
		//Add every colony that's in the supplement
		for(Colony supplementColony : supplement) {
			int id = supplementColony.getId();

			Colony baseColony = findColonyById(base, id);
			if(baseColony == null) {
				//If this colony is in the base set, it's already been added.
				//Here, it isn't in the base set, so it's added to the final set.
				finalSet.add(supplementColony);
			}
		}

		return finalSet;
	}

	/**
	 * Find a colony with given ID in a given set
	 * @param set The set of colonies to search
	 * @param id The colony ID to search for
	 * @return A reference to the colony in the set with the given
	 * ID, or null if no such colony was found
	 */
	private Colony findColonyById(Set<Colony> set, int id) {
		for(Colony colony : set) {
			if(colony.getId() == id) {
				return colony;
			}
		}
		return null;
	}

	/**
	 * A thread that writes the colonies to the JSON file
	 * 
	 * @author Sam Crow
	 */
	private class FileWriteTask extends Thread {

		@Override
		public void run() {
			File file = new File(kDir+kJsonFileName);

			FileParser<Colony> parser = new JSONFileParser(file);
			parser.write(colonies);

		}
	}
}
