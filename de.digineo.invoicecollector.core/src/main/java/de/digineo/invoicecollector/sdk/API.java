package de.digineo.invoicecollector.sdk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.digineo.invoicecollector.sdk.crawler.Crawler;
import de.digineo.invoicecollector.sdk.crawler.CrawlerException;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.sdk.objects.Invoice;
import de.digineo.invoicecollector.sdk.persistence.Connector;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class API {
	
	/** Datenbankzugriff */
	private Connector connector;
	
	/** Die Singleton-Instanz der API */
	public final static API INSTANCE = new API();	
	
	/** Speicherort für alle Rechnungen */
	private String folder;
	
	/** Pfad zu dem files-Verzeichnis */
	private String filePath;
	
	/**
	 * Zuletzt aktualisierte Rechnungen, um SQL
	 * Out Of Memory zu verhindern
	 */
	private HashMap<String,Invoice> currentInvoices = new HashMap<String,Invoice>();
	
	/**
	 * Initialisiert die API 
	 */
	private API() { }
	
	/**
	 * Initialisiert den Connector
	 * @throws ConnectorException
	 */
	public void connect() throws ConnectorException {
		connector = new Connector();
	}
	
	/**
	 * Setzt den Pfad für Rechnungen
	 * @param folder
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	/**
	 * Gibt den Pfad zu den Rechnungen zurück.
	 * @return
	 */
	public String getFolder() {
		return folder;
	}
	
	/**
	 * Setzt den Ordner zum File-Verzeichnis
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Gibt den Pfad zum File-Verzeichnis zurück
	 * @return
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Gibt den Connector zurück.
	 * @return
	 */
	public Connector getConnector() {
		return connector;
	}
	
	/**
	 * Lädt alle Crawler-Klassen, die in dem Dokument files/crawlers.xml aufgelistet
	 * sind.
	 * @return
	 * @throws CrawlerException
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<Class> getModules() throws CrawlerException {
		ArrayList<Class> classes = new ArrayList<Class>();
		
		DocumentBuilder db;
		NodeList nodes;
		Class cl;
		
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDom = db.parse(filePath+"crawlers.xml");
			XPath xpath = XPathFactory.newInstance().newXPath();
			nodes = (NodeList) xpath.evaluate("//crawlers/*", xmlDom, XPathConstants.NODESET);
		} catch (Exception e1) {
			throw new CrawlerException("Fehler beim XML-Parsen: "+e1.getMessage());
		}
		
		for (int i = 0; i < nodes.getLength(); i++) {
			try {
				cl = Class.forName(Crawler.PACKAGE_ID+"."+nodes.item(i).getTextContent());
				classes.add(cl);
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return classes;
	}
	
	/**
	 * Erstellt einen einzelnen Account anhand seines Benutzernamen, seines Passwort
	 * und einem ausgewählten Modul. 
	 * @param username
	 * @param password
	 * @param module
	 * @throws ConnectorException
	 * @throws CrawlerException
	 */
	public Account createAccount(String username, String password, String module) throws ConnectorException {
		Account account = new Account(username, password, module);
		account.create();
		return account;
	}
	
	public Account createAccount(String username, ImapAccount imapAccount, ImapFilter imapFilter) throws ConnectorException {
		Account account = new Account(username, imapAccount, imapFilter);
		account.create();
		return account;
	}
	
	/**
	 * Erstellt einen ImapAccount und speichert ihn in der Datenbank.
	 * @param host
	 * @param username
	 * @param password
	 * @param port
	 * @param ssl
	 * @return
	 * @throws ConnectorException
	 */
	public ImapAccount createImapAccount(String host, String username, String password, 
			String port, boolean ssl) throws ConnectorException {
		ImapAccount account = new ImapAccount(host, username, password, Integer.parseInt(port), ssl);
		account.create();
		return account;
	}
	
	/**
	 * Erstellt einen ImapFilter.
	 * @param name
	 * @param subject
	 * @param fileName
	 * @param search
	 * @return
	 * @throws ConnectorException
	 */
	public ImapFilter createImapFilter(String name, String subject, String fileName,
			String search) throws ConnectorException {
		ImapFilter filter = new ImapFilter(name, subject, fileName, search);
		filter.create();
		return filter;
	}
	
	/**
	 * Lädt alle Accounts aus der Datenbank und gibt sie als ArrayList zurück
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Account> getAccounts() throws ApiException {
		ArrayList<Account> accounts = new ArrayList<Account>();
		
		try {
			
			Account acc;
			ResultSet result = connector.getResult("SELECT accounts.*, " +
					"imap_accounts.host AS imap_account_host, " +
					"imap_accounts.username AS imap_account_username, imap_accounts.password AS imap_account_password, " +
					"imap_accounts.ssl AS imap_account_ssl, imap_accounts.port AS imap_account_port, " +
					"imap_filters.subject AS imap_filter_subject, " +
					"imap_filters.search AS imap_filter_search, imap_filters.filename AS imap_filter_filename, " +
					"imap_filters.name AS imap_filter_name " +
					"FROM accounts LEFT JOIN " +
					"imap_accounts ON imap_accounts.id = accounts.imap_account_id LEFT JOIN  " +
					"imap_filters ON imap_filters.id = accounts.imap_filter_id " +
					"ORDER BY module, username");
			while(result.next()) {
				acc = new Account();
				acc.extractFromResult(result);
				accounts.add(acc);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return accounts;
	}
	
	/**
	 * Lädt alle ImapAccounts aus der Datenbank und gibt sie als ArrayList zurück
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<ImapAccount> getImapAccounts() throws ApiException {
		ArrayList<ImapAccount> accounts = new ArrayList<ImapAccount>();
		
		try {
			
			ImapAccount acc;
			ResultSet result = connector.getResult("SELECT * FROM imap_accounts ORDER BY host, username");
			while(result.next()) {
				acc = new ImapAccount();
				acc.extractFromResult(result);
				accounts.add(acc);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return accounts;
	}
	
	/**
	 * Gibt alle Imap-Filter aus der Datenbank zurück.
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<ImapFilter> getImapFilter() throws ApiException {
		ArrayList<ImapFilter> filter = new ArrayList<ImapFilter>();
		
		try {
			
			ImapFilter f;
			ResultSet result = connector.getResult("SELECT * FROM imap_filters ORDER BY name");
			while(result.next()) {
				f = new ImapFilter();
				f.extractFromResult(result);
				filter.add(f);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return filter;
	}
	
	/**
	 * Lädt alle eingetragene Rechnungen in eine ArrayList.
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoices() throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		try {
			Invoice invoice;
			ResultSet result = connector.getResult("SELECT invoices.*, accounts.module, accounts.username " +
					"FROM invoices LEFT JOIN accounts ON accounts.id = invoices.account_id ORDER BY date DESC, number DESC");
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				currentInvoices.put(invoice.getAccountId()+"_"+invoice.getNumber(), invoice);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return invoices;
	}
	
	/**
	 * Lädt alle eingetragene Rechnungen in eine ArrayList, die zwischen
	 * einem Start- und einem Enddatum liegen.
	 * @param start Startdatum
	 * @param end Enddatum
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoices(String start, String end) throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			Invoice invoice;
			ResultSet result = connector.getResult("SELECT invoices.*, accounts.module, " +
					"accounts.username FROM invoices " +
					"LEFT JOIN accounts ON accounts.id = invoices.account_id WHERE " +
					"invoices.date >= '"+start+"' AND " +
							"invoices.date <= '"+end+"' " +
									"ORDER BY date DESC, number DESC");
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		
		return invoices;
	}
	
	/**
	 * Lädt anhand von einem Account, Start- und Enddatum Rechnungen.
	 * @param module Dienst
	 * @param username Account-Name
	 * @param start Startdatum
	 * @param end Enddatum
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoices(String module, String username, String start, String end) throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			Invoice invoice;
			ResultSet result;
			if(module.equals("Imap")) {
				result = connector.getResult("SELECT invoices.*, module, username " +
						"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
						"WHERE accounts.imap_account_id > 0 AND username = '"+username+"' AND" +
								" date(date) > date('"+start+"') AND " +
							"date(date) < date('"+end+"') " +
								"ORDER BY date DESC, number DESC");
			} else {
				result = connector.getResult("SELECT invoices.*, module, username " +
						"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
						"WHERE module = '"+module+"' AND username = '"+username+"' AND" +
								" date(date) > date('"+start+"') AND " +
							"date(date) < date('"+end+"') " +
								"ORDER BY date DESC, number DESC");
			}
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return invoices;
	}
	
	/**
	 * Lädt Rechnungen anhand eines Accounts, Start- und Enddatum und einem
	 * Suchinfix für die Rechnungsnummer.
	 * @param module Dienst
	 * @param username Benutzername
	 * @param start Startdatum
	 * @param end Enddatum
	 * @param search Such-Infix
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoices(String module, String username, String start, 
			String end, String search) throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			Invoice invoice;
			ResultSet result;
			if(module.equals("Imap")) {
				result = connector.getResult("SELECT invoices.*, module, username " +
						"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
						"WHERE accounts.imap_account_id > 0 AND username = '"+username+"' AND" +
								" date(date) > date('"+start+"') AND " +
							"date(date) < date('"+end+"') AND " +
									"number LIKE '%"+search+"%' " +
								"ORDER BY date DESC, number DESC");
			} else {
				result = connector.getResult("SELECT invoices.*, module, username " +
						"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
						"WHERE module = '"+module+"' AND username = '"+username+"' AND" +
								" date(date) > date('"+start+"') AND " +
							"date(date) < date('"+end+"') AND " +
									"number LIKE '%"+search+"%' " +
								"ORDER BY date DESC, number DESC");
			}
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return invoices;
	}

	/**
	 * Sucht alle Rechnungen anhand eines Start- und eines Enddatums heraus und
	 * sucht dabei nach einem Rechnungsnr.-Infix.
	 * @param start Startdatum
	 * @param end Enddatum
	 * @param search Suchstring
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoices(String start, String end, String search) throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			Invoice invoice;
			ResultSet result = connector.getResult("SELECT invoices.*, module, username " +
					"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
					"WHERE " +
							" date(date) > date('"+start+"') AND " +
						"date(date) < date('"+end+"') AND " +
								"number LIKE '%"+search+"%' " +
							"ORDER BY date DESC, number DESC");
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return invoices;
	}
	
	/**
	 * Gibt alle Rechnungen zu einem bestimmten Account
	 * zurück.
	 * @param module Dienst
	 * @param username Benutzername.
	 * @return
	 * @throws ApiException
	 */
	public ArrayList<Invoice> getInvoicesByAccount(String module, String username) throws ApiException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			Invoice invoice;
			ResultSet result;
			if(module.equals("Imap")) {
				result = connector.getResult("SELECT invoices.*, module, username " +
						"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
						"WHERE accounts.imap_account_id > 0 AND username = '"+username+"' " +
								"ORDER BY date DESC, number DESC");
			} else {
				result = connector.getResult("SELECT invoices.*, module, username " +
					"FROM invoices LEFT OUTER JOIN accounts ON accounts.id = invoices.account_id " +
					"WHERE module = '"+module+"' AND username = '"+username+"' " +
							"ORDER BY date DESC, number DESC");
			}
			while(result.next()) {
				invoice = new Invoice();
				invoice.extractFromResult(result);
				invoices.add(invoice);
			}
			
		} catch (ConnectorException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		} catch (SQLException e) {
			throw new ApiException("Fehler beim Abfragen der Datenbank: "+e.getMessage());
		}
		
		return invoices;
	}
	
	/**
	 * Prüft, ob eine Rechnung bereits existiert anhand
	 * der zwischengespeicherten Rechnungen
	 * @see getInvoices
	 * @param account Account
	 * @param number Eindeutige Nummer der Rechnung
	 * @return
	 */
	public boolean existInvoice(Account account, String number) {
		return currentInvoices.containsKey(account.getId()+"_"+number);
	}
	
	/**
	 * Wandelt ein Double in einen Euro-Betrag um.
	 * @param number Der Betrag
	 * @return
	 */
	public static String getEuro(Double number) {
		String amount = number.toString();
		String[] pieces = amount.split("\\.");
		if(pieces[1].length() == 1) {
			int cent = Integer.parseInt(pieces[1]);
			if(cent == 0) pieces[1] = "00";
			else pieces[1] = ""+(cent*10);
		}
		return pieces[0]+","+pieces[1]+" $"; // TODO replace it with " €"
	}
	
}
