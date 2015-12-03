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
}
