package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
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

public class Hexonet extends Crawler {
	
	public Hexonet(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://wi.hexonet.net/wi/54cd/include.php");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("LOGIN_USER").setValueAttribute(username);
		form.getInputByName("LOGIN_PASSWORD").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(currentPage.getElementsByName("title").get(0).asText().contains("Anmelden")) throw new LoginException();
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/wi/54cd/xirca/invoice/invoicelist.php");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() < 4) continue;
				if(links.size() < 1) continue;
				
				Matcher matcher = Pattern.compile("^\\d+$").matcher(links.get(0).asText());
				if(!matcher.find()) continue;
				
				pdfLink = (HtmlAnchor) links.get(0);
				
				if(pdfLink != null) {
					invoice = createInvoice(links.get(0).asText(), cells.get(1).asText(), cells.get(3).asText(), pdfLink);
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
			getPage("/wi/54cd/logout.php");
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
