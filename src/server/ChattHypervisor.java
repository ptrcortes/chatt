/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattHypervisor
{
	private static HashMap<Integer, ChattRoom> rooms = new HashMap<Integer, ChattRoom>();

	private static ServerSocket socket;

	/**
	 * This thread listens for and sets up connections to new clients.
	 *
	 * @author Peter Cortes
	 */
	private static class ClientAccepter implements Runnable
	{
		public void run()
		{
			try
			{
				while (true)
				{
					// accept a new client, get output & input streams
					Socket s = socket.accept();
					ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(s.getInputStream());

					// read the client's name
					String clientName = (String) input.readObject();

					MetaClient m = new MetaClient(clientName, output, input);

					output.writeBoolean(true);
					output.flush();
					
					//TODO: pass m to a selected ChattRoom
					rooms.get(9001).addClient(m);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException
	{
		socket = new ServerSocket(9001);
/*
		try
		{
			for (int i = 9001; i < 10000; i += 1)
				rooms.put(i, new ChattRoom(i));
		}
		catch (IOException | IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
		}

		System.out.println(rooms);
*/
		rooms.put(9001, new ChattRoom());
		new Thread(new ClientAccepter()).start();
	}
}
