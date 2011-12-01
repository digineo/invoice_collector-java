package de.digineo.invoicecollector.sdk.objects;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class ImapAccount extends Account {
	
	private String host;
	private boolean ssl;
	private int port;
	private ImapFilter filter;
	
	public ImapAccount() { }
	
	public ImapAccount(String host, String username, String password, int port, boolean ssl) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.ssl = ssl;
	}
	
	public void extractFromResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("id");
			host = result.getString("host");
			ssl = result.getBoolean("ssl");
			username = result.getString("username");
			password = result.getString("password");
			port = result.getInt("port");
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public void extractFromAccountResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("imap_account_id");
			host = result.getString("imap_account_host");
			ssl = result.getBoolean("imap_account_ssl");
			username = result.getString("imap_account_username");
			password = result.getString("imap_account_password");
			port = result.getInt("imap_account_port");
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public int getPort() {
		return port;
	}
	
	public String getHost() {
		return host;
	}
	
	public boolean isSsl() {
		return ssl;
	}
	
	public ImapFilter getFilter() {
		return filter;
	}
	
	/**
	 * Erstellt einen ImapAccount und speichert die ID.
	 */
	public void create() throws ConnectorException {
		id = API.INSTANCE.getConnector().executeGetId("INSERT INTO imap_accounts (username, password, host, port, ssl) " +
				"VALUES ('"+this.username+"', '"+this.password+"', '"+this.host+"', '"+this.port+"', '"+(this.ssl ? 1 : 0)+"')");

		try {
			API.INSTANCE.getConnector().getCon().close();
			API.INSTANCE.getConnector().createConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Löscht den aktuellen Account
	 * @throws ConnectorException 
	 * @throws SQLException 
	 */
	public void remove() throws ConnectorException, SQLException {
		API.INSTANCE.getConnector().execute("DELETE FROM imap_accounts WHERE id = '"+this.id+"'");
		API.INSTANCE.getConnector().execute("DELETE FROM accounts WHERE imap_account_id = '"+this.id+"'");
		
		
		API.INSTANCE.getConnector().getCon().close();
		API.INSTANCE.getConnector().createConnection();
	}
	
	/**
	 * Editiert Usernamen, Passwort und Port eines Imap-Accounts.
	 * @param username
	 * @param password
	 * @param port
	 * @throws ConnectorException
	 */
	public void update(String username, String password, int port, boolean ssl) throws ConnectorException {
		this.username = username;
		this.password = password;
		this.port = port;
		this.ssl = ssl;
		API.INSTANCE.getConnector().execute("UPDATE imap_accounts SET " +
				"password = '"+password+"', username = '"+username+"', " +
						"port = '"+port+"', ssl = '"+(ssl ? 1 : 0)+"' WHERE id = '"+this.id+"'");
	}
	
}
