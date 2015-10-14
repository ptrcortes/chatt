package server;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class represents a user of Chatt. It currently contains methods for
 * generating a hashed password and comparing a given password to the original
 * one.
 *
 * @author Peter Cortes
 */
public class UserAccount implements Serializable
{
	private static final long serialVersionUID = 6287051133452019725L;

	// note: declaring a variable as final doesn't necessarily make it immutable
	public final String username;
	public final byte[] salt = new byte[32];
	public final byte[] shPassword;

	/**
	 * Takes in a username and a desired password. The plaintext password is not
	 * stored at any time. Instead, a 32 byte salt is generated using an
	 * instance of SecureRandom. This salt is prepended to the user's password,
	 * and then the whole thing is hashed using SHA-256. This salt is stored
	 * with the account, to be used to hash the provided password when the user
	 * attempts to log in. Finally, this newly hashed and salted password is
	 * compared with the stored hashed and salted password. If the two match,
	 * the login is valid.
	 * 
	 * @param name The user's desired username
	 * @param password The user's desired password
	 */
	public UserAccount(String name, String password)
	{
		username = name;
		new SecureRandom().nextBytes(salt);
		shPassword = hashWithSalt(password);
	}

	/**
	 * Takes in a plaintext password in the form of a UTF-8 String, and returns
	 * a hashed salted version of the password as a byte array.
	 * 
	 * @param plaintextPass the password to hash and salt
	 * @return a byte array form of the password that's been salted and hashed
	 */
	private byte[] hashWithSalt(String plaintextPass)
	{
		byte[] passbytes = null;

		try
		{
			passbytes = plaintextPass.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			System.err.println(e.getMessage());
		}

		byte[] saltedpass = new byte[passbytes.length + salt.length];
		saltedpass = Arrays.copyOf(salt, salt.length + passbytes.length);
		for (int i = salt.length; i < saltedpass.length; i++)
			saltedpass[i] = passbytes[i - salt.length];

		MessageDigest d = null;
		try
		{
			d = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			System.err.println(e.getMessage());
		}

		return d.digest(saltedpass);
	}

	/**
	 * Compares an arbitrary password with the password tied to this account.
	 * 
	 * @param password the password to test against
	 * @return true if the passwords match, false otherwise
	 */
	public boolean comparePassword(String password)
	{
		byte[] givenPass = hashWithSalt(password);
		return Arrays.equals(shPassword, givenPass);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserAccount other = (UserAccount) obj;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("username: " + username + "\n");
		sb.append("salt: " + Arrays.toString(salt) + "\n");
		sb.append("salted and hashed password: " + Arrays.toString(shPassword) + "\n");
		return sb.toString();
	}

	/**
	 * This main method is only used to test the instance methods of this class.
	 * 
	 * @param args not used
	 */
	public static void main(String args[])
	{
		UserAccount a = new UserAccount("meow", "passpass");
		System.out.println(a.comparePassword("pass"));
		System.out.println(a.comparePassword("passpass"));
		System.out.println(a);
	}
}
