package org.samcrow.data;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;

import org.samcrow.data.AsyncLineInputStream.LineReadHandler;

/**
 * Stores data associated with the server and manages synchronization
 * 
 * @author Sam Crow
 */
public class Server implements LineReadHandler {

	/** The current known colonies */
	protected HashSet<Colony> colonies = new HashSet<Colony>();

	/** The socket connection to the server */
	protected Socket socket;

	/** The stream used to get data from the server */
	protected AsyncLineInputStream input;

	/** The stream used to send data to the server */
	protected OutputStream output;

	protected ServerConnectionTask thread;

	/**
	 * Constructor
	 * 
	 * @param ipAddress
	 *            The IP address to connect to
	 * @param port
	 *            The port to connect over
	 */
	public Server(String ipAddress, int port) {
		thread = new ServerConnectionTask(ipAddress, port);
		thread.start();
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
			socket = new Socket();

			try {
				InetSocketAddress portAddress = new InetSocketAddress(port);
				socket.bind(portAddress);
			} catch (IOException e) {
				System.err.println("Could not bind to local port " + port);
				e.printStackTrace();
			}

			try {
				System.out.println("Trying to connect...");
				socket.connect(new InetSocketAddress(ipAddress, port));
			} catch (IOException e) {
				System.err.println("Could not connect to " + ipAddress);
				e.printStackTrace();
			}

			if (!socket.isConnected()) {
				System.err.println("Not connected to the server.");
			}

			try {
				input = new AsyncLineInputStream(socket.getInputStream());
				input.setHandler(Server.this);// Input the outer instance of
												// Server.
			} catch (IOException e) {
				System.err
						.println("Exception getting the input stream for the socket");
				e.printStackTrace();
			}
			try {
				output = socket.getOutputStream();
			} catch (IOException e) {
				System.err
						.println("Exception getting the output stream for the socket.");
				e.printStackTrace();
			}
		}

	}

	@Override
	public void lineRead(String line) {
		System.out.println("Read: " + line);

	}
}
