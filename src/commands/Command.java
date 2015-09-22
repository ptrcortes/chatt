package commands;

import java.io.Serializable;

/**
 * This abstract class defines a serializable command that can be sent and
 * executed on either a client or server.
 * 
 * @author Peter Cortes
 * @author Gabriel Kishi
 */
public abstract class Command<T> implements Serializable
{
	private static final long serialVersionUID = -4838155228547508978L;

	/**
	 * Executes the command on the given argument
	 * 
	 * @param recipient Object to execute the command on
	 */
	public abstract void runOn(T recipient);
}
