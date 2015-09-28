// Authors: Peter Cortes and Garrett Macduffee
// This JFrame is used to log in to the jukebox
// program.

package client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * LogIn is a window used for logging in to the net paint system. The user can
 * choose a username, and specify what address to connect to. It notifies the
 * user when problems occur and what its status is.
 * 
 * @author Peter Cortes
 * @author Garrett MacDuffee
 */
public class Login extends JFrame
{
	private static final long serialVersionUID = -2889648528112988639L;

	private static final int MIN_USERNAME_LENGTH = 3;
	private static final String IP_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final Color RED = new Color(200, 50, 50);
	private static final Color GREEN = new Color(50, 150, 50);
	private static final Color GRAY = new Color(50, 50, 50);

	private JLabel status = new JLabel("awaiting input...");
	private JTextField name = new JTextField();
	private JTextField address = new JTextField();
	private JTextField port = new JTextField();
	private JButton login = new JButton("Login");
	private JButton quit = new JButton("Exit");

	/**
	 * Login doesn't have a setupGUI method, instead its all done in the
	 * constructor.
	 */
	public Login()
	{
		setLayout(new GridLayout(5, 2));
		setSize(400, 130);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Login to a Chatt server");

		add(new JLabel("    Username"));
		add(name);

		add(new JLabel("    IP Address"));
		add(address);

		add(new JLabel("    Port"));
		add(port);

		add(quit);
		add(login);

		add(new JLabel("    Status:"));
		add(status);

		getRootPane().setDefaultButton(login);
		quit.addActionListener(e -> {
			final Timer t = new Timer(100, ap -> System.exit(NORMAL));
			status.setForeground(GRAY);
			status.setText("quitting...");
			t.setRepeats(false);
			t.start();
		});
	}

	/**
	 * clear is called when the user signs out to wipe the information that was
	 * previously entered.
	 */
	public void clear()
	{
		status.setForeground(null);
		status.setText("awaiting input...");
		name.setText("");
		address.setText("");
		port.setText("");
	}

	/**
	 * This method is used to add a login listener from outside this class.
	 * 
	 * @param l the login listener that runs when the login button is pressed.
	 */
	public void addLoginListener(ActionListener l)
	{
		login.addActionListener(l);
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
		if (name.getText().length() < MIN_USERNAME_LENGTH)
		{
			setWarning("username too short");
			return false;
		}

		// check address if name is invalid
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

		status.setForeground(GREEN);
		status.setText("attempting connection");

		return true;
	}

	/**
	 * getFields is called when the parent class wants to read the information
	 * entered into the fields.
	 * 
	 * @return an array of strings with the field contents
	 */
	public String[] getFields()
	{
		String[] s = new String[3];

		s[0] = name.getText();
		s[1] = address.getText();
		s[2] = port.getText();

		return s;
	}

	public String getName()
	{
		return name.getText();
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
		status.setForeground(RED);
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
		final Timer update = new Timer(400, e -> {
			status.setForeground(RED);
			status.setText(message);
		});

		update.setRepeats(false);
		update.start();
	}
}