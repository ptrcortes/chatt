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
public class ChattRoom implements Server
{
	private ServerSocket socket; // the server socket

	/**
	 * A Map relating usernames to output streams
	 */
	private Map<String, ObjectOutputStream> outputs;

	/**
	 * This thread reads and executes commands sent by a client
	 *
	 * @author Peter Cortes
	 */
	private class SingleClientThread implements Runnable
	{
		private ObjectInputStream input; // the input stream from the client
		private String name;

		public SingleClientThread(ObjectInputStream input, String client)
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
					Command<Server> command = (Command<Server>) input.readObject();
					command.runOn(ChattRoom.this);

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
				outputs.remove(name);
				System.out.println(ChattRoom.this + " connection to " + name + " lost");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This thread listens for and sets up connections to new clients.
	 *
	 * @author Peter Cortes
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
						new Thread(new SingleClientThread(input, clientName)).start();

						// add a notification message to the chat log
						System.out.println(ChattRoom.this + " added client \"" + clientName + "\"");

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

	public ChattRoom(int port) throws IOException
	{
		outputs = new TreeMap<String, ObjectOutputStream>();

		socket = new ServerSocket(port);

		// spawn a client accepter thread
		new Thread(new ClientAccepter()).start();

		System.out.println(this + " initialized");
	}

	/**
	 * Writes an UpdateClientCommand to every connected user.
	 * 
	 * @param message the message to write to the clients
	 */
	public void sendMessageToClients(Message message)
	{
		try
		{
			for (ObjectOutputStream out: outputs.values())
				out.writeObject(new UpdateClientCommand(message));
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
		try
		{
			outputs.remove(clientName).close(); // remove from map
			System.out.println(this + " disconnected \"" + clientName + "\"");
			sendMessageToClients(new Message(clientName, "disconnected"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("CR%04dU%02d", socket.getLocalPort(), outputs.size());
	}

	public static void main(String[] args)
	{
		try
		{
			new ChattRoom(9001);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
