/**
 * This thread is passed a socket that it reads from. Whenever it gets input
 * it writes it to the ChatScreen text area using the displayMessage() method.
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.*;

public class ReaderThread implements Runnable
{
	Socket server;
	BufferedReader fromServer;
	ChatScreen screen;
	Vector<String> userList;

	public ReaderThread(Socket server, ChatScreen screen) {
		this.server = server;
		this.screen = screen;
	}

	public void run() {
		try {
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));

			while (true) {
				String message = fromServer.readLine();
				if (message.contains("<") || message.contains(">")) {
					if (message.substring(message.indexOf("<") + 1, message.indexOf(",")).equals("server")) {
						String temp = message.substring(message.indexOf(",") + 1);
						if (message.contains("joined")) {
							temp = temp.substring(temp.indexOf(",") + 1, temp.indexOf("joined") - 1);
							userList.add(temp);
						} else {
							temp = temp.substring(temp.indexOf(",") + 1, temp.indexOf("left") - 1);
							userList.remove(temp);
						}
						screen.setUserList(userList);
					}
					switch(message.substring(0, message.indexOf("<"))){
						case "broadcast" -> {
							String sender = message.substring(message.indexOf("<") + 1, message.indexOf(","));
							String time = message.substring(message.indexOf(",") + 1, message.lastIndexOf(","));
							String msg = message.substring(message.lastIndexOf(",") + 1, message.indexOf(">"));
							screen.displayMessage(sender + " (" + time + "): " + msg);
						}
						case "private" -> {
							String sender = message.substring(message.indexOf("<") + 1, message.indexOf(","));
							String time = message.substring(message.indexOf(",") + 1, message.lastIndexOf(","));
							String msg = message.substring(message.lastIndexOf(",") + 1, message.indexOf(">"));
							time = time.substring(time.indexOf(",")+1);
							String recipient = message.substring(message.indexOf(",") + 1, message.indexOf(",", message.indexOf(",") + 1));
							screen.displayMessage(sender + " (" + time + ") to " + recipient + ": " + msg);
						}
						case "userlist" -> {
							String list = message.substring(message.indexOf("<")+1, message.indexOf(">"));
							userList = new Vector<>(Arrays.asList(list.split(",")));
							screen.setUserList(userList);
						}
						default -> screen.displayMessage(message);
					}
				} else {
					switch (message) {
						case "1" -> screen.displayMessage("Username already taken. Please try again.");
						case "2" -> screen.displayMessage("Username contains invalid characters. Please try again.");
						case "3" -> screen.displayMessage("Username is too long. Please try again.");
						case "4" -> screen.displayMessage("Welcome to Zac and Landon's Chatroom!");
						case "5" -> screen.displayMessage("Your message was too long. Please try again.");
						case "6" -> screen.displayMessage("Reserved Character used. Please try again.");
						case "9" -> screen.displayMessage("Recipient is not online. Please try again.");
					}
				}
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }
		finally {
			if (fromServer != null) {
				try {
					fromServer.close();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}
}