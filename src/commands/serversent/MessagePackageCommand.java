package commands.serversent;

import shared.Message;
import client.Client;
import commands.Command;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class MessagePackageCommand extends Command<Client>
{
	private static final long serialVersionUID = 7447412470637397130L;
	private Message message; // the message from the server

	/**
	 * Creates a new UpdateClientCommand with the given log of messages
	 * 
	 * @param message the log of messages
	 */
	public MessagePackageCommand(Message message)
	{
		this.message = message;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Client recipient)
	{
		// update the client
		recipient.updateMessageList(message);
	}
}
