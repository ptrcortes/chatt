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
import java.util.TreeSet;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattHypervisor
{
	private HashMap<Integer, ChattRoom> rooms = new HashMap<Integer, ChattRoom>();
	public TreeSet<String> currentUsers = new TreeSet<String>();

	private ServerSocket socket;

	private static ChattHypervisor instance;

	/**
	 * Singleton interface for hypervisor. Allows chat rooms to get an instance
	 * of the hypervisor.
	 * 
	 * @return the single hypervisor instance
	 */
	public static ChattHypervisor getInstance()
	{
		if (instance == null)
			instance = new ChattHypervisor();

		return instance;
	}

	/**
	 * This thread listens for and sets up connections to new clients.
	 *
	 * @author Peter Cortes
	 */
	private class ClientAccepter implements Runnable
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

					if (currentUsers.contains(clientName.toLowerCase()))
					{
						output.writeBoolean(false);
						output.flush();
						s.close();
					}
					else
					{
						MetaClient m = new MetaClient(clientName, output, input);

						output.writeBoolean(true);
						output.flush();

						currentUsers.add(clientName.toLowerCase());

						rooms.get(9001).addClient(m);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private ChattHypervisor()
	{
		try
		{
			socket = new ServerSocket(9001);
			new Thread(new ClientAccepter()).start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void initialize()
	{
		ChattRoom t = new ChattRoom();
		rooms.put(9001, t);
	}

	public static void main(String[] args) throws IOException
	{
		ChattHypervisor ch = ChattHypervisor.getInstance();
		ch.initialize();

		/*
		 * try { for (int i = 9001; i < 10000; i += 1) rooms.put(i, new
		 * ChattRoom(i)); } catch (IOException | IllegalArgumentException e) {
		 * System.err.println(e.getMessage()); }
		 * 
		 * System.out.println(rooms);
		 */
	}
}
