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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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
public class ChattClient extends Application implements Client {
	// TODO: Gabe and Charles: you can turn this class into a javafx
	// application, or you can use this class as a controller, and define a new
	// set of classes to be the jfx application.

	private String clientName; // this client's username
	private Socket serverConnection; // connection to server
	private ObjectOutputStream out; // output stream
	private ObjectInputStream in; // input stream

	private LoginFX prompt;

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
	private class LoginAction implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
		
			// TODO Auto-generated method stub
			// if the data is valid, try to connect

			if (prompt.verifyFields() == true) {
				clientName = prompt.getName();

				try {
					serverConnection.close();
				} catch (IOException e1) {
					System.err.println(e1.getMessage());
				} catch (NullPointerException e1) {
					// do nothing if serverConnection doesn't exist
				}

				try {
					serverConnection = new Socket();
					// connection called separately to include timeout
					serverConnection.connect(
							new InetSocketAddress(prompt.getAddress(), Integer
									.parseInt(prompt.getPort())), 500);
					out = new ObjectOutputStream(
							serverConnection.getOutputStream());
					in = new ObjectInputStream(
							serverConnection.getInputStream());

					// setupGUI(clientName, prompt.getAddress(),
					// prompt.getPort());

					// write out the name of this client
					out.writeObject(clientName);
					out.flush();

					// if the connection was accepted
					if (in.readBoolean() == true) {
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
						final Timer show = new Timer(400,
								event -> prompt.close());
						show.setRepeats(false);
						show.start();

						// start a thread for handling server events
						new Thread(new ServerHandler()).start();
						new Thread(new ChatSender()).start();
					}

					else {
						serverConnection.close();
						throw new DuplicateNameException();
					}
				} catch (DuplicateNameException x) {
					prompt.setDelayedWarning("username taken");
				} catch (NumberFormatException x) {
					prompt.setDelayedWarning("invalid number");
				} catch (IOException x) {
					prompt.setDelayedWarning(x.getMessage());
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
	private class ServerHandler implements Runnable {
		public void run() {
			try {
				// read the next command from the server and execute it
				while (connected) {
					@SuppressWarnings("unchecked")
					Command<Client> c = (Command<Client>) in.readObject();
					c.runOn(ChattClient.this);
				}
			} catch (SocketException | EOFException e) {
				return; // "gracefully" terminate after disconnect
			} catch (Exception e) {
				System.err.println(e.getMessage());
			} finally {
				try {
					serverConnection.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	private class ChatSender implements Runnable {
		Scanner s;

		public void run() {
			s = new Scanner(System.in);

			while (true) {
				try {
					out.writeObject(new SendMessageCommand(new Message(
							clientName, s.nextLine())));
				} catch (IOException e) {
					System.err.println("chatsender: " + e.getMessage());
				}
			}
		}
	}

	public ChattClient() {
		prompt = new LoginFX();
		prompt.addLoginListener(new LoginAction());
		prompt.main(null);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage mainStage) throws Exception {
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
	public void update(Message message) {
		// TODO Auto-generated method stub
		System.out.println(message);
	}

	@Override
	public String toString() {
		return String.format("CC%04dU%s", serverConnection.getLocalPort(),
				clientName);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("There was a problem setting the look and feel");
			e.printStackTrace();
		}

		new ChattClient();
		// Application.launch();
	}
}
