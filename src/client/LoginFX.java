package client;

import javax.swing.Timer;

import server.ChattHypervisor;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LoginFX extends Application {

	private static final long serialVersionUID = -2889648528112988639L;

	private static final int MIN_USERNAME_LENGTH = 3;
	private static final String IP_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	// private static final Color RED = new Color(200, 50, 50);
	// private static final Color GREEN = new Color(50, 150, 50);
	// private static final Color GRAY = new Color(80, 80, 80);

	private Label labelIP = new Label("IP Address");
	private Label labelUsername = new Label("Username");
	private Label labelPort = new Label("Port");
	private Label status = new Label("awaiting input...");
	private TextField username = new TextField();
	private TextField address = new TextField();
	private TextField port = new TextField();
	private Button login = new Button("Login");
	private Button exit = new Button("Exit");
	private Stage mainStage;

	/**
	 * Login doesn't have a setupGUI method, instead its all done in the
	 * constructor.
	 */
	public LoginFX() {
	}

	/**
	 * clear is called when the user signs out to wipe the information that was
	 * previously entered.
	 */
	public void clear() {
		status.setText("awaiting input...");
		username.setText("");
		address.setText("");
		port.setText("");
	}

	/**
	 * This method is used to add a login listener from outside this class.
	 * 
	 * @param l
	 *            the login listener that runs when the login button is pressed.
	 */
	public void addLoginListener(EventHandler<ActionEvent> event) {

		login.setOnAction(event);
	}

	/**
	 * This method wraps a regular expression that's used to check if the ip
	 * address is a valid form.
	 * 
	 * @param ip
	 *            the entered IP address as a string
	 * @return true if acceptable, false otherwise
	 */
	public boolean validateIP(String ip) {
		return ip.matches("localhost") || ip.matches(IP_REGEX);
	}

	/**
	 * This method checks all the fields to make sure they contain valid data
	 * 
	 * @return true if all fields valid, false otherwise
	 */
	public boolean verifyFields() {
		// check name first
		if (username.getText().length() < MIN_USERNAME_LENGTH) {
			setWarning("username too short");
			return false;
		}

		// check address if name is invalid
		if (!validateIP(address.getText())) {
			setWarning("invalid address");
			return false;
		}

		// only check port if name and address are okay
		try {
			Integer.parseInt(port.getText());
		} catch (NumberFormatException e) {
			setWarning("invalid port number");
			return false;
		}

		// status.setForeground(GREEN);
		status.setText("attempting connection");

		return true;
	}

	/**
	 * getFields is called when the parent class wants to read the information
	 * entered into the fields.
	 * 
	 * @return an array of strings with the field contents
	 */
	public String[] getFields() {
		String[] s = new String[3];

		s[0] = username.getText();
		s[1] = address.getText();
		s[2] = port.getText();

		return s;
	}

	public String getName() {
		return username.getText();
	}

	public String getAddress() {
		return address.getText();
	}

	public String getPort() {
		return port.getText();
	}

	/**
	 * This private method is used to instantly set a warning message from
	 * within this class.
	 * 
	 * @param message
	 *            the warning to show
	 */
	private void setWarning(String message) {
		// status.setForeground(RED);
		status.setText(message);
	}

	/**
	 * This method is used to set a warning message from outside this class. The
	 * warning appears in red after 400 milliseconds.
	 * 
	 * @param message
	 *            the desired warning message
	 */
	public void setDelayedWarning(String message) {
		final Timer update = new Timer(400, e -> {
			// status.setForeground(RED);
				status.setText(message);
			});

		update.setRepeats(false);
		update.start();
	}

	@Override
	public void start(Stage arg0) throws Exception {
		// TODO Auto-generated method stub
		mainStage = arg0;
		mainStage.setTitle("Log in to a Chatt server");
		mainStage.setResizable(false);

		Group root = new Group();
		Scene scene = new Scene(root, 355, 130, Color.LIGHTGRAY);

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(0, 5, 0, 5));

		labelUsername.setPadding(new Insets(0, 110, 0, 0));
		grid.add(labelUsername, 0, 0);
		grid.add(username, 1, 0);

		grid.add(labelIP, 0, 1);
		grid.add(address, 1, 1);

		grid.add(labelPort, 0, 2);
		grid.add(port, 1, 2);

		exit.setMaxWidth(Double.MAX_VALUE);
		login.setMaxWidth(Double.MAX_VALUE);
		grid.add(exit, 0, 3);
		grid.add(login, 1, 3);

		exit.setOnAction(e -> exitAction());
		grid.add(status, 1, 4);

		// grid.setGridLinesVisible(true);
		root.getChildren().add(grid);

		mainStage.setScene(scene);
		mainStage.centerOnScreen();
		mainStage.show();

	}

	private void exitAction() {
		// TODO Auto-generated method stub
		final Timer t = new Timer(100, ap -> System.exit(0));
		status.setText("quitting...");
		t.setRepeats(false);
		t.start();
	}

	public static void main(String[] args) {
		Application.launch();
	}

	public void close() {
		// TODO Auto-generated method stub
		mainStage.close();
	}
}