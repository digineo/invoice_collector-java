package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Textbroker extends Crawler {
	
	public Textbroker(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.textbroker.de/");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("e_mail").setValueAttribute(username);
		form.getInputByName("pass").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(currentPage.getUrl().toString().endsWith("/c/home.php")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/Account/payments.aspx");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			String number = "";
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(links.size() < 1) continue;
				if(cells.size() < 4) continue;
				
				pdfLink = (HtmlAnchor) links.get(0);
				
				if(pdfLink != null) {
					Matcher matcher = Pattern.compile("Rechnung (\\S+)").matcher(pdfLink.asText());
					if(matcher.find()) number = matcher.group(1);
					
					invoice = createInvoice(number, cells.get(1).asText(), 
							cells.get(3).asText(), new URL("https://www.textbroker.de/c/"+pdfLink.getAttribute("href")));
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
			getPage("/c/logout.php");
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
