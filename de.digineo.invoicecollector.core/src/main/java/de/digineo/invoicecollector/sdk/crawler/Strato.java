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

public class Strato extends Crawler {
	
	private String sessionId;
	
	public Strato(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.strato.de/apps/CustomerService");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("identifier").setValueAttribute(username);
		form.getInputByName("passwd").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(!currentPage.getUrl().toString().endsWith("node=kds_CustomerEntryPage")) throw new LoginException();
		
		sessionId = extractParam("sessionID");
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/apps/CustomerService?sessionID="+sessionId+"&node=OnlineInvoice&source=menu");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() != 6) continue;
				
				for(HtmlElement link : links) {
					if(link.getAttribute("title").contains("pdf")) pdfLink = (HtmlAnchor) link;
				}
				
				if(pdfLink != null) {
					invoice = createInvoice(links.get(0).asText(), cells.get(0).asText(), cells.get(3).asText(), pdfLink);
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
			getPage("/apps/CustomerService?sessionID="+this.sessionId+"&node=kds_Logout");
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
