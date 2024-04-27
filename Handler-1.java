import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Handler
{
    private HashMap<String, BufferedWriter> clients;

    /**
     * this method is invoked by a separate thread
     */
    public void process(Socket client, HashMap<String, BufferedWriter> clients) throws java.io.IOException {
        BufferedWriter toClient = null;
        BufferedReader fromClient = null;
        String message = null;
        String username = null;

        try {
            toClient = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

            while (true) {
                message = fromClient.readLine();
                String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                int commaCount = message.length() - message.replace(",", "").length();
                int leftCount = message.length() - message.replace("<", "").length();
                int rightCount = message.length() - message.replace(">", "").length();
                switch (message.substring(0, message.indexOf("<"))) {
                    case "user" -> {
                        username = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
                        if (username.contains("<") || username.contains(">") || username.contains(",")) {
                            toClient.write("2\n");
                        } else if (clients.containsKey(username)) {
                            toClient.write("1\n");
                        } else if (username.length() > 20) {
                            toClient.write("3\n");
                        } else {
                            clients.put(username, toClient);
                            toClient.write("4\n");
                            toClient.write("userlist<");
                            ArrayList<String> keys = new ArrayList<>(clients.keySet());
                            for (String key : keys) {
                                toClient.write(key + ",");
                            }
                            toClient.write(">\n");
                            for (String key : clients.keySet()) {
                                clients.get(key).write("broadcast<server," + currentTime + "," + username + " joined the chatroom.>\n");
                                clients.get(key).flush();
                            }
                        }
                        toClient.flush();
                    }
                    case "broadcast" -> {
                        if(message.length() > 1024) {
                            toClient.write("5\n");
                            toClient.flush();
                        } else if((commaCount != 2) || (leftCount != 1) || (rightCount != 1)) {
                            toClient.write("6\n");
                            toClient.flush();
                        } else {
                            String sender = message.substring(message.indexOf("<") + 1, message.indexOf(","));
                            clients.get(sender).write("8\n");
                            for (String key : clients.keySet()) {
                                clients.get(key).write(message + "\n");
                                clients.get(key).flush();
                            }
                        }
                    }
                    case "private" -> {
                        if(message.length() > 1024) {
                            toClient.write("5\n");
                            toClient.flush();
                        } else if((commaCount != 3) || (leftCount != 1) || (rightCount != 1)) {
                            toClient.write("6\n");
                            toClient.flush();
                        } else {
                            String sender = message.substring(message.indexOf("<") + 1, message.indexOf(","));
                            String recipient = message.substring(message.indexOf(",") + 1, message.indexOf(",", message.indexOf(",") + 1));
                            clients.get(sender).write("7\n");
                            clients.get(sender).write(message + "\n");
                            clients.get(sender).flush();
                            if (clients.containsKey(recipient)) {
                                clients.get(recipient).write(message + "\n");
                                clients.get(recipient).flush();
                            } else {
                                clients.get(sender).write("9\n");
                                clients.get(sender).flush();
                            }
                        }
                    }
                    case "ls" -> {
                        String sender = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
                        ArrayList<String> keys = new ArrayList<>(clients.keySet());
                        clients.get(sender).write("userlist<");
                        for (String key : keys) {
                            clients.get(sender).write(key + ",");
                        }
                        clients.get(sender).write(">\n");
                        clients.get(sender).flush();
                    }
                    case "exit" -> {
                        String sender = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
                        clients.remove(sender).close();
                        for (String key : clients.keySet()) {
                            clients.get(key).write("broadcast<server," + currentTime + "," + sender + " left the chatroom.>\n");
                            clients.get(key).flush();
                        }
                    }
                }
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
        finally {
            // close streams and socket
            if (toClient != null)
                toClient.close();
            if (fromClient != null)
                fromClient.close();
        }
    }
}