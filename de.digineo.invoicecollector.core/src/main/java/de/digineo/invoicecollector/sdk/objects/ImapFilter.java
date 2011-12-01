package de.digineo.invoicecollector.sdk.objects;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class ImapFilter extends DataObject {
	
	String name;
	String search;
	String subject;
	String fileName;
	
	public ImapFilter() { }
	
	public ImapFilter(String name, String subject, String fileName, String search) {
		this.name = name;
		this.subject = subject;
		this.fileName = fileName;
		this.search = search;
	}
	
	public void extractFromResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("id");
			name = result.getString("name");
			search = result.getString("search");
			subject = result.getString("subject");
			fileName = result.getString("filename");
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public void extractFromAccountResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("imap_filter_id");
			name = result.getString("imap_filter_name");
			search = result.getString("imap_filter_search");
			subject = result.getString("imap_filter_subject");
			fileName = result.getString("imap_filter_filename");
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getSearch() {
		return search;
	}
	
	public String getSubject() {
		return subject;
	}
	
	/**
	 * Löscht den aktuellen Filter
	 * @throws ConnectorException 
	 * @throws SQLException 
	 */
	public void remove() throws ConnectorException, SQLException {
		API.INSTANCE.getConnector().execute("DELETE FROM imap_filters WHERE id = '"+this.id+"'");
		API.INSTANCE.getConnector().execute("DELETE FROM accounts WHERE imap_filter_id = '"+this.id+"'");
		
		
		API.INSTANCE.getConnector().getCon().close();
		API.INSTANCE.getConnector().createConnection();
	}
	
	/**
	 * Erstellt einen ImapFilter und speichert die ID.
	 */
	public void create() throws ConnectorException {
		id = API.INSTANCE.getConnector().executeGetId("INSERT INTO imap_filters (name, search, subject, filename) " +
				"VALUES ('"+this.name+"', '"+this.search+"', '"+this.subject+"', '"+this.fileName+"')");

		try {
			API.INSTANCE.getConnector().getCon().close();
			API.INSTANCE.getConnector().createConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Editiert die Suchfilter eines Filters.
	 * @param subject
	 * @param fileName
	 * @param search
	 * @throws ConnectorException
	 */
	public void update(String subject, String fileName, String search) throws ConnectorException {
		this.subject = subject;
		this.fileName = fileName;
		this.search = search;
		API.INSTANCE.getConnector().execute("UPDATE imap_filters SET " +
				"subject = '"+subject+"', filename = '"+fileName+"', " +
						"search = '"+search+"' WHERE id = '"+this.id+"'");
	}
	
}
