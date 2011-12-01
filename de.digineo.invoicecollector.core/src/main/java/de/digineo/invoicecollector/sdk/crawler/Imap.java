package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.DataObject;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Imap extends Crawler {
	
	private ImapAccount imapAccount;
	private ImapFilter imapFilter;
	private Folder inbox;
	private Store store;
	
	public Imap(Account account, ImapAccount imapAccount, ImapFilter imapFilter, Store store) {
		super(imapAccount, imapFilter);
		this.imapAccount = imapAccount;
		this.imapFilter = imapFilter;
		this.account = account;
		this.store = store;
	}

	public void login() throws LoginException, IOException {
		try {
			Properties props = System.getProperties();
			Session session = Session.getInstance(props);
			store = session.getStore("imaps");
			store.connect(imapAccount.getHost(), imapAccount.getUsername(), imapAccount.getPassword());
			
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new LoginException();
		}
	}
	
	public Store getStore() {
		return store;
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		Folder inbox;
		Invoice invoice;
		Multipart mp;
		Part part;
		String disposition;
		InputStream pdfFile;
		String pdfFileName;
		String number;
		String date;
		Matcher matcher;
		try {
			inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			Message[] messages = inbox.search(getSearchTerm());
			
			for(int i = 0; i < messages.length; i++) {
				
				pdfFileName = "";
				pdfFile = null;
				
				mp = (Multipart) messages[i].getContent();
				
				for (int j=0; j<mp.getCount(); j++) {
					part = mp.getBodyPart(j);
					disposition = part.getDisposition();
					if(disposition == null) continue;
					
					if(disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE)) {
						pdfFileName = part.getFileName();
						pdfFile = part.getInputStream();
						break;
					} 

				}

				if(pdfFile == null || pdfFileName == null) continue;
				
				
				if(!imapFilter.getSubject().equals("")) {
					matcher = Pattern.compile(imapFilter.getSubject()).matcher(messages[i].getSubject());
					if(!matcher.find()) continue;
				}
				
				number = ""; 
				matcher = Pattern.compile(imapFilter.getFileName()).matcher(pdfFileName);
				if(matcher.find() && matcher.group(1) != null) number = matcher.group(1);
				
				date = DataObject.dateFormat2.format(messages[i].getReceivedDate());
				matcher = Pattern.compile("(\\d\\d)\\.(\\d{4})\\.(\\d\\d)").matcher(messages[i].getSubject());
				if(matcher.find()) date = matcher.group();
				
				invoice = createInvoice(number, date, null, pdfFile);
				if(invoice != null) {
					invoice.setFileName(pdfFileName);
					invoices.add(invoice);
				}
			}
			
			
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new CrawlerException("Fehler beim Laden.",e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CrawlerException("Fehler beim Laden.",e);
		}
		return invoices;
	}
	
	private SearchTerm getSearchTerm() {
		String[] lines = imapFilter.getSearch().split("\n");
		SearchTerm subject = new SubjectTerm("");
		SearchTerm from = new FromStringTerm("");
		
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].startsWith("FROM")) {
				from = new FromStringTerm(lines[i].substring(5));
			} else if(lines[i].startsWith("SUBJECT")) {
				subject = new SubjectTerm(lines[i].substring(8));
			}
		}
		
		return new AndTerm(subject, from);
	}
	
	public void logout() throws LogoutException {
		try {
			if(inbox != null) inbox.close(false);
			if(store != null) store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new LogoutException(e.getMessage());
		}

	}
	
}
