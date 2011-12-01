package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Hosteurope extends Crawler {
	
	public Hosteurope(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://kis.hosteurope.de/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("kdnummer").setValueAttribute(username);
		form.getInputByName("passwd").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(!currentPage.getUrl().toString().endsWith("/index.php")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/kundenkonto/rechnungen/");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			URL pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				
				if(cells.size() < 7) continue;
				
				ArrayList<HtmlElement> inputs = (ArrayList<HtmlElement>) cells.get(0).getElementsByAttribute("input", "name", "belegnr");
				if(inputs.size() < 1) continue;
				
				String number = inputs.get(0).getAttribute("value");
				
				pdfLink = new URL("https://kis.hosteurope.de/kundenkonto/rechnungen/index.php?inline=yes&belegnr="+number);
				
				if(pdfLink != null) {
					invoice = createInvoice(number, cells.get(3).asText(), cells.get(6).asText(), pdfLink);
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
			getPage("/?logout=1");
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
