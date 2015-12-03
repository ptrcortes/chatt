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
}
