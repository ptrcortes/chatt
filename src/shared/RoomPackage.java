/**
 * 
 */
package shared;

import java.io.Serializable;

/**
 *
 *
 * @author Peter Cortes
 */
public class RoomPackage implements Serializable
{
	private static final long serialVersionUID = 8671563612239901184L;
	public final String name;
	public final int id;

	public RoomPackage(String name, int id)
	{
		this.name = name;
		this.id = id;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		RoomPackage other = (RoomPackage) obj;
		if (id != other.id)
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	public String toLongString()
	{
		return id + ": " + name;
	}
}
