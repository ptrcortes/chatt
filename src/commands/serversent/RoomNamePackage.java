package commands.serversent;

import client.Client;
import commands.Command;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class RoomNamePackage extends Command<Client>
{
	private static final long serialVersionUID = -3363638006742964099L;
	private String roomName;

	public RoomNamePackage(String roomName)
	{
		this.roomName = roomName;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Client recipient)
	{
		// update the client
		recipient.setRoomName(roomName);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomName == null) ? 0 : roomName.hashCode());
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
		RoomNamePackage other = (RoomNamePackage) obj;
		if (roomName == null)
		{
			if (other.roomName != null)
				return false;
		}
		else if (!roomName.equals(other.roomName))
			return false;
		return true;
	}
}
