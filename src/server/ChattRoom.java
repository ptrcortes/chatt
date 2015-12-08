/**
 * 
 */
package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Timer;

import commands.Command;
import commands.clientsent.CreateRoomCommand;
import commands.clientsent.DisconnectCommand;
import commands.serversent.MessagePackageCommand;
import commands.serversent.RoomNamePackage;
import commands.serversent.RoomPackageCommand;
import shared.Message;
import shared.RoomPackage;

/**
 * This object is the server that does the communicating with the clients. It
 * distributes messages and handles events such as user connection and
 * disconnection.
 *
 * @author Peter Cortes
 */
public class ChattRoom implements Server
{
	private static int roomsCreated = 0;

	public static ChattRoom createNewRoom(String name)
	{
		return new ChattRoom(++roomsCreated, name);
	}

	private ChattHypervisor service;
	public String roomName = "Default";
	public final int roomID;

	/**
	 * A map relating usernames to output streams
	 */
	private Set<MetaClient> clients;

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
				while (clients.contains(user))
				{
					// read a command from the client, execute on this server
					@SuppressWarnings("unchecked")
					Command<Server> command = (Command<Server>) user.instream.readObject();
					command.runOn(ChattRoom.this);

					if (command instanceof CreateRoomCommand)
					{
						clients.remove(user);
						return;
					}

					// terminate if client is disconnecting
					if (command instanceof DisconnectCommand)
					{
						user.instream.close();
						return;
					}
				}

				return;
			}
			catch (StreamCorruptedException e)
			{
				removeUser();
				System.err.println(ChattRoom.this + " connection to " + user + " corrupted (" + e.getMessage() + ")");
			}
			catch (EOFException | SocketException e)
			{
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
			clients.remove(user);
			service.currentUsers.remove(user);
		}
	}

	private ChattRoom(int identifier, String desiredName)
	{
		roomID = identifier;

		if (desiredName != null)
			roomName = desiredName;

		clients = new TreeSet<MetaClient>();
		service = ChattHypervisor.getInstance();

		Timer t = new Timer(10000, ae -> sendRoomsToClients());
		t.setRepeats(true);
		t.start();
	}

	public void addClient(MetaClient m)
	{
		clients.add(m);
		new Thread(new SingleClientThread(m)).start();
		System.out.println(ChattRoom.this + " added client \"" + m.username + "\"");

		sendMessageToClients(new Message(m.username + " connected to " + roomName));
	}

	/**
	 * Called periodically to send all clients a list of available rooms. This
	 * method gets the room list from the hypervisor and makes a RoomPackage
	 * from each. It then sends a command with this list to all the clients.
	 */
	private void sendRoomsToClients()
	{
		if (clients.size() == 0)
			return;

		// System.out.println(this + " sending rooms to clients");
		LinkedList<RoomPackage> out = new LinkedList<RoomPackage>();
		for (ChattRoom r: service.rooms.values())
			out.addLast(new RoomPackage(r.clients.size() + ": " + r.roomName, r.roomID));

		RoomPackageCommand c = new RoomPackageCommand(out);

		try
		{
			for (MetaClient m: clients)
				m.outstream.writeObject(c);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			MessagePackageCommand out = new MessagePackageCommand(message);
			for (MetaClient m: clients)
				m.outstream.writeObject(out);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MetaClient getUser(String name) throws NoSuchElementException
	{
		for (Iterator<MetaClient> it = clients.iterator(); it.hasNext();)
		{
			MetaClient test = it.next();
			if (test.username.equalsIgnoreCase(name))
				return test;
		}

		throw new NoSuchElementException();
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
			MetaClient m = getUser(clientName);
			m.outstream.flush();
			m.outstream.close();
			clients.remove(m); // remove from set
			service.currentUsers.remove(m);

			System.out.println(this + " disconnected \"" + clientName + "\"");
			sendMessageToClients(new Message(clientName + " disconnected"));
		}
		catch (NoSuchElementException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see server.Server#createAndSwitch(java.lang.String, java.lang.String)
	 */
	@Override
	public void createAndSwitch(String username, String roomname)
	{
		service.createAndSwitch(getUser(username), roomname);
		clients.remove(new MetaClient(username));
		sendMessageToClients(new Message(username + " has left the room"));
	}

	@Override
	public void switchRoom(String username, int roomID)
	{
		if (roomID == this.roomID)
		{
			System.out.println(this + " " + username + " tried to switch into the same room");
		}
		else if (service.switchClientToRoom(getUser(username), roomID))
		{
			clients.remove(new MetaClient(username));
			System.out.println(this + " switching " + username + " to room " + roomID);
			sendMessageToClients(new Message(username + " has left the room"));
		}
		else
		{
			System.out.println(this + " " + username + " couldn't be switched to room " + roomID);
		}
	}

	@Override
	public void getRoomName(String username)
	{
		MetaClient m = getUser(username);
		try
		{
			m.outstream.writeObject(new RoomNamePackage(roomName));
		}
		catch (IOException e)
		{
			System.err.println(this + " couldn't send room name to " + username);
		}
	};

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("CR%04dU%02d", roomID, clients.size());
	}
}
