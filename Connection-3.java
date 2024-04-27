import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Connection implements Runnable
{
	private Socket	client;
	private HashMap<String, BufferedWriter> clients;
	private static Handler handler = new Handler();

	public Connection(Socket client, HashMap<String, BufferedWriter> clients) {
		this.client = client;
		this.clients = clients;
	}

	/**
	 * This method runs in a separate thread.
	 */
	public void run() {
		try {
			handler.process(client, clients);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
}