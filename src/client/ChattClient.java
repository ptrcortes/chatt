package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.DuplicateNameException;
import shared.Message;

import commands.Command;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattClient extends Application implements Client
{
	// TODO: Gabe and Charles: you can turn this class into a javafx
	// application, or you can use this class as a controller, and define a new
	// set of classes to be the jfx application.

	private String clientName; // user name of the client

	private Socket server; // connection to server
	private ObjectOutputStream out; // output stream
	private ObjectInputStream in; // input stream

	private Login prompt;

	private boolean connected = true;

	/**
	 * LoginListener has code that is executed whenever the login button is
	 * pressed. It checks for valid input, and then checks if the connection was
	 * accepted by the server.
	 * 
	 * Some of this stuff isn't valid anymore because the NRCClient was written
	 * using Swing instead of JavaFX.
	 * 
	 * @author Peter Cortes
	 */
	private class LoginListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// if the data is valid, try to connect
			if (prompt.verifyFields() == true)
			{
				clientName = prompt.getName();

				try
				{
					server.close();
				}
				catch (NullPointerException | IOException e1)
				{
					System.out.println(e1.getMessage());
				}

				try
				{
					server = new Socket();
					// connection outside of constructor to include timeout
					server.connect(new InetSocketAddress(prompt.getAddress(), Integer.parseInt(prompt.getPort())), 500);
					out = new ObjectOutputStream(server.getOutputStream());
					in = new ObjectInputStream(server.getInputStream());

					// setupGUI(clientName, prompt.getAddress(),
					// prompt.getPort());

					// write out the name of this client
					out.writeObject(clientName);

					// if the connection was accepted
					if (in.readBoolean() == true)
					{
						// this listener sends a disconnect command when closing
						// ChattClient.this.addWindowListener(new
						// WindowAdapter()
						// {
						// public void windowClosing(WindowEvent arg0)
						// {
						// try
						// {
						// out.writeObject(new DisconnectCommand(clientName));
						// out.close();
						// in.close();
						// }
						// catch (IOException ioe)
						// {
						// System.out.println(ioe.getMessage());
						// }
						// }
						// });

						// login accepted
						final Timer show = new Timer(400, event -> {
							prompt.setVisible(false);
							// ChattClient.this.setVisible(true);
							});

						show.setRepeats(false);
						show.start();

						// start a thread for handling server events
						new Thread(new ServerHandler()).start();
					}

					else
					{
						server.close();
						throw new DuplicateNameException();
					}
				}
				catch (DuplicateNameException x)
				{
					prompt.setWarning("username taken");
				}
				catch (NumberFormatException x)
				{
					prompt.setWarning("invalid number");
				}
				catch (IOException x)
				{
					prompt.setWarning(x.getMessage());
				}
			}
		}
	}

	/**
	 * This class reads and executes commands sent from the server
	 * 
	 * @author Peter Cortes
	 * @author Gabriel Kishi
	 */
	private class ServerHandler implements Runnable
	{
		@SuppressWarnings("unchecked")
		public void run()
		{
			try
			{
				while (connected)
				{
					// read a command from server and execute it
					Command<Client> c = (Command<Client>) in.readObject();
					c.runOn(ChattClient.this);
				}
			}
			catch (SocketException | EOFException e)
			{
				return; // "gracefully" terminate after disconnect
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	public ChattClient()
	{
		prompt = new Login();
		prompt.addLoginListener(new LoginListener());
		prompt.setVisible(true);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage arg0) throws Exception
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see client.Client#update(shared.Message)
	 */
	@Override
	public void update(Message message)
	{
		// TODO Auto-generated method stub
		System.out.println(message);
	}

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			System.err.println("There was a problem setting the look and feel");
			e.printStackTrace();
		}

		new ChattClient();
	}
}
