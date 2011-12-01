package de.digineo.invoicecollector.preferences.dialogs;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.preferences.AccountPreferences;
import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.crawler.CrawlerException;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.tables.InvoiceTable;
import de.digineo.invoicecollector.views.FilterView;

public class NewAccountDialog extends Dialog {
	
	protected final FormToolkit toolkit = new FormToolkit(Display.getDefault());
	protected Label lblUsername;
	protected Text username;
	protected Text password;
	protected Combo module;
	protected Combo imapAccount;
	protected Combo imapFilter;
	protected Button modeWeb;
	protected Button modeImap;
	protected boolean isWeb = true;
	protected HashMap<String,ImapFilter> imapFilters = new HashMap<String,ImapFilter>();
	protected HashMap<String,ImapAccount> imapAccounts = new HashMap<String,ImapAccount>();
	protected Shell parentShell;

	public NewAccountDialog(Shell parent) {
		super(parent);
	}
	
	protected Control createDialogArea(Composite parent) {
		
		parentShell = parent.getShell();
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		
		// Formular zum Bearbeiten von Accounts
		Form form = new Form(parent, SWT.BORDER | SWT.LEFT | SWT.PUSH);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL | SWT.PUSH);
		fillLayout.spacing = 10;
		form.getBody().setLayout(fillLayout);
		Section section1 = toolkit.createSection(form.getBody(),
				Section.EXPANDED | Section.COMPACT);
		Composite composite = new Composite(section1, SWT.BOTTOM | SWT.PUSH);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
		section1.setClient(composite);
		composite.setLayout(new GridLayout(4, false));
		
		modeWeb = new Button(composite, SWT.LEFT | SWT.RADIO);
		modeWeb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		modeWeb.setSelection(true);
		modeWeb.setText("Web");
		toolkit.adapt(modeWeb, true, true);
		modeWeb.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				isWeb = true;
				updateMode();
			}
			
		});
		
		modeImap = new Button(composite, SWT.LEFT | SWT.RADIO);
		modeImap.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 2, 1));
		modeImap.setSelection(false);
		modeImap.setText("Imap");
		toolkit.adapt(modeImap, true, true);
		modeImap.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				isWeb = false;
				updateMode();
			}
			
		});
		
		lblUsername = new Label(composite, SWT.RIGHT);
		GridData gridData = new GridData(SWT.RIGHT, SWT.RIGHT, false, false, 2, 1);
		gridData.widthHint = 150;
		lblUsername.setLayoutData(gridData);
		toolkit.adapt(lblUsername, true, true);
		
		username = new Text(composite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gridData.widthHint = 250;
		username.setLayoutData(gridData);
		toolkit.adapt(username, true, true);
		
		Label lblFirstname = new Label(composite, SWT.NONE);
		lblFirstname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 2, 1));
		toolkit.adapt(lblFirstname, true, true);
		lblFirstname.setText("Passwort");
		password = new Text(composite, SWT.NONE);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		password.setText("");
		toolkit.adapt(password, true, true);
		
		Label lblCombo = new Label(composite, SWT.NONE);
		lblCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 2, 1));
		toolkit.adapt(lblCombo, true, true);
		lblCombo.setText("Modul");
		module = new Combo(composite, SWT.NONE);
		module.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		
		try {
			for(Class<?> cl : API.INSTANCE.getModules()) {
				module.add(cl.getSimpleName());
				if(module.getText().equals("")) module.setText(cl.getSimpleName());
			}
		} catch (CrawlerException e1) {
			e1.printStackTrace();
		}
		toolkit.adapt(module, true, true);
		
		
		Label lblAccount = new Label(composite, SWT.NONE);
		lblAccount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(lblAccount, true, true);
		lblAccount.setText("Imap-Account");
		imapAccount = new Combo(composite, SWT.NONE);
		imapAccount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		imapAccount.setEnabled(false);
		refreshImapAccount();
		toolkit.adapt(imapAccount, true, true);
		
		
		Label lblFilter = new Label(composite, SWT.NONE);
		lblFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		toolkit.adapt(lblFilter, true, true);
		lblFilter.setText("Imap-Filter");
		imapFilter = new Combo(composite, SWT.NONE);
		imapFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		imapFilter.setEnabled(false);
		refreshImapFilter();
		toolkit.adapt(imapFilter, true, true);
		
		updateMode();
		
        return parent;
    }
	
	@Override
	protected Point getInitialSize() {
		return new Point(436, 310);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Account erstellen");
	}
	
	protected void refreshImapAccount() {
		imapAccount.removeAll();
		imapAccounts.clear();
		try {
			ArrayList<ImapAccount> imapAccountsList = API.INSTANCE.getImapAccounts();
			for(ImapAccount acc : imapAccountsList) {
				imapAccount.add(acc.getUsername() + " ("+acc.getHost()+")");
				imapAccounts.put(acc.getUsername() + " ("+acc.getHost()+")", acc);
			}
		} catch (ApiException e1) {
			Perspective.showError("Imap-Accounts konnten nicht geladen werden: "+e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	protected void updateMode() {
		lblUsername.setText(isWeb ? "Benutzername" : "Bezeichnung");
		imapFilter.setEnabled(!isWeb);
		imapAccount.setEnabled(!isWeb);
		module.setEnabled(isWeb);
		password.setEnabled(isWeb);
		modeWeb.setSelection(isWeb);
		modeImap.setSelection(!isWeb);
	}
	
	protected void refreshImapFilter() {
		imapFilter.removeAll();
		imapFilters.clear();
		try {
			ArrayList<ImapFilter> imapFiltersList = API.INSTANCE.getImapFilter();
			for(ImapFilter filter : imapFiltersList) {
				imapFilter.add(filter.getName());
				imapFilters.put(filter.getName(), filter);
			}
		} catch (ApiException e1) {
			Perspective.showError("Imap-Filter konnten nicht geladen werden: "+e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	private void createAccount() {
		if(isWeb && !username.getText().equals("") && !password.getText().equals("")) {
			try {
				Account account = Activator.api.createAccount(username.getText(),password.getText(),module.getText());
			
				FilterView filterView = (FilterView) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FilterView.ID);
				filterView.refreshModule();
				
				InvoiceTable table = (InvoiceTable) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(InvoiceTable.ID);
				table.crawlInvoices(account);
				
				// Aktualisieren
				IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(AccountPreferences.ID);
				if(node != null && node.getPage() != null) {
					AccountPreferences ap = (AccountPreferences) node.getPage();
					ap.refresh();
				}
				
				username.setText("");
				password.setText("");
			} catch (Exception e) {
				e.printStackTrace();
			} 
			return;
		}
		
		if(isWeb || username.getText().equals("")) return;
		
		try {
			Account account = Activator.api.createAccount(username.getText(), imapAccounts.get(imapAccount.getText()), imapFilters.get(imapFilter.getText()));
		
			FilterView filterView = (FilterView) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FilterView.ID);
			filterView.refreshModule();
			
			InvoiceTable table = (InvoiceTable) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(InvoiceTable.ID);
			table.crawlInvoices(account);
			
			// Aktualisieren
			IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(AccountPreferences.ID);
			if(node != null && node.getPage() != null) {
				AccountPreferences ap = (AccountPreferences) node.getPage();
				ap.refresh();
			}
			
			username.setText("");
			password.setText("");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	protected void okPressed() {
		createAccount();
		okPressedNormal();
	}
	
	protected void okPressedNormal() {
		super.okPressed();
	}
}
