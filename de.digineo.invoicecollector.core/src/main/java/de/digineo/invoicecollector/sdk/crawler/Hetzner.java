package de.digineo.invoicecollector.sdk.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class Hetzner extends Crawler {
	
	public Hetzner(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://robot.your-server.de/");
		getPage("login/check?user="+username+"&password="+password);
		
		if(currentPage.getUrl().toString().endsWith("login/check")) throw new LoginException();
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			getPage("/invoice");
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("div");

			DomNodeList<HtmlElement> cells;
			String dataList[];
			
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				if(!elem.getAttribute("class").equals("box_wide")) continue;
				
				cells = elem.getElementsByTagName("table");
				
				for(HtmlElement cell : cells) {
					
					if(cell.getAttribute("onclick").equals("")) break;
					
					dataList = cell.getAttribute("onclick").split("/");
					if(dataList.length < 7) break;
					
					URL url = new URL("https://robot.your-server.de/invoice/deliver?number="+dataList[4]+"&date="+dataList[6].substring(0,dataList[6].indexOf("'")));
					invoice = createInvoice(dataList[4], parseDate(dataList[6].substring(0,dataList[6].indexOf("'")),"yyyyMMdd"), null, url);
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
			getPage("/login/logout");
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
