package org.samcrow.data.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.samcrow.data.Colony;
import org.samcrow.data.io.JSONParser;

import android.os.Looper;
import android.widget.Toast;

/**
 * Gets colonies from a server over the network
 * @author Sam Crow
 */
public class NetworkColonyProvider implements ColonyProvider {

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


	/**
	 * The current set of colonies
	 */
	private volatile Set<Colony> colonies;

	/**
	 * Constructor
	 * @param ipAddress The IP address to connect to
	 * @param port The remote port to connect to
	 */
	public NetworkColonyProvider(String ipAddress, int port) {
		synchronized (this) {
			this.ipAddress = ipAddress;
			this.port = port;
		}
		// Create and start a new thread to connect to the server.
		new ServerConnectionTask().start();
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
		new ColonySubmitAllTask().start();

	}

	/* (non-Javadoc)
	 * @see org.samcrow.data.provider.ColonyProvider#updateColony(org.samcrow.data.Colony)
	 */
	@Override
	public void updateColony(Colony colony)
			throws UnsupportedOperationException {
		new ColonySubmitTask(colony).start();

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
			synchronized (NetworkColonyProvider.this) {// Everything in this method
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
			synchronized (NetworkColonyProvider.this) {

				JSONObject request = new JSONObject();
				try {
					request.put("request", "get-colonies");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				output.println(request.toString());

				String line = waitForLine();

				try {
					colonies = new JSONParser().parseAll(new JSONObject(line).getJSONArray("colonies"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
			synchronized (NetworkColonyProvider.this) {
				JSONObject request = new JSONObject();
				try {
					request.put("request", "update");
					request.put("colony", colonyToUpdate.toJSON());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				output.println(request.toString());

				waitForLine();
				// should return "success".

			}
		}
	}

	private class ColonySubmitAllTask extends NetworkTask {
		public ColonySubmitAllTask() {
			super("Colony submit all task");
		}

		@Override
		public void run() {

			waitForConnection();

			synchronized(NetworkColonyProvider.this) {
				JSONArray colonyArray = new JSONParser().encodeAll(colonies);

				JSONObject request = new JSONObject();
				try {
					request.put("request", "update_all");
					request.put("colonies", colonyArray);

					output.println(request.toString());

					waitForLine();

				} catch (JSONException e) {
					e.printStackTrace();
				}

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
			Toast.makeText(null, message, Toast.LENGTH_SHORT).show();
		}
	}

}
