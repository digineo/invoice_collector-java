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

public class Swb extends Crawler {
	
	public Swb(String username, String password, Account account) {
		super(username, password, account);
	}

	public void login() throws LoginException, IOException {
		getPage("https://www.swb-gruppe.de/online-service/hb/css/startLogin.jsp");
		
		HtmlForm form = getFirstForm();
		
		form.getInputByName("Process.User.RegGPNumber").setValueAttribute(username);
		form.getInputByName("Process.User.Password").setValueAttribute(password);
		
		currentPage = getFirstButton(form).click();
		
		DomNodeList<HtmlElement> headers = currentPage.getElementsByTagName("h3");
		if(headers.size() > 0 && headers.get(0).asText().contains("Login")) throw new LoginException();
		
		
	}
	
	public ArrayList<Invoice> crawl() throws CrawlerException {
		ArrayList<Invoice> invoices = new ArrayList<Invoice>();
		
		try {
			post("DokAnsicht");
			
			DomNodeList<HtmlElement> headers = currentPage.getElementsByTagName("h3");
			if(headers.size() > 0 && !headers.get(0).asText().equals("Dokumente")) throw new CrawlerException("Ungültige Seite: "+headers.get(0).asText());
			 
			DomNodeList<HtmlElement> trList = currentPage.getElementsByTagName("tr");

			DomNodeList<HtmlElement> cells;
			DomNodeList<HtmlElement> links;
			String date = "";
			ArrayList<String> params;
			HtmlAnchor pdfLink;
			Invoice invoice;
			
			for(HtmlElement elem : trList) {
				
				pdfLink = null;
				
				cells = elem.getElementsByTagName("td");
				links = elem.getElementsByTagName("a");
				params = new ArrayList<String>();
				
				if(cells.size() < 2) continue;
				if(links.size() < 1) continue;
				
				pdfLink = (HtmlAnchor) links.get(0);

				Matcher matcher = Pattern.compile("download\\('(.+)','(.+)'\\)").matcher(pdfLink.getAttribute("href"));
				while(matcher.find()) {
					params.add(matcher.group());
				}
				
				if(pdfLink != null) {
					matcher = Pattern.compile("Rechnung vom ([\\d\\.]{10})").matcher(cells.get(1).asText());
					if(matcher.find()) date = matcher.group(1);
					invoice = createInvoice(params.get(1), date, null, 
							new URL("https://www.swb-gruppe.de/docs/"+params.get(1)+"?docTypeId="+params.get(2)));
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
			post("Logout");
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
	
	private void post(String action) throws IOException {
		getPage("/online-service/hb/css/do?action=save&proc=Startseite%20Online-Service&current=mainmenu&UID=&go2=1&goto=1&Process.User.Selection="+action+"&typosize=elements&Process.User.Topic=&isGotoNextScreen=false&currentScreen=0");
	}
	
}
