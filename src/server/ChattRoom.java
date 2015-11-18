/**
 * 
 */
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
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
	private ChattHypervisor service;
	public String roomName = "Default";

	/**
	 * A map relating usernames to output streams
	 */
	private Map<MetaClient, ObjectOutputStream> outputs;

	/**
	 * This thread reads and executes commands sent by a client
	 *
	 * @author Peter Cortes
	 */
	private class SingleClientThread implements Runnable
	{
		private final MetaClient user;

		/**
		 * This constructor permanently associates this thread with a single
		 * client.
		 * 
		 * @param m the user this thread talks to
		 */
		public SingleClientThread(MetaClient m)
		{
			this.user = m;
		}

		public void run()
		{
			try
			{
				while (true)
				{
					// read a command from the client, execute on this server
					@SuppressWarnings("unchecked")
					Command<Server> command = (Command<Server>) user.instream.readObject();
					command.runOn(ChattRoom.this);

					// terminate if client is disconnecting
					if (command instanceof DisconnectCommand)
					{
						user.instream.close();
						return;
					}
				}
			}
			catch (StreamCorruptedException e)
			{
				e.printStackTrace();
				removeUser();
				System.err.println(ChattRoom.this + " connection to " + user + " corrupted (" + e.getMessage() + ")");
			}
			catch (EOFException | SocketException e)
			{
				e.printStackTrace();
				removeUser();
				System.err.println(ChattRoom.this + " connection to " + user + " lost");
			}
			catch (Exception e)
			{
				System.err.println(ChattRoom.this + " an unexpected error occured");
				e.printStackTrace();
			}
		}

		/**
		 * This method removes a name from the output map and notifies its
		 * observers that it's been removed.
		 */
		private void removeUser()
		{
			outputs.remove(user);
			service.currentUsers.remove(user);
		}
	}

	public ChattRoom()
	{
		outputs = new TreeMap<MetaClient, ObjectOutputStream>();
		service = ChattHypervisor.getInstance();
	}

	public void addClient(MetaClient m)
	{
		outputs.put(m, m.outstream);
		new Thread(new SingleClientThread(m)).start();
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
	 * Disconnects a given user from the server gracefully.
	 * 
	 * @param clientName user to disconnect
	 */
	public void disconnect(String clientName)
	{
		try
		{
			MetaClient m = new MetaClient(clientName);
			outputs.get(m).flush();
			outputs.remove(m).close(); // remove from map
			service.currentUsers.remove(m);

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
		// TODO: remove port magic number
		return String.format("CR%04dU%02d", 9001, outputs.size());
	}
}
