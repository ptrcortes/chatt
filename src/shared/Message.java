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
	private static final long serialVersionUID = -113666302780197568L;

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
			return "[" + time + "] " + message;
		else
			return "[" + time + "] " + sender + ": " + message;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (meMessage ? 1231 : 1237);
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + (sysMessage ? 1231 : 1237);
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		Message other = (Message) obj;
		if (meMessage != other.meMessage)
			return false;
		if (message == null)
		{
			if (other.message != null)
				return false;
		}
		else if (!message.equals(other.message))
			return false;
		if (sender == null)
		{
			if (other.sender != null)
				return false;
		}
		else if (!sender.equals(other.sender))
			return false;
		if (sysMessage != other.sysMessage)
			return false;
		if (time == null)
		{
			if (other.time != null)
				return false;
		}
		else if (!time.equals(other.time))
			return false;
		return true;
	}
}
