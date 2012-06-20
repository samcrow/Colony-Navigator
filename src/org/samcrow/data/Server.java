package org.samcrow.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * Stores data associated with the server and manages synchronization
 * 
 * @author Sam Crow
 */
public class Server {

	/** The current known colonies */
	protected HashSet<Colony> colonies = new HashSet<Colony>();

	/** The socket connection to the server */
	protected Socket socket;

	/** The stream used to get data from the server */
	protected BufferedReader input;

	/** The stream used to send data to the server */
	protected PrintStream output;

	protected ServerConnectionTask connectThread;

	/**
	 * Constructor
	 * 
	 * @param ipAddress
	 *            The IP address to connect to
	 * @param port
	 *            The port to connect over
	 */
	public Server(String ipAddress, int port) {
		connectThread = new ServerConnectionTask(ipAddress, port);
		connectThread.start();
	}

	protected class ServerConnectionTask extends Thread {

		protected String ipAddress;
		protected int port;

		/**
		 * Constructor
		 * 
		 * @param ipAddress
		 *            The IP address to connect to
		 * @param port
		 *            The port to connect over
		 */
		public ServerConnectionTask(String ipAddress, int port) {
			super("Server connection thread");
			this.ipAddress = ipAddress;
			this.port = port;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				socket = new Socket(ipAddress, port);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {
				input = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				System.err
						.println("Exception getting the input stream for the socket");
				e.printStackTrace();
			}
			try {
				output = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				System.err
						.println("Exception getting the output stream for the socket.");
				e.printStackTrace();
			}

		}

	}

	protected class ColonyDataRequestTask extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			assert (socket != null && output != null);

			output.println("get-colonies");

			try {
				String line;
				// Wait until a valid line was read
				while ((line = input.readLine()) != null)
					;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
