package org.samcrow.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.samcrow.data.Colony;
import org.samcrow.data.JSONSerializable;
import org.samcrow.data.ProtocolParser;

import android.app.Activity;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

/**
 * <p>
 * Contains a plethora of threads that manage connections to the server. This
 * class is designed to completely hide the complex technical aspects of the
 * connection and data synchronization.
 * </p>
 * 
 * Involved threads:
 * <ul>
 * <li><strong>ServerConnectionTask:</strong> Connects to the server. Modifies
 * the socket and the input and output streams.</li>
 * <li><strong>ColonyRequestTask:</strong> Gets the list of colonies from the
 * server. Accesses the input and output streams. Modifies the list of colonies.
 * </li>
 * <li><strong>ColonySubmitTask:</strong> Sends updated data for one colony to
 * the server. Accesses the input and output streams. Modifies the list of
 * colonies.
 * </ul>
 * 
 * @author Sam Crow
 */
public class ServerConnection extends ColonyChangeCallbackManager {

	/** A reference to the application activity that uses this */
	protected Activity activity;

	// Configuration
	/** The IP address of the server to connect to */
	private String ipAddress;
	/** The port number on the server to connect to */
	private int port;

	/** If this device is currently connected to the server */
	private volatile boolean connected = false;

	// Socket and I/O things
	/** The TCP socket used for communication with the server */
	private Socket socket;
	/** The stream used to send data to the server */
	private PrintStream output;
	/** The stream used to receive data from the server */
	private BufferedReader input;

	// Colony change callback things

	/**
	 * Constructor. This constructor spawns a separate thread that actually
	 * makes the connection.
	 * 
	 * @param activity
	 *            The activity that uses this class. Usually, just pass in
	 * 
	 *            <pre>
	 * this
	 * </pre>
	 * 
	 *            .
	 * @param ipAddress
	 *            The server's IP address
	 * @param port
	 *            The port on the server to connect to
	 */
	public ServerConnection(Activity activity, String ipAddress, int port) {
		synchronized (this) {
			this.activity = activity;
			this.ipAddress = ipAddress;
			this.port = port;
		}
		// Create and start a new thread to connect to the server.
		new ServerConnectionTask().start();
	}

	/**
	 * Get the current list of known colonies
	 * 
	 * @return the colonies. This may be null.
	 */
	@Override
	public synchronized List<Colony> getColonies() {
		synchronized (colonies) {
			return colonies;
		}
	}

	/**
	 * Spawn a thread to separately push an updated colony entry to the server.
	 * 
	 * @param colony
	 *            The colony to update.
	 */
	public synchronized void updateColony(Colony colony) {
		// The colony, as a reference to an actual object in the list,
		// should already have been updated in the list in memory.

		// Spawn and start the thread
		new ColonySubmitTask(colony).start();
		// Spawn and start the thread to write the new data to the file
		new ColonySubmitFileTask().start();
	}

	/**
	 * This thread connects to the server, opens the input and output streams,
	 * and spawns a {@link ColonyRequestTask} to get the list of known colonies.
	 * 
	 * @author Sam Crow
	 */
	private class ServerConnectionTask extends NetworkTask {

		public ServerConnectionTask() {
			super("Server Connection Task");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			synchronized (ServerConnection.this) {// Everything in this method
				// is synchronized
				// with regard to the socket
				try {
					socket = new Socket(ipAddress, port);
					connected = true;
				} catch (UnknownHostException e) {
					e.printStackTrace();
					connected = false;
				} catch (IOException e) {
					e.printStackTrace();
					connected = false;
				}

				if (connected == false || socket == null) {
					// Give up this time.
					displayError("Connection error");
					return;
				}

				try {
					output = new PrintStream(socket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
					connected = false;
				}

				if (connected == false || socket == null || output == null) {
					// Give up this time.
					displayError("Error opening output stream");
					return;
				}

				try {
					input = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
					connected = false;
				}

				if (connected == false || socket == null || output == null
						|| input == null) {
					// Give up this time.
					displayError("Error opening input stream");
					return;
				}
			}

			// Spawn another thread to get the set of colonies
			new ColonyRequestTask().start();
		}

	}

	/**
	 * Requests the list of colonies from the server and stores it in the member
	 * variable colonies.
	 * 
	 * @author Sam Crow
	 */
	private class ColonyRequestTask extends NetworkTask {

		public ColonyRequestTask() {
			super("Colony Request");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			waitForConnection();

			// Keep any other request thread from running while this one is
			// working
			synchronized (ServerConnection.this) {

				output.println("get-colonies");

				String line = waitForLine();

				// Now 'line' represents the response
				colonies = ProtocolParser.stringToColonies(line);

				fireCallback();// Notify other objects that the set of colonies
								// has changed
			}
		}
	}

	/**
	 * A task that submits updated colony information to the server. The format
	 * is
	 * 
	 * <pre>
	 * update-colony{[colony ID],[x],[y],[active (true or false)]}\n
	 * </pre>
	 * 
	 * @author Sam Crow
	 */
	private class ColonySubmitTask extends NetworkTask {

		private Colony colonyToUpdate;

		public ColonySubmitTask(Colony colonyToUpdate) {
			super("Colony Submit Task");
			this.colonyToUpdate = colonyToUpdate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			waitForConnection();

			// Don't let any other thread modify anything about the
			// ServerConnection while this one is working on it
			synchronized (ServerConnection.this) {
				output.println("update-colony"
						+ ProtocolParser.colonyToString(colonyToUpdate));
				waitForLine();
				// should return "success".

				fireCallback();
			}
		}
	}

	/**
	 * This class extends Thread and implements methods that wait for a line of
	 * response text to be received and wait for a connection to the server.<br />
	 * Classes that connect to the server should extend this class to use these
	 * methods.
	 * 
	 * @author Sam Crow
	 */
	private abstract class NetworkTask extends Thread {

		// Override the useful Thread constructor
		/**
		 * Constructor
		 * 
		 * @param threadName
		 *            A name to assign to this thread
		 */
		public NetworkTask(String threadName) {
			super(threadName);
		}

		/**
		 * Block until the input stream receives a line, then return it.
		 * 
		 * @return The line received, or an empty string if something went wrong
		 */
		protected String waitForLine() {

			if (input == null) {// Some other thread may not have initialized
								// the
								// input stream.
				return "";
			}

			String line = null;
			try {
				// Wait for a line to be received
				while ((line = input.readLine()) == null)
					;
			} catch (IOException e) {
			} finally {
				if (line == null) {
					line = "";
				}
			}

			return line;
		}

		/**
		 * Block while attempting to connect to the server. This method returns
		 * when a connection has been made.
		 */
		protected void waitForConnection() {
			while (!connected) {
				// Create a task to try to connect.
				// Do this in the same thread (with the run method).
				new ServerConnectionTask().run();
			}
		}

		/**
		 * Show the user a message
		 * 
		 * @param message
		 *            The message to display
		 */
		protected void displayError(String message) {
			Looper.prepare();
			Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * This class writes the set of colonies to a file named colonies&#46;csv.
	 * 
	 * @author Sam Crow
	 */
	private class ColonySubmitFileTask extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// If the memory card is available and writable
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {

				File directory = Environment.getExternalStorageDirectory();

				if (!directory.exists()) {// If the directory doesn't exist
					directory.mkdirs();// Create it
				}

				// Open a file in the previously provided directory, named
				// colonies.json
				File jsonFile = new File(directory, "colonies.json");

				JSONObject json = new JSONObject();

				synchronized (colonies) {
					for (JSONSerializable colony : colonies) {
						try {
							// Add the JSON of this colony to the JSON array
							json.append("colonies", colony.toJSON());
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				try {
					json.write(new FileWriter(jsonFile));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
