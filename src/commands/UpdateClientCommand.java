package commands;

import client.ChattClient;
import shared.Message;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class UpdateClientCommand extends Command<ChattClient>
{
	private static final long serialVersionUID = 4222014184904080846L;
	private Message message; // the message from the server

	/**
	 * Creates a new UpdateClientCommand with the given log of messages
	 * 
	 * @param message the log of messages
	 */
	public UpdateClientCommand(Message message)
	{
		this.message = message;
	}

	/**
	 * @see commands.Command#execute(java.lang.Object)
	 */
	public void execute(ChattClient recipient)
	{
		// update the client
		recipient.update(message);
	}
}
