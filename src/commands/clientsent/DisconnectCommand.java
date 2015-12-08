package commands.clientsent;

import commands.Command;
import server.Server;

/**
 * This command is sent by a client that is disconnecting.
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class DisconnectCommand extends Command<Server>
{
	private static final long serialVersionUID = -3810515760788481346L;
	private String clientName; // client who is disconnecting

	/**
	 * Creates a disconnect command for the given client
	 * 
	 * @param name username of client to disconnect
	 */
	public DisconnectCommand(String name)
	{
		clientName = name;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		// disconnect client
		recipient.disconnect(clientName);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientName == null) ? 0 : clientName.hashCode());
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
		DisconnectCommand other = (DisconnectCommand) obj;
		if (clientName == null)
		{
			if (other.clientName != null)
				return false;
		}
		else if (!clientName.equals(other.clientName))
			return false;
		return true;
	}
}
