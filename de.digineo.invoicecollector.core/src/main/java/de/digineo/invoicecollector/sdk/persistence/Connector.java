package de.digineo.invoicecollector.sdk.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class Connector {
	
	private Connection con;
	
	/**
	 * Baut die Verbindung zur Datenbank auf und erstellt eine Connection.
	 * @throws ConnectorException
	 */
	public Connector() throws ConnectorException {
		try {
			Class.forName("org.sqlite.JDBC");
			createConnection();
			createDatabase();
		} catch (ClassNotFoundException e) {
			throw new ConnectorException("SQLite JDBC konnte nicht gefunden werden: "+e.getMessage());
		} catch (SQLException e) {
			throw new ConnectorException("SQL-Fehler: "+e.getMessage());
		}
	}
	
	public void createConnection() throws SQLException {
		con = DriverManager.getConnection("jdbc:sqlite:invoicecollector.db");
	}
	
	/**
	 * Erstellt die Datenbank, falls sie noch nicht existiert.
	 * Liste dabei die Datei invoicecollector.sql ein und verarbeitet die Kommandos.
	 */
	private void createDatabase() throws ConnectorException {
		try {
			Properties props = new Properties();
			props.load(Connector.class.getClassLoader().getResourceAsStream("database.properties"));
			for(int i=1;;i++){
				String statement=props.getProperty(String.valueOf(i));
				if(statement==null){
					break;
				}
				this.execute(statement);
			}
		} catch (IOException e) {
			throw new ConnectorException("Fehler beim initialisieren der Datenbank: "+e.getMessage());
		}
	}
	
	/**
	 * Gibt die aktuelle Verbindung zurück.
	 * @return
	 */
	public Connection getCon() {
		return con;
	}
	
	/**
	 * Führt ein beliebiges SQL-Statement aus.
	 * @param statement
	 * @throws ConnectorException
	 */
	public void execute(String statement) throws ConnectorException {
		try {
			Statement st = con.createStatement();
			st.execute(statement);
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new ConnectorException("SQL-Fehler: "+e.getMessage());
		}
	}
	
	/**
	 * Macht einen neuen Eintrag und gibt die eingetragene ID zurück.
	 * @param statement
	 * @return
	 * @throws ConnectorException
	 */
	public int executeGetId(String statement) throws ConnectorException {
		try {
			Statement st = con.createStatement();
			st.execute(statement);
			ResultSet result = st.executeQuery("SELECT last_insert_rowid()");
			result.next();
			return result.getInt(1);
		} catch (SQLException e) {
			throw new ConnectorException("SQL-Fehler: "+e.getMessage());
		}
	}
	
	/**
	 * Führt ein beliebiges SQL-Statement aus und gibt
	 * ein Resultset zurück.
	 * @param statement
	 * @return
	 * @throws ConnectorException
	 */
	public ResultSet getResult(String statement) throws ConnectorException {
		try {
			Statement st = con.createStatement();
			return st.executeQuery(statement);
		} catch (SQLException e) {
			throw new ConnectorException("SQL-Fehler: "+e.getMessage());
		}
	}
	
}
