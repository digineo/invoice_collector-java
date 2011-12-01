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

public class Linklift extends Crawler {
	
	public Linklift(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.linklift.de/einloggen/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("LL_email").setValueAttribute(username);
		form.getInputByName("LL_password").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		DomNodeList<HtmlElement> headers = currentPage.getElementsByTagName("h1");
		if(headers.size() > 0 && headers.get(0).asText().equals("Einloggen")) throw new LoginException();
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/mein-konto/rechnungen/?t=adv");
			DomNodeList<HtmlElement> headers = currentPage.getElementsByTagName("h3");
			if(headers.size() > 0 && !headers.get(0).asText().equals("Meine Rechnungen")) throw new CrawlerException("Ungültige Seite"); 
			
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() < 5) continue;
				if(links.size() < 1) continue;
				
				for(HtmlElement link : links) {
					if(link.getAttribute("href").contains("pdf")) pdfLink = (HtmlAnchor) link;
				}
				
				if(pdfLink != null) {
					invoice = createInvoice(cells.get(2).asText(), cells.get(1).asText(), cells.get(4).asText(), pdfLink);
					if(invoice != null) {
						if(cells.get(0).equals("Gutschrift")) invoice.setAmount((-1) * invoice.getAmount());
						invoices.add(invoice);
					}
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
			getPage("/ausloggen/");
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
