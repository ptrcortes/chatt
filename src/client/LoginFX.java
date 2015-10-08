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
public class LoginFX extends Stage
{
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final String IP_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final Color RED = Color.RED;
	private static final Color GREEN = Color.GREEN;
	private static final Color GRAY = Color.BLACK;

	private Label labelIP = new Label("IP Address");
	private Label labelUsername = new Label("Username");
	private Label labelPort = new Label("Port");
	private Label status = new Label("awaiting input...");
	private TextField username = new TextField();
	private TextField address = new TextField();
	private TextField port = new TextField();
	private Button login = new Button("Login");
	private Button exit = new Button("Exit");

	/**
	 * clear is called when the user signs out to wipe the information that was
	 * previously entered.
	 */
	public void clear()
	{
		status.setTextFill(null);
		status.setText("awaiting input...");
		username.setText("");
		address.setText("");
		port.setText("");
	}

	/**
	 * This method is used to add a login listener from outside this class.
	 * 
	 * @param l the login listener that runs when the login button is pressed.
	 */
	public void addLoginHandler(EventHandler<ActionEvent> handler)
	{
		login.setOnAction(handler);
	}

	/**
	 * This method wraps a regular expression that's used to check if the ip
	 * address is a valid form.
	 * 
	 * @param ip the entered IP address as a string
	 * @return true if acceptable, false otherwise
	 */
	public boolean validateIP(String ip)
	{
		return ip.matches("localhost") || ip.matches(IP_REGEX);
	}

	/**
	 * This method checks all the fields to make sure they contain valid data
	 * 
	 * @return true if all fields valid, false otherwise
	 */
	public boolean verifyFields()
	{
		// check name first
		if (username.getText().length() < MIN_USERNAME_LENGTH)
		{
			setWarning("username too short");
			return false;
		}

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

	public LoginFX()
	{
		setTitle("Login to a Chatt server");
		setResizable(false);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(2);
		grid.setPadding(new Insets(0, 5, 0, 5));

		Text title = new Text("Login to Chatt");
		title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 25));
		grid.add(title, 0, 0, 2, 1);

		grid.add(labelUsername, 0, 1);
		grid.add(username, 1, 1);

		grid.add(labelIP, 0, 2);
		grid.add(address, 1, 2);

		grid.add(labelPort, 0, 3);
		grid.add(port, 1, 3);

		exit.setMaxWidth(Double.MAX_VALUE);
		login.setDefaultButton(true);
		HBox loginpane = new HBox(200);
		loginpane.setAlignment(Pos.BOTTOM_RIGHT);
		loginpane.getChildren().add(login);
		grid.add(loginpane, 1, 6);

		// grid.add(exit, 0, 3);

		exit.setOnAction(e -> exitAction());
		grid.add(status, 1, 4);

		// grid.setGridLinesVisible(true);
		Scene scene = new Scene(grid, 300, 300, Color.LIGHTGRAY);
		setScene(scene);
		centerOnScreen();
		show();
	}

	private void exitAction()
	{
		status.setTextFill(GRAY);
		status.setText("quitting...");
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), ae -> System.exit(0)));
		timeline.play();
	}
}