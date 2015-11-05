package client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * TODO: add description
 *
 * @author Gabe Serrano
 * @author Peter Cortes
 */
public class LoginStage extends Stage
{
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final String IP_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final Color RED = Color.RED;
	private static final Color GREEN = Color.GREEN;
	private static final Color BLACK = Color.BLACK;

	private final Label status = new Label("awaiting input...");
	private final TextField username = new TextField();
	private final PasswordField password = new PasswordField();
	private final TextField address = new TextField();
	private final TextField port = new TextField();
	private final Button login = new Button("Login");
	private final Button exit = new Button("Exit");

	/**
	 * Clears all input boxes for the login window, and resets the status color
	 * to black. This method is called when the user signs out in order to wipe
	 * the information that was previously entered.
	 */
	public void clear()
	{
		status.setTextFill(BLACK);
		status.setText("awaiting input...");
		username.setText("");
		address.setText("");
		port.setText("");
	}

	/**
	 * Adds a login listener from outside this class.
	 * 
	 * @param handler the class with code that runs when the login button is
	 *            pressed.
	 */
	public void addLoginHandler(EventHandler<ActionEvent> handler)
	{
		login.setOnAction(handler);
	}

	/**
	 * Wraps a regular expression that's used to check if the ip address is a
	 * valid form.
	 * 
	 * @param ip the entered IP address as a string
	 * @return true if acceptable, false otherwise
	 */
	public boolean validateIP(String ip)
	{
		return ip.matches("localhost") || ip.matches(IP_REGEX);
	}

	/**
	 * Contains multiple checks for username validity. Currently, this method
	 * checks the length of the username and verifies that it only contains
	 * letters and numbers.
	 * 
	 * @param name the username to verify
	 * @return true if valid, false otherwise
	 */
	public boolean validateUserName(String name)
	{
		if (username.getText().length() < MIN_USERNAME_LENGTH)
		{
			setWarning("username too short");
			return false;
		}

		if (!name.matches("^[a-zA-Z0-9]*$"))
		{
			setWarning("invalid username");
			return false;
		}

		return true;
	}

	/**
	 * Checks all the fields to make sure they contain valid data.
	 * 
	 * @return true if all fields valid, false otherwise
	 */
	public boolean verifyFields()
	{
		// check name first
		if (!validateUserName(username.getText()))
			return false;

		// check address if name is valid
		if (!validateIP(address.getText()))
		{
			setWarning("invalid address");
			return false;
		}

		// only check port if name and address are okay
		try
		{
			Integer.parseInt(port.getText());
		}
		catch (NumberFormatException e)
		{
			setWarning("invalid port number");
			return false;
		}

		status.setTextFill(GREEN);
		status.setText("attempting connection");

		return true;
	}

	public String getName()
	{
		return username.getText();
	}

	public String getAddress()
	{
		return address.getText();
	}

	public String getPort()
	{
		return port.getText();
	}

	/**
	 * This private method is used to instantly set a warning message from
	 * within this class.
	 * 
	 * @param message the warning to show
	 */
	private void setWarning(String message)
	{
		status.setTextFill(RED);
		status.setText(message);
	}

	/**
	 * This method is used to set a warning message from outside this class. The
	 * warning appears in red after 400 milliseconds.
	 * 
	 * @param message the desired warning message
	 */
	public void setDelayedWarning(String message)
	{
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
			status.setTextFill(RED);
			status.setText(message);
		}));
		timeline.play();
	}

	public LoginStage()
	{
		setTitle("Chatt");
		setResizable(false);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(5);
		grid.setPadding(new Insets(0, 5, 0, 5));

		Text title = new Text("Log in to Chatt");
		title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 25));
		HBox titlepane = new HBox(200);
		titlepane.setAlignment(Pos.TOP_LEFT);
		titlepane.getChildren().add(title);

		grid.add(titlepane, 0, 0);

		username.setPromptText("Username");
		grid.add(username, 0, 1);

		password.setPromptText("Password");
		grid.add(password, 0, 2);

		address.setPromptText("Chatt Server Address");
		address.setText("localhost");
		grid.add(address, 0, 3);

		port.setPromptText("Chatt Server Port");
		port.setText("9001");
		grid.add(port, 0, 4);

		grid.add(status, 0, 5);

		exit.setMaxWidth(Double.MAX_VALUE);
		login.setDefaultButton(true);
		HBox loginpane = new HBox(200);
		loginpane.setAlignment(Pos.BOTTOM_RIGHT);
		loginpane.getChildren().add(login);
		grid.add(loginpane, 0, 6);

		// grid.add(exit, 0, 3);

		exit.setOnAction(e -> exitAction());

		// grid.setGridLinesVisible(true);
		Scene scene = new Scene(grid, 300, 300, Color.LIGHTGRAY);
		setScene(scene);
		centerOnScreen();
		show();
	}

	private void exitAction()
	{
		status.setTextFill(BLACK);
		status.setText("quitting...");
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), ae -> System.exit(0)));
		timeline.play();
	}
}