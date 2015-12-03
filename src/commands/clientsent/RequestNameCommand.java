package commands.clientsent;

import commands.Command;
import server.Server;

/**
 * Adds a text message to the server's chat log
 * 
 * @author Peter Cortes
 */
public class RequestNameCommand extends Command<Server>
{
	private static final long serialVersionUID = -3195547640038906777L;
	public final String username;

	public RequestNameCommand(String username)
	{
		this.username = username;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		// add message to server's chat log
		recipient.getRoomName(username);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
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
		RequestNameCommand other = (RequestNameCommand) obj;
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
