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

public class Affilinet extends Crawler {
	
	public Affilinet(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("http://www.affili.net/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("ctl00$ctl03$txtLogin").setValueAttribute(username);
		form.getInputByName("ctl00$ctl03$txtPassword").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(!currentPage.getUrl().toString().endsWith("/Start/default.aspx")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/Account/payments.aspx");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() < 7) continue;
				
				for(HtmlElement link : links) {
					if(link.asText().contains("PDF")) pdfLink = (HtmlAnchor) link;
				}
				
				if(pdfLink != null) {
					invoice = createInvoice(cells.get(2).asText(), cells.get(0).asText(), cells.get(7).asText(), pdfLink);
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
			getPage("/Login/Logout.aspx");
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
