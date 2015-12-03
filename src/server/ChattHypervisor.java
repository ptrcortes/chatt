/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
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
	public HashMap<Integer, ChattRoom> rooms = new HashMap<Integer, ChattRoom>();
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
						output.reset();

						addUser(candidateUser);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private void addUser(MetaClient candidateUser)
		{
			// TODO: logic for assignment to a room goes here

			currentUsers.add(candidateUser);

			rooms.get(1).addClient(candidateUser);
		}
	}

	private ChattHypervisor()
	{
		try
		{
			socket = new ServerSocket(9001);
			new Thread(new ClientAccepter()).start();
		}
		catch (BindException e)
		{
			System.err.println("server already running");
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void initialize()
	{
		ChattRoom t = ChattRoom.createNewRoom("apple room");
		rooms.put(t.roomID, t);
		t = ChattRoom.createNewRoom("berry room");
		rooms.put(t.roomID, t);
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
		System.out.println("chatt hypervisor initialized\n" + ch);

		/*
		 * try { for (int i = 9001; i < 10000; i += 1) rooms.put(i, new
		 * ChattRoom(i)); } catch (IOException | IllegalArgumentException e) {
		 * System.err.println(e.getMessage()); }
		 * 
		 * System.out.println(rooms);
		 */
	}

	/**
	 * @param user
	 * @param roomname
	 */
	public void createAndSwitch(MetaClient user, String roomname)
	{
		ChattRoom t = ChattRoom.createNewRoom(roomname);
		rooms.put(t.roomID, t);
		t.addClient(user);
	}

	/**
	 * @param user
	 * @param roomID
	 */
	public boolean switchClientToRoom(MetaClient user, int roomID)
	{
		if (rooms.containsKey(roomID))
		{
			rooms.get(roomID).addClient(user);
			return true;
		}

		return false;
	}
}
