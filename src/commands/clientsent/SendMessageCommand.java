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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		SendMessageCommand other = (SendMessageCommand) obj;
		if (message == null)
		{
			if (other.message != null)
				return false;
		}
		else if (!message.equals(other.message))
			return false;
		return true;
	}
}
