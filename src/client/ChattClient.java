package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import commands.Command;
import commands.DisconnectCommand;
import commands.SendMessageCommand;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import shared.DuplicateNameException;
import shared.Message;

/**
 *
 *
 * @author Peter Cortes
 * @author Gabe Serrano
 */
public class ChattClient extends Application implements Client
{
	private static final String CHATTBLUE = "#1E90FF;";
	private String clientName; // this client's username
	private Socket server; // connection to server
	private ObjectOutputStream out; // output stream
	private ObjectInputStream in; // input stream

	private boolean connected = true;
	private LoginStage prompt;

	private Stage chattStage;
	private GridPane grid;
	private ListView<Message> chatts;
	private ObservableList<Message> chattHistory = FXCollections.observableArrayList();
	private ListView<String> rooms;
	private ObservableList<String> availableRooms = FXCollections.observableArrayList();
	private TextArea chattArea;
	private Button sendButton;
	private Button connectButton;
	private Text allRooms;
	private Text userName;
	private Text currentRoom;
	private Text points;

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
						// new Thread(new ChatSender()).start();

						chattStage.setTitle("Chatt: " + clientName);
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

			Timeline timer = new Timeline(new KeyFrame(Duration.millis(400), ae -> {
				chattStage.hide();
				prompt.show();
			}));
			timer.play();
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
	/*
	 * @Deprecated private class ChatSender implements Runnable { Scanner s;
	 * 
	 * public void run() { // this is where the messages get sent to the server
	 * 
	 * s = new Scanner(System.in);
	 * 
	 * while (true) { try { out.writeObject(new SendMessageCommand(new
	 * Message(clientName, s.nextLine()))); out.flush(); } catch
	 * (SocketException e) { e.printStackTrace(); System.err.println(
	 * "chatsender: " + e.getMessage()); } catch (IOException e) {
	 * e.printStackTrace(); System.err.println("chatsender: " + e.getMessage());
	 * } } } }/*
	 * 
	 * /** This class sends a disconnect command to the server before completely
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
				out.flush();
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
		prompt.setOnCloseRequest(new ShutdownHandler());

		Group root = new Group();
		Scene scene = new Scene(root, 800, 600);

		Button logout = new Button("logout");
		logout.addEventHandler(ActionEvent.ANY, new SignoutHandler());

		BorderPane border = new BorderPane();
		border.prefHeightProperty().bind(scene.heightProperty());
		border.prefWidthProperty().bind(scene.widthProperty());
		border.setCenter(makeChattSpace());

		VBox chattLocation = new VBox();
		chattLocation.getChildren().addAll(makeChattSpace(), makeChattArea(), makeSendButton());

		HBox userInfo = new HBox();
		userInfo.setPadding(new Insets(15, 12, 15, 12));
		userInfo.setSpacing(10);
		userInfo.setStyle("-fx-background-color: " + CHATTBLUE);

		userInfo.getChildren().add(makeInfoGrid());

		// userInfo.getChildren().addAll(userName, currentRoom, points);

		HBox bottom = new HBox();
		bottom.setPadding(new Insets(15, 12, 15, 12));
		bottom.setSpacing(10);
		bottom.setStyle("-fx-background-color: " + CHATTBLUE);

		VBox roomsBox = new VBox();
		roomsBox.getChildren().addAll(makeRoomsTitle(), makeListOfRooms(), makeConnectButton());

		border.setTop(userInfo);
		border.setBottom(bottom);
		border.setLeft(roomsBox);

		border.setCenter(chattLocation);

		root.getChildren().add(border);

		chattStage = meow;
		chattStage.setTitle("Chatt: " + clientName);
		chattStage.setResizable(false);
		chattStage.setScene(scene);
		chattStage.centerOnScreen();
		chattStage.setOnCloseRequest(new ShutdownHandler());
		chattArea.requestFocus();
	}

	private GridPane makeInfoGrid()
	{
		userName = new Text("DemoUserName");
		userName.setFont(Font.font("Tahoma", FontWeight.BOLD, 10));

		currentRoom = new Text("DemoRoomName");
		currentRoom.setFont(Font.font("Tahoma", FontWeight.BOLD, 10));

		points = new Text("DemoPoints");
		points.setFont(Font.font("Tahoma", FontWeight.BOLD, 10));

		grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(5);
		grid.setPadding(new Insets(0, 5, 0, 5));

		grid.add(userName, 0, 0);
		grid.add(currentRoom, 1, 0);
		grid.add(points, 2, 0);

		return grid;
	}

	private Button makeConnectButton()
	{
		connectButton = new Button("Connect");
		connectButton.setOnAction(ae -> System.out.println("Button Works"));
		return connectButton;
	}

	private ListView<String> makeListOfRooms()
	{
		rooms = new ListView<String>();
		rooms.setItems(availableRooms);
		return rooms;
	}

	private Text makeRoomsTitle()
	{
		allRooms = new Text("All Available Rooms");
		allRooms.setFont(Font.font("Tahoma", FontWeight.NORMAL, 18));
		return allRooms;
	}

	private ListView<Message> makeChattSpace()
	{
		chatts = new ListView<Message>();
		chatts.setItems(chattHistory);
		return chatts;
	}

	private String validateText(String text)
	{
		// TODO: expand the message validation
		String out = text.trim();
		return out;
	}

	private TextArea makeChattArea()
	{
		chattArea = new TextArea();
		chattArea.setPromptText("Type here; press enter to send");
		chattArea.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				// TODO Auto-generated method stub
				if (ke.getCode().equals(KeyCode.ENTER))
				{
					String text = validateText(chattArea.getText());
					if (!text.equals(""))
						sendMessage(text);
				}
			}
		});
		return chattArea;
	}

	private Button makeSendButton()
	{
		sendButton = new Button("Send");
		sendButton.setOnAction(ae -> {
			String s = chattArea.getText().trim();
			if (!s.equals(""))
				sendMessage(s);
		});
		return sendButton;
	}

	private void sendMessage(String s)
	{
		// TODO Auto-generated method stub
		try
		{
			if (s.startsWith("/me "))
			{
				s = s.substring(s.indexOf("/me ") + 4);
				out.writeObject(new SendMessageCommand(new Message(clientName, s, true)));
			}
			else
				out.writeObject(new SendMessageCommand(new Message(clientName, s)));

			out.flush();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.err.println("chatsender: " + e.getMessage());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("chatsender: " + e.getMessage());
		}
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), ae -> chattArea.clear()));
		timeline.play();

	}

	/**
	 * @see client.Client#update(shared.Message) This method sends a new string
	 *      to the chat history observable list which will update the ListView
	 *      in the GUI as soon as anything is added.
	 */
	@Override
	public void update(Message message)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				chattHistory.add(message);
			}
		});

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
