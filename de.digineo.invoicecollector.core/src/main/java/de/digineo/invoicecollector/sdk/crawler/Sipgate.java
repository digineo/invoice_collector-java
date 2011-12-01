package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Sipgate extends Crawler {
	
	public Sipgate(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://secure.sipgate.de/user/login.php");
		
		HtmlForm form = (HtmlForm) currentPage.getElementsByName("login_simple").get(0);
		
		form.getInputByName("uname").setValueAttribute(username);
		form.getInputByName("passw").setValueAttribute(password);
		
		currentPage = form.getElementsByTagName("button").get(0).click();
		
		if(!currentPage.getUrl().toString().endsWith("/start.php")) throw new LoginException();

	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/user/invoice.php");
			DomNodeList<HtmlElement> links = currentPage.getElementsByTagName("a");
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement link : links) {
				
				pdfLink = null;
				
				if(link.asText().contains("Rechnung vom")) pdfLink = (HtmlAnchor) link;
				
				if(pdfLink != null) {
					
					invoice = createInvoice(pdfLink.getAttribute("href").substring(pdfLink.getAttribute("href").indexOf("nr=")+3), pdfLink.asText().substring(pdfLink.asText().indexOf("vom ")+4), null, pdfLink);
					if(invoice != null) invoices.add(invoice);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new CrawlerException(e.getMessage(),e);
		}
		
		return invoices;
	}
	
	public void logout() throws LogoutException { }
	
}
