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

public class Simplytel extends Crawler {
	
	public Simplytel(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.simplytel.de/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("credential_0").setValueAttribute(username);
		form.getInputByName("credential_1").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(!currentPage.getUrl().toString().endsWith("/index3.php")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/rechnungonline.php?action=rechnung24&unteraction=uebersicht&sehen=Alle+Rechnungen+anzeigen");
			
			DomNodeList<HtmlElement> selectList = currentPage.getElementsByTagName("select");
			HtmlElement select = null;
			
			for(HtmlElement elem : selectList) {
				if(elem.getAttribute("name") == "datum") {
					select = elem;
					break;
				}
			}
			
			if(select == null) throw new CrawlerException("Keine Rechnungen gefunden");
			
			DomNodeList<HtmlElement> optionList = select.getElementsByTagName("option");

			Invoice invoice;
			
			for(HtmlElement elem : optionList) {
				invoice = createInvoice(elem.getAttribute("value").replace("-", ""), 
							elem.asText(), null, 
							new URL("https://www.simplytel.de/phppdfdrillisch.php?dt=RECH&datum="+elem.getAttribute("value")+"&unterunteraction=ConvertDoc"));
				if(invoice != null) invoices.add(invoice);
			}
		} catch(Exception e) {
			throw new CrawlerException(e);
		}
		
		return invoices;
	}
	
	public void logout() throws LogoutException {
		try {
			getPage("/frei/logout.pl");
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
