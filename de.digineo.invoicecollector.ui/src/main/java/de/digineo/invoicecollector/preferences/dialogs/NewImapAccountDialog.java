package de.digineo.invoicecollector.preferences.dialogs;

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
import de.digineo.invoicecollector.preferences.ImapAccountPreferences;

public class NewImapAccountDialog extends Dialog {
	
	protected final FormToolkit toolkit = new FormToolkit(Display.getDefault());
	protected Text username;
	protected Text password;
	protected Text host;
	protected Text port;
	protected Button ssl;
	protected Shell parentShell;

	public NewImapAccountDialog(Shell parent) {
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
		composite.setLayout(new GridLayout(2, false));
		
		Label lblUsername = new Label(composite, SWT.NONE);
		lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblUsername, true, true);
		lblUsername.setText("Benutzername");
		
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gridData.widthHint = 240;
		username = new Text(composite, SWT.NONE);
		username.setLayoutData(gridData);
		toolkit.adapt(username, true, true);
		
		Label lblFirstname = new Label(composite, SWT.NONE);
		lblFirstname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblFirstname, true, true);
		lblFirstname.setText("Passwort");
		password = new Text(composite, SWT.NONE);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		password.setText("");
		toolkit.adapt(password, true, true);
		
		Label lblHost = new Label(composite, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblHost, true, true);
		lblHost.setText("Host");
		host = new Text(composite, SWT.NONE);
		host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		host.setText("");
		toolkit.adapt(host, true, true);
		
		Label lblPort = new Label(composite, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblPort, true, true);
		lblPort.setText("Port");
		port = new Text(composite, SWT.NONE);
		port.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		port.setText("993");
		toolkit.adapt(port, true, true);
		
		Label lblSsl = new Label(composite, SWT.NONE);
		lblSsl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblSsl, true, true);
		lblSsl.setText("SSL");
		ssl = new Button(composite, SWT.CHECK);
		ssl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		ssl.setSelection(true);
		toolkit.adapt(ssl, true, true);
		
		ssl.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				if(ssl.getSelection()) port.setText("993");
				else port.setText("443");
			}
			
		});
		
        return parent;
    }
	
	@Override
	protected Point getInitialSize() {
		return new Point(370, 250);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Imap-Account erstellen");
	}
	
	@Override
	protected void okPressed() {
		if(!username.getText().equals("") && !password.getText().equals("") && !host.getText().equals("")) {
			try {
				Activator.api.createImapAccount(host.getText(),username.getText(),password.getText(),port.getText(),ssl.getSelection());
				
				// Aktualisieren
				IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(ImapAccountPreferences.ID);
				if(node != null && node.getPage() != null) {
					ImapAccountPreferences ap = (ImapAccountPreferences) node.getPage();
					ap.refresh();
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		okPressedNormal();
	}
	
	protected void okPressedNormal() {
		super.okPressed();
	}
}
