package commands;

import java.util.LinkedList;

import client.Client;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class RoomPackageCommand extends Command<Client>
{
	private static final long serialVersionUID = 4222014184904080846L;
	private LinkedList<String> rooms; // the message from the server

	/**
	 * Creates a new UpdateClientCommand with the given log of messages
	 * 
	 * @param rooms the log of messages
	 */
	public RoomPackageCommand(LinkedList<String> rooms)
	{
		this.rooms = rooms;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Client recipient)
	{
		// update the client
		recipient.updateRoomList(rooms);
	}
}
