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
	private static final long serialVersionUID = 8363345283854159167L;
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
}
