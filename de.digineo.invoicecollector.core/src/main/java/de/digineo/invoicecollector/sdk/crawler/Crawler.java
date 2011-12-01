package de.digineo.invoicecollector.sdk.crawler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.DataObject;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.sdk.objects.Invoice;


public abstract class Crawler {
	
	public static String PACKAGE_ID = "de.digineo.invoicecollector.sdk.crawler";
	protected String username;
	protected String password;
	protected Account account;
	protected WebClient webClient;
	
	/** Die zuletzt abgerufene Seite */
	protected HtmlPage currentPage;
	
	/**
	 * Abstrakte Login-Methode.
	 * Gibt wahr zuück, falls der Login erfolgreich war.
	 * @return
	 */
	public abstract void login() throws LoginException, IOException;
	/**
	 * Loggt den User wieder aus.
	 * @return
	 */
	public abstract void logout() throws LogoutException, IOException;
	/**
	 * Crawlt von dem Dienst alle Rechnungen und
	 * gibt sie als ArrayList zurück.
	 * @return
	 */
	public abstract ArrayList<Invoice> crawl() throws CrawlerException;
	
	/**
	 * Initialisiert den Crawler
	 * @param username
	 * @param password
	 * @param account
	 */
	public Crawler(String username, String password, Account account) {
		this.username = username;
		this.password = password;
		this.account = account;
		this.webClient = new WebClient();
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF); 
		webClient.setJavaScriptEnabled(true);
		webClient.setThrowExceptionOnScriptError(false);
	}
	
	public Crawler(ImapAccount imapAccount, ImapFilter imapFilter) { }
	
	/**
	 * Lädt alle neuen Rechnungen eines Accounts.
	 * @throws LoginException
	 * @throws IOException 
	 * @throws CrawlerException 
	 * @throws LogoutException
	 */
	public void getInvoices() throws LoginException, IOException, CrawlerException {
		
		try {
			
			this.login();
			
			ArrayList<Invoice> invoices = this.crawl();
			ArrayList<String> checksums = new ArrayList<String>();
			
			
			PreparedStatement prep = API.INSTANCE.getConnector().getCon().prepareStatement("INSERT INTO " +
					"invoices (account_id, number, date, amount, original_file_name, checksum) " +
					"VALUES (?, ?, ?, ?, ?, ?);");
			
			for(Invoice invoice : invoices) {
				if(checksums.contains(invoice.getChecksum())) continue;
				invoice.create(prep);
				checksums.add(invoice.getChecksum());
			}
			
			prep.executeBatch();

			API.INSTANCE.getConnector().getCon().close();
			API.INSTANCE.getConnector().createConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				this.logout();
			} catch (LogoutException e) {
				e.printStackTrace();
			} finally {
				if(webClient != null && webClient.getWebWindows() != null && webClient.getWebWindows().size() > 0) webClient.closeAllWindows();
			}
			
		}
	}
	
	/**
	 * Crawlt Rechnungen, ohne sich ein- und auszuloggen.
	 * @throws CrawlerException
	 * @throws SQLException
	 */
	public void getInvoicesImap() throws CrawlerException, SQLException {
		ArrayList<Invoice> invoices = this.crawl();
		ArrayList<String> checksums = new ArrayList<String>();
		PreparedStatement prep = API.INSTANCE.getConnector().getCon().prepareStatement("INSERT INTO " +
				"invoices (account_id, number, date, amount, original_file_name, checksum) " +
				"VALUES (?, ?, ?, ?, ?, ?);");
		
		for(Invoice invoice : invoices) {
			if(checksums.contains(invoice.getChecksum())) continue;
			invoice.create(prep);
			checksums.add(invoice.getChecksum());
		}
		
		prep.executeBatch();

		API.INSTANCE.getConnector().getCon().close();
		API.INSTANCE.getConnector().createConnection();
	}
	
	/**
	 * Extrahiert aus der URL der aktuellen Seite ein
	 * Get-Parameter
	 * @param param
	 * @return Wert des Get-Parameters
	 */
	protected String extractParam(String param) {
		 String value = currentPage.getUrl().getQuery();
		 value = value.substring(value.indexOf(param+"=") + (param+"=").length());
		 int stop = value.indexOf("&");
		 if(stop >= 0) value = value.substring(0, stop);
		 return value;
	}
	 
	 /**
	  * Erstellt eine Invoice-Instanz und kopiert die Rechnung,
	  * sofern die Rechnung noch nicht existiert.
	  * @param number die Rechnungsnummer
	  * @param date Das Datum der Rechnung als String
	  * @param amount Der Rechnungsbetrag als String
	  * @param pdfLink Der Link zu dem RechnungsPdf
	  * @return
	  */
	 protected Invoice createInvoice(String number, String date, String amount, HtmlAnchor pdfLink) {
		try {
			return createInvoice(number, date, amount, getFile(pdfLink,number,DataObject.dateFormat2.parse(date)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return null;
	 }
	 

	 /**
	  * Erstellt eine Invoice-Instanz und kopiert die Rechnung,
	  * sofern die Rechnung noch nicht existiert.
	  * @param number die Rechnungsnummer
	  * @param date Das Datum der Rechnung als String
	  * @param amount Der Rechnungsbetrag als String
	  * @param pdfLink Der Link zu dem RechnungsPdf
	  * @return
	  */
	 protected Invoice createInvoice(String number, String date, String amount, URL pdfLink) {
		try {
			UnexpectedPage page = webClient.getPage(pdfLink);
			WebResponse response = page.getWebResponse();
			return createInvoice(number, date, amount, getFile(response.getContentAsStream(),number,DataObject.dateFormat2.parse(date)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	 }
	 
	 /**
	  * Erstellt eine Rechnung anhand eines InputStreams.
	  * @param number
	  * @param date
	  * @param amount
	  * @param is
	  * @return
	  */
	 protected Invoice createInvoice(String number, String date, String amount, InputStream is) {
		try {
			return createInvoice(number, date, amount, getFile(is,number,DataObject.dateFormat2.parse(date)));
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} 
		return null;
	 }
	 
	 /**
	  * Erstellt anhand von Daten eine Rechnung, sofern diese noch nicht existiert.
	  * @param number Nummer der Rechnung
	  * @param date Datum der Rechnung
	  * @param amount Rechnungsbetrag
	  * @param pdfFile Pdf-Datei
	  * @return
	  */
	 protected Invoice createInvoice(String number, String date, String amount, File pdfFile) {
		 try {
			 // Prüfen ob Rechnung bereits existiert.
			if(account != null && API.INSTANCE.existInvoice(account, number)) return null;
			
			// Nur erstellen wenn Pdf tatsächlich neu
			if(account != null && pdfFile == null) return null;
			
			if(account != null) return new Invoice(account, number, (amount == null ? null : extractAmount(amount)), DataObject.dateFormat2.parse(date), 
					pdfFile, calculateHash(pdfFile));
			else return new Invoice(account, number, (amount == null ? null : extractAmount(amount)), DataObject.dateFormat2.parse(date), 
					null, "");
		
		 } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	 }
	 
	 /**
	  * Kopiert anhand eines Links eine Datei in den Rechnungsordner
	  * der Form modul/jahr/rechnung.pdf
	  * @param link
	  * @return
	  */
	 private File getFile(HtmlAnchor link, String number, Date date) {
		WebResponse response;
		try {
			response = link.click().getWebResponse();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return getFile(response.getContentAsStream(),number,date);
	 }
	 
	 /**
	  * Erstellt aus einem Input-Stream ein RechnungsPdf,
	  * welches in den Ordner Modul/Jahr/meinpdf.pdf gespeichert wird.
	  * @param is InputStream
	  * @param number Rechnungsnr.
	  * @param date Datumd der Rechnung
	  * @return
	  */
	 private File getFile(InputStream is, String number, Date date) {
		 try {
			 if(account == null) return null;
			 String origName = account.getUsername()+"_"+number+".pdf";
			 
			 File moduleDir = this.createModuleDir();
			 File yearDir = this.createYearDir(moduleDir,date);
			 
			 File file = new File(API.INSTANCE.getFolder()+moduleDir.getName()+File.separator+yearDir.getName()+File.separator+origName);
			 
			 if(file.exists()) return null;
			 
			 OutputStream out = new FileOutputStream(file);
			 byte buf[] = new byte[1024];
			 int len;
			 while((len=is.read(buf))>0) {
				 out.write(buf,0,len);
			 }
			 out.close();
			 is.close();
			 return file;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	 }
	 
	 /**
	  * Extrahiert den Rechnungsbetrag und gibt ihn als Double zurück.
	  * @param value
	  * @return
	  */
	 protected Double extractAmount(String value) {
		 Matcher matcher = Pattern.compile("[\\d\\.,]*\\d+[\\.,]\\d+").matcher(value);
		 while(matcher.find()) {
			 value = matcher.group();
			 break;
		 }
		 return Double.parseDouble(value.replaceAll("[\\.,]", ""))/100;
	 }
	 
	 /**
	  * Gibt das erste Formular auf einer Page zurück.
	  * @param page
	  * @return
	  */
	 protected HtmlForm getFirstForm(HtmlPage page) {
		 return page.getForms().get(0);
	 }
	 
	 /**
	  * Gibt das erste Formular aus der aktuellen Page zurück.
	  * @param page
	  * @return
	  */
	 protected HtmlForm getFirstForm() {
		 return currentPage.getForms().get(0);
	 }
	 
	 /**
	  * Gibt den ersten Submit-Button eines Formulares zurück.
	  * @param form
	  * @return
	  */
	 protected HtmlElement getFirstButton(HtmlForm form) {
		 List<HtmlInput> submits = form.getElementsByAttribute("input", "type", "submit");
		 if(submits.size() > 0) return submits.get(0);
		 List<HtmlButton> buttons = form.getElementsByAttribute("button", "type", "submit");
		 if(buttons.size() > 0) return buttons.get(0);
		 return null;
	 }
	 
	 /**
	 * Erstellt ggf. den Ordner für das jeweilige Jahr.
	 * @param moduleDir der Pfad zum Modul
	 * @return Der Ordner zum aktuellen Jahr
	 */
	private File createYearDir(File moduleDir, Date date) {
		File yearDir = new File(API.INSTANCE.getFolder()+moduleDir.getName()+File.separator+DataObject.dateFormatYear.format(date));
		if(!yearDir.exists()) yearDir.mkdir();
		return yearDir;
	}
	
	/**
	 * Erstellt ein Ordner für das aktuelle Modul,
	 * falls dieser noch nicht existiert.
	 * @return
	 */
	private File createModuleDir() {
		File dir;
		if(account.getImapAccount() == null) dir = new File(API.INSTANCE.getFolder()+account.getModule());
		else dir = new File(API.INSTANCE.getFolder()+"Imap");
		if(!dir.exists()) dir.mkdir();
		return dir;
	}
	
	/**
	 * Lädt die aktuelle Seite.
	 * @param url
	 * @throws IOException
	 */
	protected void getPage(String url) throws IOException {
		if(url.startsWith("http")) {
			currentPage = webClient.getPage(url);
			return;
		} 
		
		String[] pieces = currentPage.getUrl().toString().split("/");
		String currentUrl = "";
		for(int i = 0;i < 3; i++) {
			 currentUrl += pieces[i]+"/";
		}
		if(url.startsWith("/")) currentUrl += url.substring(1);
		else currentUrl += url;

		currentPage = webClient.getPage(currentUrl);
	}
	
	/**
	 * Gibt die SHA1-Checksumme von einer angegebenen
	 * Datei zurück.
	 * @param file
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private String calculateHash(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest algorithm = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DigestInputStream dis = new DigestInputStream(bis, algorithm);
		
		while (dis.read() != -1);

		byte[] hash = algorithm.digest();
		return byteArray2Hex(hash);
	}
	
	/**
	 * Erzeugt aus Bytes Hex-String
	 * @param hash
	 * @return
	 */
	private String byteArray2Hex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
	
	/**
	 * Parsiert ein Datum, das ein beliebiges Format hat in das
	 * dd.MM.yyyy Format
	 * @param date Datum
	 * @param dateFormatString Formatstring
	 * @return
	 * @throws ParseException
	 */
	protected String parseDate(String date, String dateFormatString) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
		return DataObject.dateFormat2.format(dateFormat.parse(date));
	}

	
}
