/**
 * An annoying server listening on port 6008.
 *
 * @author - Greg Gagne.
 */

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class  AnnoyingServer
{
    public static final int DEFAULT_PORT = 6008;

    // construct a thread pool for concurrency
    private static final Executor exec = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;
        HashMap<String, BufferedWriter> clients = new HashMap<>();

        try {
            // establish the socket
            sock = new ServerSocket(DEFAULT_PORT);

            while (true) {
                /**
                 * now listen for connections
                 * and service the connection in a separate thread.
                 */
                Runnable task = new Connection(sock.accept(), clients);
                exec.execute(task);
            }
        }
        catch (IOException ioe) { System.err.println(ioe); }
        finally {
            if (sock != null)
                sock.close();
        }
    }
}


