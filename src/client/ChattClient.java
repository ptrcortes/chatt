package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Optional;

import commands.Command;
import commands.clientsent.CreateRoomCommand;
import commands.clientsent.DisconnectCommand;
import commands.clientsent.RequestNameCommand;
import commands.clientsent.SendMessageCommand;
import commands.clientsent.SwitchRoomCommand;
import commands.serversent.LoginResponse;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import shared.DuplicateNameException;
import shared.Message;
import shared.RoomPackage;

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
	private ListView<RoomPackage> rooms;
	private ObservableList<RoomPackage> availableRooms = FXCollections.observableArrayList();
	private TextArea chattArea;
	private Button sendButton;
	private Button connectButton;
	private Button createButton;
	private Text allRooms;
	private Text userName;
	private Text currentRoom;
	@SuppressWarnings("unused")
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
					// service = ChattHypervisor.getInstance();
					// connection called separately to include timeout
					server.connect(new InetSocketAddress(prompt.getAddress(), Integer.parseInt(prompt.getPort())), 500);
					out = new ObjectOutputStream(server.getOutputStream());
					in = new ObjectInputStream(server.getInputStream());

					// write out the name of this client
					out.writeObject(clientName);
					out.flush();

					// if the connection was accepted
					if (((LoginResponse) in.readObject()).accepted)
					{
						connected = true;

						// login accepted
						new Timeline(new KeyFrame(Duration.millis(400), ae -> {
							prompt.hide();
							chattStage.show();
						})).play();

						// start a thread for handling server events
						new Thread(new ServerHandler()).start();
						// new Thread(new ChatSender()).start();

						chattStage.setTitle("Chatt: " + clientName);
						userName.setText(clientName);
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
				catch (ClassNotFoundException x)
				{
					x.printStackTrace();
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

			new Timeline(new KeyFrame(Duration.millis(400), ae -> {
				chattStage.hide();
				prompt.show();
			})).play();
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
				out.writeObject(new RequestNameCommand(clientName));
				out.flush();

				// read the next command from the server and execute it
				while (connected)
				{
					@SuppressWarnings("unchecked")
					Command<Client> c = (Command<Client>) in.readObject();
					c.runOn(ChattClient.this);
				}
			}
			catch (ClassCastException e)
			{
				System.out.println("class cast");
				e.printStackTrace();
			}
			catch (OptionalDataException e)
			{
				System.out.println("wtf\n");
				e.printStackTrace();
			}
			catch (SocketException | EOFException e)
			{
				// System.out.println("returning from ServerHandler");
				return; // "gracefully" terminate after disconnect
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
				e.printStackTrace();
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
		chattLocation.setAlignment(Pos.CENTER_RIGHT);
		chattLocation.setStyle("-fx-background-color: " + CHATTBLUE);
		chattLocation.getChildren().addAll(makeChattSpace(), makeChattBoxAndButton());

		HBox userInfo = new HBox();
		userInfo.setAlignment(Pos.CENTER_RIGHT);
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
		roomsBox.setAlignment(Pos.CENTER);
		roomsBox.setStyle("-fx-background-color: " + CHATTBLUE);

		roomsBox.getChildren().addAll(makeRoomsTitle(), makeListOfRooms(), makeRoomButtons());

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

	private HBox makeChattBoxAndButton()
	{
		HBox boxAndButton = new HBox();
		boxAndButton.getChildren().addAll(makeChattArea(), makeSendButton());
		return boxAndButton;
	}

	private HBox makeRoomButtons()
	{
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.getChildren().addAll(makeConnectButton(), makeCreateButton());
		return buttonBox;
	}

	private GridPane makeInfoGrid()
	{
		userName = new Text(clientName);
		userName.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));

		 currentRoom = new Text("");
		 currentRoom.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));

		grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(5);
		grid.setPadding(new Insets(0, 5, 0, 5));

		grid.add(userName, 0, 0);
		// grid.add(currentRoom, 1, 0);

		return grid;
	}

	private Button makeConnectButton()
	{
		connectButton = new Button("Connect");
		connectButton.setPrefWidth(125);
		connectButton.setOnAction(ae -> {
			try
			{
				// TODO: find a way to display system messages to users
				System.out.println("selected room: " + rooms.getSelectionModel().getSelectedItem().toLongString());
				out.writeObject(new SwitchRoomCommand(clientName, rooms.getSelectionModel().getSelectedItem().id));
				out.flush();
				// out.writeObject(new RequestNameCommand(clientName));
				// out.flush();
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			catch (NullPointerException e)
			{}
		});
		return connectButton;
	}

	private Button makeCreateButton()
	{
		createButton = new Button("Create");
		createButton.setPrefWidth(125);
		createButton.setOnAction(ae -> {
			try
			{
				// TODO: find a way to display system messages to users

				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("");
				dialog.setGraphic(null);
				dialog.setHeaderText("Create a new Chatt room!");
				dialog.setContentText("Please enter a name for the room:");

				Optional<String> result = dialog.showAndWait();

				result.ifPresent(inputName -> {
					try
					{
						out.writeObject(new CreateRoomCommand(clientName, inputName));
						// out.writeObject(new RequestNameCommand(clientName));
						out.flush();
					}
					catch (IOException e)
					{}
				});
			}
			catch (NullPointerException e)
			{}
		});

		return createButton;
	}

	private ListView<RoomPackage> makeListOfRooms()
	{
		rooms = new ListView<RoomPackage>();
		rooms.setPrefHeight(600);
		rooms.setItems(availableRooms);
		return rooms;
	}

	private Text makeRoomsTitle()
	{
		allRooms = new Text("Loading rooms...");
		allRooms.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

		return allRooms;
	}

	private ListView<Message> makeChattSpace()
	{
		chatts = new ListView<Message>();

		chatts.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>()
		{
			@Override
			public ListCell<Message> call(ListView<Message> messageListView)
			{
				return new ListCell<Message>()
				{
					@Override
					protected void updateItem(Message m, boolean empty)
					{
						super.updateItem(m, empty);

						if (empty || m == null)
						{
							setText(null);
							setGraphic(null);
						}
						else if (m.meMessage)
						{
							setText(m.toString());
							setTextFill(Paint.valueOf("black"));
							setFont(Font.font("Verdana", FontWeight.BOLD, -1));
						}
						else if (m.sysMessage)
						{
							setText(m.toString());
							setTextFill(Paint.valueOf(("blue")));
							setFont(Font.font("Verdana", FontWeight.BOLD, -1));
						}
						else
						{
							setText(m.toString());
							setTextFill(Paint.valueOf("black"));
							setFont(Font.font("Verdana", FontWeight.NORMAL, -1));
						}
					}
				};
			}
		});

		chatts.setPrefHeight(700);
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
		chattArea.setPrefHeight(50);
		chattArea.setPromptText("Type here; press enter to send");
		chattArea.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
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
		sendButton.setPrefWidth(125);
		sendButton.setPrefHeight(50);
		sendButton.setOnAction(ae -> {
			String s = validateText(chattArea.getText());
			if (!s.equals(""))
				sendMessage(s);
		});
		return sendButton;
	}

	private void sendMessage(String s)
	{
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

		new Timeline(new KeyFrame(Duration.millis(10), ae -> chattArea.clear())).play();
	}

	/**
	 * @see client.Client#updateMessageList(shared.Message) This method sends a
	 *      new string to the chat history observable list which will update the
	 *      ListView in the GUI as soon as anything is added.
	 */
	@Override
	public void updateMessageList(Message message)
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
	public void setRoomName(String roomName)
	{
		currentRoom.setText(roomName);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.Client#updateRoomList(java.util.LinkedList)
	 */
	@Override
	public void updateRoomList(LinkedList<RoomPackage> rooms)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (allRooms.getText().contains("Loading"))
					allRooms.setText("Available rooms:");

				if (availableRooms.equals(rooms))
					return;

				availableRooms.clear();
				for (RoomPackage r: rooms)
					availableRooms.add(r);
			}
		});
	}

	@Override
	public String toString()
	{
		return String.format("CC%04dU%s", server.getLocalPort(), clientName);
	}

	public static void main(String[] args) throws IOException
	{
		// ChattHypervisor.main(null);
		Application.launch();
	}
}
