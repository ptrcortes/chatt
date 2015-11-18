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
public class MetaClient implements Comparable<MetaClient>
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

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaClient other = (MetaClient) obj;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equalsIgnoreCase(other.username))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MetaClient o)
	{
		return this.username.toLowerCase().compareTo(o.username.toLowerCase());
	}
}
