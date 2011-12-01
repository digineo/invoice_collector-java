package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Keyweb extends Crawler {
	
	private HtmlAnchor invoiceLink;
	
	public Keyweb(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://kcm.keyweb.de/index.cgi");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("loginname").setValueAttribute(username);
		form.getInputByName("loginpasswd").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		List<HtmlElement> anchors = (List<HtmlElement>) currentPage.getElementsByTagName("a");
		for(HtmlElement link : anchors) {
			if(link.getAttribute("href").contains("rechnungonline")) {
				invoiceLink = (HtmlAnchor) link;
				break;
			}
		}
		
		if(invoiceLink == null) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			currentPage = invoiceLink.click();
			DomNodeList<HtmlElement> formList = currentPage.getElementsByTagName("form");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			HtmlAnchor pdfLink;
			Invoice invoice;
			String number;
			
			for(HtmlElement elem : formList) {
				
				if(!elem.getAttribute("name").equals("RNR")) continue;
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				
				if(cells.size() < 7) continue;
				if(links.size() < 1) continue;
				
				pdfLink = (HtmlAnchor) links.get(0);
				
				number = null;
				
				Matcher matcher = Pattern.compile("\\d+").matcher(cells.get(6).asText());
				if(matcher.find()) number = matcher.group(0);
				
				
				if(number != null) {
					invoice = createInvoice(number, cells.get(4).asText(), cells.get(2).asText(), pdfLink);
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
		// gibt es nicht.
	}
	
}
