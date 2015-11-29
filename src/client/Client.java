/**
 * 
 */
package client;

import java.util.LinkedList;

import shared.Message;
import shared.RoomPackage;

/**
 *
 *
 * @author Peter Cortes
 */
public interface Client
{
	/**
	 * This method is called by update commands, it's up to the client to decide
	 * what to do with the new Message.
	 * 
	 * @param message The new Message object that the client is supposed to
	 *            handle.
	 */
	public void updateMessageList(Message message);

	/**
	 * This method is called by a server command when the server pushes new room
	 * data to clients.
	 * 
	 * @param rooms the list of rooms
	 */
	public void updateRoomList(LinkedList<RoomPackage> rooms);
}
