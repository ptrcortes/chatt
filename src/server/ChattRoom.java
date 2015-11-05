/**
 * 
 */
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
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
	private ChattHypervisor service;
	public String roomName = "Default";

	/**
	 * A map relating usernames to output streams
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
			catch (StreamCorruptedException e)
			{
				e.printStackTrace();
				removeUser();
				System.err.println(ChattRoom.this + " connection to " + name + " corrupted (" + e.getMessage() + ")");
			}
			catch (EOFException | SocketException e)
			{
				e.printStackTrace();
				removeUser();
				System.err.println(ChattRoom.this + " connection to " + name + " lost");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		/**
		 * This method removes a name from the output map and notifies its
		 * observers that it's been removed.
		 */
		private void removeUser()
		{
			outputs.remove(name);
			service.currentUsers.remove(name);
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

					if (outputs.containsKey(clientName.toLowerCase()))
					{
						output.writeBoolean(false);
						output.flush();
						s.close();
					}
					else
					{
						output.writeBoolean(true);
						output.flush();
						// map client name to output stream
						outputs.put(clientName.toLowerCase(), output);

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

	public ChattRoom()
	{
		outputs = new TreeMap<String, ObjectOutputStream>();
		service = ChattHypervisor.getInstance();
	}

	public ChattRoom(int port) throws IOException
	{
		outputs = new TreeMap<String, ObjectOutputStream>();

		socket = new ServerSocket(port);
		service = ChattHypervisor.getInstance();

		// spawn a client accepter thread
		new Thread(new ClientAccepter()).start();

		System.out.println(this + " initialized");
	}

	public void addClient(MetaClient m)
	{
		outputs.put(m.username.toLowerCase(), m.outStream);
		new Thread(new SingleClientThread(m.inStream, m.username)).start();
		System.out.println(ChattRoom.this + " added client \"" + m.username + "\"");

		sendMessageToClients(new Message(m.username, "connected"));
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
			outputs.get(clientName.toLowerCase()).flush();
			outputs.remove(clientName.toLowerCase()).close(); // remove from map
			service.currentUsers.remove(clientName);

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
		// TODO: remove hard code of port num
		return String.format("CR%04dU%02d", 9001, outputs.size());
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
