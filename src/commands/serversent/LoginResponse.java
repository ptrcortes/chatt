package commands.serversent;

import client.Client;
import commands.Command;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class LoginResponse extends Command<Client>
{
	private static final long serialVersionUID = 1316361478843323022L;
	public final boolean accepted;

	public LoginResponse(boolean accepted)
	{
		this.accepted = accepted;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Client recipient)
	{}
}
