package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import server.ChattHypervisor;
import shared.DuplicateNameException;
import shared.Message;
import commands.Command;
import commands.SendMessageCommand;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattClient extends Application implements Client
{
	private String clientName; // this client's username
	private Socket serversocket; // connection to server
	private ObjectOutputStream out; // output stream
	private ObjectInputStream in; // input stream

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
	private class LoginAction implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent e)
		{
			System.out.println("handling" + e);
			// if the data is valid, try to connect
			if (LoginFX.verifyFields() == true)
			{
				clientName = LoginFX.getName();

				try
				{
					serversocket.close();
				}
				catch (IOException e1)
				{
					System.err.println(e1.getMessage());
				}
				catch (NullPointerException e1)
				{
					// do nothing if serverConnection doesn't exist
				}

				try
				{
					serversocket = new Socket();
					// connection called separately to include timeout
					serversocket.connect(new InetSocketAddress(LoginFX.getAddress(), Integer.parseInt(LoginFX.getPort())), 500);
					out = new ObjectOutputStream(serversocket.getOutputStream());
					in = new ObjectInputStream(serversocket.getInputStream());

					// setupGUI(clientName, prompt.getAddress(),
					// prompt.getPort());

					// write out the name of this client
					out.writeObject(clientName);
					out.flush();

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
						final Timer show = new Timer(400, event -> LoginFX.close());
						show.setRepeats(false);
						show.start();

						// start a thread for handling server events
						new Thread(new ServerHandler()).start();
						new Thread(new ChatSender()).start();
					}

					else
					{
						serversocket.close();
						throw new DuplicateNameException();
					}
				}
				catch (DuplicateNameException x)
				{
					LoginFX.setDelayedWarning("username taken");
				}
				catch (NumberFormatException x)
				{
					LoginFX.setDelayedWarning("invalid number");
				}
				catch (IOException x)
				{
					LoginFX.setDelayedWarning(x.getMessage());
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
		public void run()
		{
			try
			{
				// read the next command from the server and execute it
				while (connected)
				{
					@SuppressWarnings("unchecked")
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
				System.err.println(e.getMessage());
			}
			finally
			{
				try
				{
					serversocket.close();
				}
				catch (IOException e)
				{
					// do nothing
				}
			}
		}
	}

	private class ChatSender implements Runnable
	{
		Scanner s;

		public void run()
		{
			s = new Scanner(System.in);

			while (true)
			{
				try
				{
					out.writeObject(new SendMessageCommand(new Message(clientName, s.nextLine())));
				}
				catch (IOException e)
				{
					System.err.println("chatsender: " + e.getMessage());
				}
			}
		}
	}

	public ChattClient()
	{
		LoginFX.main(null);
		// prompt = new LoginFX();
		EventHandler<ActionEvent> meow = new LoginAction();
		LoginFX.addLoginHandler(meow);

		// prompt.addLoginListener(new LoginAction());
		// prompt.login.setOnAction(e -> new LoginAction().handle(e));
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */

	@Override
	public void start(Stage mainStage) throws Exception
	{
		Group root = new Group();
		Scene scene = new Scene(root, 350, 600);

		Button hypeVisor = new Button("Start Rooms");
		hypeVisor.setOnAction(even -> ChattHypervisor.main(null));

		root.getChildren().add(hypeVisor);

		mainStage.setTitle("Start Chatting");
		mainStage.setScene(scene);
		mainStage.centerOnScreen();
		mainStage.show();

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

	@Override
	public String toString()
	{
		return String.format("CC%04dU%s", serversocket.getLocalPort(), clientName);
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
		// Application.launch();
	}
}
