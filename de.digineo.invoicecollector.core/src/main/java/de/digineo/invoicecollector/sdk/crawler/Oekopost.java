package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.DataObject;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Oekopost extends Crawler {
	
	public Oekopost(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.oekopost.de/login/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("username").setValueAttribute(username);
		form.getInputByName("password").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(currentPage.getElementById("userInfo") == null) throw new LoginException(); 
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/user/account/invoices/");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			String date;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				if(cells.size() < 2) continue;
				
				links = cells.get(0).getElementsByTagName("a");
				
				if(links.size() == 0) continue;
				
				for(HtmlElement link : links) {
					if(link.getAttribute("href").contains(".pdf")) pdfLink = (HtmlAnchor) link;
				}
				
				if(pdfLink != null) {
					date = cells.get(1).asText().split(" ")[0];
					if(date.contains("Heute")) date = DataObject.dateFormat2.format(new Date());
					if(pdfLink.asText().equals("")) continue;
					invoice = createInvoice(pdfLink.asText(), date, null, pdfLink);
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
			getPage("/user/logout/");
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
