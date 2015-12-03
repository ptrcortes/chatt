/**
 * 
 */
package shared;

import java.io.Serializable;
import java.time.LocalTime;

/**
 *
 *
 * @author Peter Cortes
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = -197957676869021174L;

	private final String sender;
	private final String message;
	private final String time;

	public boolean meMessage = false;
	public boolean sysMessage = false;

	/**
	 * Used to create a system message appearing to be sent by the server.
	 * 
	 * @param message the message to display
	 */
	public Message(String message)
	{
		sysMessage = true;
		this.sender = null;
		this.message = message;
		this.time = LocalTime.now().toString().substring(0, 8);
	}

	/**
	 * @param sender the name of the sender of this message
	 * @param message the actual contents of this message
	 */
	public Message(String sender, String message)
	{
		this.sender = sender;
		this.message = message;
		this.time = LocalTime.now().toString().substring(0, 8);
	}

	/**
	 * @param sender the name of the sender of this message
	 * @param message the actual contents of this message
	 */
	public Message(String sender, String message, Boolean me)
	{
		this.sender = sender;
		this.message = message;
		this.time = LocalTime.now().toString().substring(0, 8);

		meMessage = true;
	}

	@Override
	public String toString()
	{
		if (meMessage)
			return "[" + time + "]   *" + sender + " " + message + "*";
		else if (sysMessage)
			return "[" + time + "] system: " + message;
		else
			return "[" + time + "] " + sender + ": " + message;
	}
}
