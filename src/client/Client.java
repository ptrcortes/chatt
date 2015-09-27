/**
 * 
 */
package client;

import shared.Message;

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
	public void update(Message message);
}
