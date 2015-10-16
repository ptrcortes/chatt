/**
 * 
 */
package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 *
 * @author Peter Cortes
 */
public class MetaClient
{
	public final String username;
	public final ObjectOutputStream outStream;
	public final ObjectInputStream inStream;

	/**
	 * Initializes all the final fields of this class
	 * 
	 * @param username The client's username
	 * @param outStream The client's sending stream
	 * @param inStream The client's receiving stream
	 */
	public MetaClient(String username, ObjectOutputStream outStream, ObjectInputStream inStream)
	{
		this.username = username;
		this.outStream = outStream;
		this.inStream = inStream;
	}
}
