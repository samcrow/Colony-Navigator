package org.samcrow.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class continuously reads characters from its stream. When \n is
 * received, it fires a callback and provides the line that was read.
 * 
 * @author Sam Crow
 */
public class AsyncLineInputStream extends DataInputStream {

	/** The thread that reads data */
	protected LineReadTask thread;

	/** The class that will receive notice of a line being read */
	protected LineReadHandler handler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public AsyncLineInputStream(InputStream in) {
		super(in);
		thread = new LineReadTask();
	}

	/**
	 * Set the handler that will be notified when a new line is read
	 * 
	 * @param handler
	 *            the handler to set
	 */
	public void setHandler(LineReadHandler handler) {
		this.handler = handler;
	}

	protected class LineReadTask extends Thread {

		/** The line that is currently being read */
		protected String currentLine = "";

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while (true) {
					if (available() > 0) {
						while (available() > 0) {
							char c = readChar();

							if (c == '\n') {
								// End of line
								if (handler != null) {
									handler.lineRead(currentLine);// Fire
																	// handler
								}
								currentLine = "";
							} else {// Not end of line, just another character
								currentLine += c;
							}
						}
					}
				}
			} catch (IOException e) {
				System.err
						.println("Exception reading data from a stream in an async thread.");
				e.printStackTrace();
			}
		}

	}

	/**
	 * An interface for a class that should be notified when a new line of text
	 * is read from the stream
	 * 
	 * @author Sam Crow
	 */
	public interface LineReadHandler {

		/**
		 * Called when a new line of text is read from the stream
		 * 
		 * @param line
		 *            The line of text that was read, excluding the newline
		 *            character(s)
		 */
		public void lineRead(String line);
	}

}
