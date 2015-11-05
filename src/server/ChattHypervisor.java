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
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattHypervisor implements Observer
{
	private HashMap<Integer, ChattRoom> rooms = new HashMap<Integer, ChattRoom>();
	private TreeSet<String> currentUsers = new TreeSet<String>();

	private ServerSocket socket;

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

						currentUsers.add(clientName);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg)
	{
		currentUsers.remove((String) arg);
	}

	public ChattHypervisor() throws IOException
	{
		socket = new ServerSocket(9001);
		new Thread(new ClientAccepter()).start();
		ChattRoom t = new ChattRoom();
		t.addObserver(this);
		rooms.put(9001, t);
	}

	public static void main(String[] args) throws IOException
	{
		new ChattHypervisor();

		/*
		 * try { for (int i = 9001; i < 10000; i += 1) rooms.put(i, new
		 * ChattRoom(i)); } catch (IOException | IllegalArgumentException e) {
		 * System.err.println(e.getMessage()); }
		 * 
		 * System.out.println(rooms);
		 */
	}
}
