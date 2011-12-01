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

public class Arcor extends Crawler {
	
	public Arcor(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.arcor.de/login/webbill_login.jsp");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("user_name").setValueAttribute(username);
		form.getInputByName("password").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		if(currentPage.getUrl().toString().contains("/login/")) throw new LoginException();
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			
			for(int i = 0;i<3;i++) getPage("https://www.webbill.arcor.de/webbill/jahresCheck.sap");
			
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
				if(links.size() == 0) continue;
				
				if(links.get(links.size()-1).getAttribute("href").contains("/Download/")) pdfLink = (HtmlAnchor) links.get(links.size()-1);

				
				if(pdfLink != null) {
					
					String number = "";
					Matcher matcher = Pattern.compile("/Rechnung\\D(\\d+)/").matcher(cells.get(1).asText());
					while(matcher.find()) {
						number = matcher.group();
						break;
					}
					
					invoice = createInvoice(number, cells.get(0).asText(), cells.get(2).asText(), pdfLink);
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
			getPage("/webbill/wblogout.sap");
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
