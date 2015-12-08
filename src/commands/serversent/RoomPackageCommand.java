package commands.serversent;

import java.util.LinkedList;

import client.Client;
import commands.Command;
import shared.RoomPackage;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class RoomPackageCommand extends Command<Client>
{
	private static final long serialVersionUID = -5950253936244021170L;
	private LinkedList<RoomPackage> rooms; // the message from the server

	/**
	 * Creates a new UpdateClientCommand with the given log of messages
	 * 
	 * @param rooms the log of messages
	 */
	public RoomPackageCommand(LinkedList<RoomPackage> rooms)
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rooms == null) ? 0 : rooms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoomPackageCommand other = (RoomPackageCommand) obj;
		if (rooms == null)
		{
			if (other.rooms != null)
				return false;
		}
		else if (!rooms.equals(other.rooms))
			return false;
		return true;
	}
}
