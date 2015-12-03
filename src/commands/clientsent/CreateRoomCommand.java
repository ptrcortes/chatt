package commands.clientsent;

import commands.Command;
import server.Server;

/**
 * Adds a text message to the server's chat log
 * 
 * @author Peter Cortes
 */
public class CreateRoomCommand extends Command<Server>
{
	private static final long serialVersionUID = -8737119861157541470L;
	private final String username;
	private final String roomname;
	
	/**
	 * Creates a new create room command with the desired room name
	 * 
	 * @param username
	 * @param roomname
	 */
	public CreateRoomCommand(String username, String roomname)
	{
		this.username = username;
		this.roomname = roomname;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		// add message to server's chat log
		recipient.createAndSwitch(username, roomname);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomname == null) ? 0 : roomname.hashCode());
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
		CreateRoomCommand other = (CreateRoomCommand) obj;
		if (roomname == null)
		{
			if (other.roomname != null)
				return false;
		}
		else if (!roomname.equals(other.roomname))
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
