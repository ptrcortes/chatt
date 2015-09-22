/**
 * 
 */
package server;

import java.io.IOException;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattHypervisor
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		try
		{
			new ChattRoom(9001);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
}
