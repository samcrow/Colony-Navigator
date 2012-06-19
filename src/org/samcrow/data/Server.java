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

	public Server(String IPAddress, int port) {

		socket = new Socket();

		try {
			socket.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			System.err.println("Could not bind to local port " + port);
			e.printStackTrace();
		}

		try {
			socket.connect(new InetSocketAddress(IPAddress, port));
		} catch (IOException e) {
			System.err.println("Could not connect to " + IPAddress);
			e.printStackTrace();
		}

		if (!socket.isConnected()) {
			System.err.println("Not connected to the server.");
		}

		try {
			input = new AsyncLineInputStream(socket.getInputStream());
			input.setHandler(this);
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

	@Override
	public void lineRead(String line) {
		System.out.println("Read line: " + line);
	}
}
