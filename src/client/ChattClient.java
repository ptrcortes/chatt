package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import shared.DuplicateNameException;
import shared.Message;
import commands.Command;
import commands.DisconnectCommand;
import commands.SendMessageCommand;

/**
 *
 *
 * @author Peter Cortes
 */
public class ChattClient extends Application implements Client
{
	private String clientName; // this client's username
	private Socket server; // connection to server
	private ObjectOutputStream out; // output stream
	private ObjectInputStream in; // input stream

	private boolean connected = true;
	private LoginStage prompt;

	private Stage chattStage;

	/**
	 * LoginListener has code that is executed whenever the login button is
	 * pressed. It checks for valid input, and then checks if the connection was
	 * accepted by the server.
	 * 
	 * @author Peter Cortes
	 */
	private class LoginAction implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent e)
		{
			// if the data is valid, try to connect
			if (prompt.verifyFields() == true)
			{
				clientName = prompt.getName();

				try
				{
					server.close();
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
					server = new Socket();
					// connection called separately to include timeout
					server.connect(new InetSocketAddress(prompt.getAddress(), Integer.parseInt(prompt.getPort())), 500);
					out = new ObjectOutputStream(server.getOutputStream());
					in = new ObjectInputStream(server.getInputStream());

					// setupGUI(clientName, prompt.getAddress(),
					// prompt.getPort());

					// write out the name of this client
					out.writeObject(clientName);
					out.flush();

					// if the connection was accepted
					if (in.readBoolean() == true)
					{
						connected = true;

						// login accepted
						Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), ae -> {
							prompt.hide();
							chattStage.show();
						}));
						timeline.play();

						// start a thread for handling server events
						new Thread(new ServerHandler()).start();
						new Thread(new ChatSender()).start();
					}

					else
					{
						server.close();
						throw new DuplicateNameException();
					}
				}
				catch (DuplicateNameException x)
				{
					prompt.setDelayedWarning("username taken");
				}
				catch (IOException x)
				{
					prompt.setDelayedWarning(x.getMessage());
				}
			}
		}
	}

	/**
	 * SignoutListener closes the connection to the server when the logout
	 * button is pressed.
	 * 
	 * @author Peter Cortes
	 * @author Garrett MacDuffee
	 */
	private class SignoutHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent e)
		{
			try
			{
				connected = false;
				out.writeObject(new DisconnectCommand(clientName));
				out.flush();
				out.close();
				in.close();
			}
			catch (IOException e1)
			{
				System.err.println(e1.getMessage());
			}

			// reset forms, clear login, and show login prompt
			prompt.clear();

			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), ae -> {
				chattStage.hide();
				prompt.show();
			}));
			timeline.play();
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
				// System.out.println("returning from ServerHandler");
				return; // "gracefully" terminate after disconnect
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * This class should be replaced with interaction through a gui.
	 *
	 * @author Peter Cortes
	 */
	@Deprecated
	private class ChatSender implements Runnable
	{
		Scanner s;

		public void run()
		{
			// this is where the messages get sent to the server

			s = new Scanner(System.in);

			while (true)
			{
				try
				{
					out.writeObject(new SendMessageCommand(new Message(clientName, s.nextLine())));
					out.flush();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					System.err.println("chatsender: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * This class sends a disconnect command to the server before completely
	 * shutting down the program. An instance of this class is added to both the
	 * login window and the chat window, so that closing either will shut down
	 * the program.
	 *
	 * @author Peter Cortes
	 */
	private class ShutdownHandler implements EventHandler<WindowEvent>
	{
		@Override
		public void handle(WindowEvent event)
		{
			try
			{
				out.writeObject(new DisconnectCommand(clientName));
				out.close();
				in.close();
			}
			catch (IOException | NullPointerException e)
			{
				// do nothing, since the program is closing
			}
			finally
			{
				System.exit(0);
			}
		}
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage meow) throws Exception
	{
		prompt = new LoginStage();
		prompt.addLoginHandler(new LoginAction());

		chattStage = meow;

		Group root = new Group();
		Scene scene = new Scene(root, 200, 200);

		Button logout = new Button("logout");
		logout.addEventHandler(ActionEvent.ANY, new SignoutHandler());

		root.getChildren().add(logout);

		chattStage.setTitle("Start Chatting");
		chattStage.setScene(scene);
		chattStage.centerOnScreen();

		chattStage.setOnCloseRequest(new ShutdownHandler());
		prompt.setOnCloseRequest(new ShutdownHandler());
	}

	/**
	 * @see client.Client#update(shared.Message)
	 */
	@Override
	public void update(Message message)
	{
		// TODO finish this
		System.out.println(message);
	}

	@Override
	public String toString()
	{
		return String.format("CC%04dU%s", server.getLocalPort(), clientName);
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

		Application.launch();
	}
}
