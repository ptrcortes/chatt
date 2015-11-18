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
	public TreeSet<MetaClient> currentUsers = new TreeSet<MetaClient>();

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

					MetaClient candidateUser = new MetaClient(clientName, output, input);

					// client already exists
					if (currentUsers.contains(candidateUser))
					{
						output.writeBoolean(false);
						output.flush();
						s.close();
					}
					else // store the client
					{
						output.writeBoolean(true);
						output.flush();

						currentUsers.add(candidateUser);

						rooms.get(9001).addClient(candidateUser);
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

	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append("rooms:\n");
		for (ChattRoom c: rooms.values())
			out.append("  " + c + "\n");

		return out.toString();
	}

	public static void main(String[] args) throws IOException
	{
		ChattHypervisor ch = ChattHypervisor.getInstance();
		ch.initialize();
		System.out.println("chatt hypervisor initialized");
		System.out.println(ch);

		/*
		 * try { for (int i = 9001; i < 10000; i += 1) rooms.put(i, new
		 * ChattRoom(i)); } catch (IOException | IllegalArgumentException e) {
		 * System.err.println(e.getMessage()); }
		 * 
		 * System.out.println(rooms);
		 */
	}
}
