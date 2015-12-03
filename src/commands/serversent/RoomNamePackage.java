package commands.serversent;

import client.Client;
import commands.Command;

/**
 * Updates a client with the current list of chat messages
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public class RoomNamePackage extends Command<Client>
{
	private static final long serialVersionUID = -3363638006742964099L;
	private String roomName;

	public RoomNamePackage(String roomName)
	{
		this.roomName = roomName;
	}

	/**
	 * @see commands.Command#runOn(java.lang.Object)
	 */
	public void runOn(Client recipient)
	{
		// update the client
		recipient.setRoomName(roomName);
	}
}
