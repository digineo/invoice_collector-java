package de.digineo.invoicecollector.preferences.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.preferences.AccountsTableContentProvider;
import de.digineo.invoicecollector.preferences.FilterTestTableLabelProvider;
import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.crawler.CrawlerException;
import de.digineo.invoicecollector.sdk.crawler.Imap;
import de.digineo.invoicecollector.sdk.crawler.LoginException;
import de.digineo.invoicecollector.sdk.crawler.LogoutException;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class FilterDialog extends Dialog {
	
	private ImapFilter imapFilter;
	private Combo account;
	private Button testButton;
	private HashMap<String,ImapAccount> accounts = new HashMap<String,ImapAccount>();
	private org.eclipse.swt.widgets.Table table;
	private TableViewer tableViewer;

	public FilterDialog(Shell parent, ImapFilter imapFilter) {
		super(parent);
		this.imapFilter = imapFilter;
	}
	
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(3, false));
		
		
		// Tabelle laden
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.PUSH);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData();
		data.heightHint = 180;
		table.setLayoutData(data);
		
		// Spalten
		TableViewerColumn numberViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn columnNumber = numberViewerColumn.getColumn();
		columnNumber.setResizable(false);
		columnNumber.setWidth(100);
		columnNumber.setText("Rechnungsnr.");
		
		TableViewerColumn fileNameViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn columnFileName = fileNameViewerColumn.getColumn();
		columnFileName.setResizable(false);
		columnFileName.setWidth(250);
		columnFileName.setText("Dateiname");
		
		TableViewerColumn dateViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn columnDate = dateViewerColumn.getColumn();
		columnDate.setResizable(false);
		columnDate.setWidth(150);
		columnDate.setText("Datum");
		
		tableViewer.setContentProvider(new AccountsTableContentProvider());
		tableViewer.setLabelProvider(new FilterTestTableLabelProvider());
		
		// Label setzen
		Label lblAccount = new Label(composite, SWT.RIGHT);
		lblAccount.setText("Test-Account");
		
		// Select setzen
		account = new Combo(composite, SWT.NONE);
		account.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		try {
			
			// Accounts laden
			ArrayList<ImapAccount> imapAccounts = API.INSTANCE.getImapAccounts();
			for(ImapAccount acc : imapAccounts) {
				account.add(acc.getHost() + " - " + acc.getUsername());
				accounts.put(acc.getHost() + " - " + acc.getUsername(), acc);
			}
			
		} catch (ApiException e) {
			e.printStackTrace();
			Perspective.showError("Fehler beim Laden der Accounts: "+e.getMessage());
		}
		
		// Button setzen
		testButton = new Button(composite, SWT.NONE);
		testButton.setText("Testen");
		testButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				if(accounts.containsKey(account.getText())) testFilter(accounts.get(account.getText()));
			}
			
		});
		
        return composite;
    }
	
	@Override
	protected Point getInitialSize() {
		return new Point(530, 330);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Filter Testen");
	}
	
	private void testFilter(ImapAccount account) {
		
		try {
			Imap imapCrawler = new Imap(null, account, imapFilter, null);
			imapCrawler.login();
			ArrayList<Invoice> invoices = imapCrawler.crawl();
			tableViewer.setInput( invoices );
			imapCrawler.logout();
		} catch (LoginException e) {
			Perspective.showError("Fehler beim Login: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Perspective.showError("Fehler: "+e.getMessage());
			e.printStackTrace();
		} catch (LogoutException e) {
			Perspective.showError("Fehler beim Logout: "+e.getMessage());
			e.printStackTrace();
		} catch (CrawlerException e) {
			Perspective.showError("Fehler beim Laden der Rechnungen: "+e.getMessage());
			e.printStackTrace();
		}
		
	}

}
