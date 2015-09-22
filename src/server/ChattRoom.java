/**
 * 
 */
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.TreeMap;

import commands.Command;
import commands.DisconnectCommand;
import commands.UpdateClientCommand;

import shared.Message;

/**
 * This object is the server that does the communicating with the clients. It
 * distributes messages and handles events such as user connection and
 * disconnection.
 *
 * @author Peter Cortes
 */
public class ChattRoom
{
	private ServerSocket socket; // the server socket

	/**
	 * A Map relating usernames to output streams
	 */
	private Map<String, ObjectOutputStream> outputs;

	/**
	 * This thread reads and executes commands sent by a client
	 */
	private class ClientHandler implements Runnable
	{
		private ObjectInputStream input; // the input stream from the client
		private String name;

		public ClientHandler(ObjectInputStream input, String client)
		{
			this.input = input;
			this.name = client;
		}

		public void run()
		{
			try
			{
				while (true)
				{
					// read a command from the client, execute on this server
					@SuppressWarnings("unchecked")
					Command<ChattRoom> command = (Command<ChattRoom>) input.readObject();
					command.execute(ChattRoom.this);

					// terminate if client is disconnecting
					if (command instanceof DisconnectCommand)
					{
						input.close();
						return;
					}
				}
			}
			catch (EOFException | SocketException e)
			{
				System.out.println("connection to " + name + " lost");
				outputs.remove(name);
				System.out.println(outputs.size() + " clients connected");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This thread listens for and sets up connections to new clients
	 */
	private class ClientAccepter implements Runnable
	{
		public void run()
		{
			try
			{
				while (true)
				{
					// accept a new client, get output & input streams
					Socket s = socket.accept();
					ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(s.getInputStream());

					// read the client's name
					String clientName = (String) input.readObject();
					if (outputs.containsKey(clientName))
					{
						output.writeBoolean(false);
						output.flush();
						s.close();
					}
					else
					{
						output.writeBoolean(true);
						// map client name to output stream
						outputs.put(clientName, output);

						// new thread to communicate with client
						new Thread(new ClientHandler(input, clientName)).start();

						// add a notification message to the chat log
						if (outputs.size() != 1)
							System.out.println("adding client \"" + clientName + "\": " + outputs.size() + " clients connected");
						else
							System.out.println("adding client \"" + clientName + "\": " + outputs.size() + " client connected");

						sendMessageToClients(new Message(clientName, "connected"));
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public ChattRoom() throws IOException
	{
		outputs = new TreeMap<String, ObjectOutputStream>();

		// start a new server on port 9001
		socket = new ServerSocket(9001);
		System.out.println("ChattRoom started on port 9001");

		// spawn a client accepter thread
		new Thread(new ClientAccepter()).start();
	}

	/**
	 * Writes an UpdateClientCommand to every connected user.
	 */
	public void sendMessageToClients(Message message)
	{
		// make an UpdateClientCommmand, write to all connected users
		UpdateClientCommand update = new UpdateClientCommand(message);

		try
		{
			for (ObjectOutputStream out: outputs.values())
				out.writeObject(update);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Disconnects a given user from the server gracefully
	 * 
	 * @param clientName user to disconnect
	 */
	public void disconnect(String clientName)
	{
		System.out.print("disconnecting " + clientName + ": ");
		try
		{
			outputs.remove(clientName).close(); // remove from map
			if (outputs.size() != 1)
				System.out.println(outputs.size() + " clients connected");
			else
				System.out.println(outputs.size() + " client connected");

			// add notification message
			sendMessageToClients(new Message(clientName, "disconnected"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			new ChattRoom();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
