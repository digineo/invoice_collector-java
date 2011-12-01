package de.digineo.invoicecollector.sdk.objects;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.crawler.Crawler;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class Invoice extends DataObject {
	
	int accountId = 0;
	String number;
	Double amount = null;
	Date date;
	File originalFile;
	Account account;
	String checksum;
	private boolean imap = false;
	String fileName;
	
	public Invoice() { }
	
	public Invoice(Account account, String number, Double amount, Date date, 
			File originalFile, String checksum) {
		super();
		this.number = number;
		this.amount = amount;
		this.date = date;
		this.originalFile = originalFile;
		if(account != null) this.accountId = account.getId();
		this.account = account;
		this.checksum = checksum;
	}

	public void extractFromResult(ResultSet result) throws ConnectorException {
		try {
			id = result.getInt("id");
			accountId = result.getInt("account_id");
			number = result.getString("number");
			checksum = result.getString("checksum");
			if(result.getString("amount") != null) amount = result.getDouble("amount");
			try {
				date = dateFormat.parse(result.getString("date"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			account = new Account(result.getString("username"),"",result.getString("module"));
			account.setId(accountId);
			
			originalFile = new File(API.INSTANCE.getFolder()+account.getModule()+File.separator+DataObject.dateFormatYear.format(date)+File.separator+result.getString("original_file_name"));
			if(!originalFile.exists()) {
				originalFile = new File(API.INSTANCE.getFolder()+"Imap"+File.separator+DataObject.dateFormatYear.format(date)+File.separator+result.getString("original_file_name"));
				if(originalFile.exists()) imap = true;
			}
			
		} catch (SQLException e) {
			throw new ConnectorException("Probleme beim Lesen des Results: "+e.getMessage());
		}
	}
	
	public String getNumber() {
		return number;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Double getAmount() {
		return amount;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public File getOriginalFile() {
		return originalFile;
	}
	
	public int getAccountId() {
		return accountId;
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public boolean isImap() {
		return imap;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	/**
	 * Öffnet das Rechnungspdf und zeigt es an.
	 */
	public void openFile() {
		if(!getOriginalFile().exists()) return;
		Desktop d = Desktop.getDesktop();
		try {
			d.open(getOriginalFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert eine Rechnung anhand eines preparedStatement.
	 * @see Crawler.getInvoices
	 * @param prep PreparedStatement, das erweitert werden muss.
	 * @throws SQLException
	 */
	public void create(PreparedStatement prep) throws SQLException {
		
		prep.setInt(1, accountId);
		prep.setString(2, number);
		prep.setString(3, dateFormat.format(date));
		if(amount == null)  prep.setNull(4, java.sql.Types.DOUBLE);
		else prep.setDouble(4, amount);
		prep.setString(5, originalFile.getName());
		prep.setString(6, checksum);
		prep.addBatch();
		
	}
	
	/**
	 * Löscht das Rechnungs-Pdf und den Datenbank-Eintrag der Rechnung.
	 * @throws ConnectorException
	 */
	public void remove() throws ConnectorException {
		API.INSTANCE.getConnector().execute("DELETE FROM invoices WHERE id = '"+id+"'");
		if(originalFile.exists()) originalFile.delete();
	}
	
}
