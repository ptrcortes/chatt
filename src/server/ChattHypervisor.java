/**
 * 
 */
package server;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattHypervisor
{
	private static HashMap<Integer, ChattRoom> rooms = new HashMap<Integer, ChattRoom>();

	public static void main(String[] args)
	{
		try
		{
			for (int i = 9001; i < 520000; i+=1000)
				rooms.put(i, new ChattRoom(i));
		}
		catch (IOException | IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
		}

		System.out.println(rooms);
	}
}
