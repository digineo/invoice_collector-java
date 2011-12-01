package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Binlayer extends Crawler {
	
	public Binlayer(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://binlayer.com/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("username").setValueAttribute(username);
		form.getInputByName("passwort").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(currentPage.getUrl().toString().contains("/login.html")) throw new LoginException();
		if(!currentPage.getUrl().toString().contains("binlayer.com")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/konto-auszahlungen.html");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() == 0) continue;
				
				for(HtmlElement link : links) {
					if(link.getAttribute("href").contains("pdf")) pdfLink = (HtmlAnchor) link;
				}
				
				if(pdfLink != null) {
					invoice = createInvoice(cells.get(0).asText(), cells.get(1).asText(), cells.get(2).asText(), pdfLink);
					if(invoice != null) invoices.add(invoice);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new CrawlerException(e.getMessage(),e);
		}
		
		return invoices;
	}
	
	public void logout() throws LogoutException {
		try {
			getPage("https://binlayer.com/logout.html");
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
			throw new LogoutException(e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new LogoutException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new LogoutException(e.getMessage());
		}
	}
	
}
