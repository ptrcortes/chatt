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

	/**
	 * @param sender
	 * @param message
	 */
	public Message(String sender, String message)
	{
		this.sender = sender;
		this.message = message;
		time = LocalTime.now().toString().substring(0, 8);
	}

	@Override
	public String toString()
	{
		return "[" + time + "] " + sender + ": " + message;
	}
}
