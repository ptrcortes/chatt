package commands.clientsent;

import commands.Command;
import server.Server;

/**
 * Adds a text message to the server's chat log
 * 
 * @author Peter Cortes
 */
public class SwitchRoomCommand extends Command<Server>
{
	private static final long serialVersionUID = 8925701358525101336L;
	private final String username;
	private final int roomID;

	/**
	 * @param username
	 * @param roomID
	 */
	public SwitchRoomCommand(String username, int roomID)
	{
		this.username = username;
		this.roomID = roomID;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		recipient.switchRoom(username, roomID);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + roomID;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		SwitchRoomCommand other = (SwitchRoomCommand) obj;
		if (roomID != other.roomID)
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		return true;
	}
}
