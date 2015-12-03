package commands.clientsent;

import commands.Command;
import server.Server;
import shared.Message;

/**
 * Adds a text message to the server's chat log
 * 
 * @author Peter Cortes
 */
public class SendMessageCommand extends Command<Server>
{
	private static final long serialVersionUID = 3426610237662854206L;
	private Message message; // message from client

	/**
	 * Creates an AddMessageCommand with the given message
	 * 
	 * @param message message to add to log
	 */
	public SendMessageCommand(Message message)
	{
		this.message = message;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Server recipient)
	{
		// add message to server's chat log
		recipient.sendMessageToClients(message);
	}
}
