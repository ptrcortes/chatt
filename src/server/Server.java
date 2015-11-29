/**
 * 
 */
package server;

import shared.Message;

/**
 *
 *
 * @author Peter Cortes
 */
public interface Server
{
	public void sendMessageToClients(Message message);
	public void disconnect(String clientName);
	public void createAndSwitch(String username, String roomname);
}
