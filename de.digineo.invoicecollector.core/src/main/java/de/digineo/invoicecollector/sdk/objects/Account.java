package de.digineo.invoicecollector.sdk.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class Account extends DataObject {
	
	protected String module;
	private boolean active;
	private boolean autoPrint;
	protected String username;
	protected String password;
	private int imapAccountId;
	private int imapFilterId;
	private ImapAccount imapAccount;
	private ImapFilter imapFilter;
	
	public void extractFromResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("id");
			module = result.getString("module");
			active = result.getBoolean("active");
			autoPrint = result.getBoolean("autoprint");
			username = result.getString("username");
			password = result.getString("password");
			imapAccountId = result.getInt("imap_account_id");
			imapFilterId = result.getInt("imap_filter_id");
			
			if(imapAccountId > 0) {
				imapAccount = new ImapAccount();
				imapAccount.extractFromAccountResult(result);
			}
			
			if(imapFilterId > 0) {
				imapFilter = new ImapFilter();
				imapFilter.extractFromAccountResult(result);
			}
			
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public Account() { }
	
	public Account(String username, String password, String module) {
		this.username = username;
		this.password = password;
		this.module = module;
	}
	
	public Account(String username, ImapAccount imapAccount, ImapFilter imapFilter) {
		this.username = username;
		this.imapAccount = imapAccount;
		this.imapFilter = imapFilter;
		this.imapAccountId = imapAccount.getId();
		this.imapFilterId = imapFilter.getId();
		this.module = "";
	}
	
	public String getModule() {
		return module;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getImapAccountId() {
		return imapAccountId;
	}
	
	public int getImapFilterId() {
		return imapFilterId;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isAutoPrint() {
		return autoPrint;
	}
	
	public ImapAccount getImapAccount() {
		return imapAccount;
	}
	
	public ImapFilter getImapFilter() {
		return imapFilter;
	}
	
	/**
	 * Speicher den aktuellen Account in die Datenbank.
	 * @throws ConnectorException
	 */
	public void create() throws ConnectorException {
		id = API.INSTANCE.getConnector().executeGetId("INSERT INTO "+tableName+"s (username, password, " +
				"module, imap_account_id, imap_filter_id) " +
				"VALUES ('"+this.username+"', '"+this.password+"', '"+this.module+"', " +
						"'"+this.imapAccountId+"', '"+this.imapFilterId+"')");

		try {
			API.INSTANCE.getConnector().getCon().close();
			API.INSTANCE.getConnector().createConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Editiert das Passwort des Accounts.
	 * @param password Neues Passwort
	 * @throws ConnectorException
	 */
	public void update(String password) throws ConnectorException {
		this.password = password;
		API.INSTANCE.getConnector().execute("UPDATE accounts SET " +
				"password = '"+password+"' WHERE id = '"+this.id+"'");
	}
	
	/**
	 * Editiert den Username.
	 * @param username
	 * @throws ConnectorException
	 */
	public void updateUsername(String username) throws ConnectorException {
		this.username = username;
		API.INSTANCE.getConnector().execute("UPDATE accounts SET " +
				"username = '"+username+"' WHERE id = '"+this.id+"'");
	}
	
	/**
	 * Entfernt einen User und all seine Rechnungen.
	 * @throws ConnectorException
	 * @throws SQLException
	 * @throws ApiException
	 */
	public void remove() throws ConnectorException, SQLException, ApiException {
		ArrayList<Invoice> invoices = API.INSTANCE.getInvoicesByAccount(module, username);
		
		for(Invoice invoice : invoices) {
			invoice.remove();
		}

		API.INSTANCE.getConnector().execute("DELETE FROM accounts WHERE id = '"+this.id+"'");
		
		
		API.INSTANCE.getConnector().getCon().close();
		API.INSTANCE.getConnector().createConnection();
		
	}
	
}
