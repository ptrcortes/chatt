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

	@Override
	public String toString()
	{
		return id + ": " + name;
	}
}
