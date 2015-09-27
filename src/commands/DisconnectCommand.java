package commands;

import server.Server;

/**
 * This command is sent by a client that is disconnecting.
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class DisconnectCommand extends Command<Server>
{
	private static final long serialVersionUID = -8557424886231888586L;
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
}
