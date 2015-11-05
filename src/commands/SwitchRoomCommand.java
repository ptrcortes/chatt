package commands;

import server.Server;

/**
 * Adds a text message to the server's chat log
 * 
 * @author Peter Cortes
 */
public class SwitchRoomCommand extends Command<Server>
{
	private static final long serialVersionUID = 8394654307009158284L;

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		//TODO: add room switch functionality
	}
}
