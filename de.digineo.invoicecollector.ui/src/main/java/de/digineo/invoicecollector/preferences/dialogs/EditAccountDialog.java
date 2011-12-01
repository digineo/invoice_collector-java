package de.digineo.invoicecollector.preferences.dialogs;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class EditAccountDialog extends NewAccountDialog {

	private Account editAccount;
	
	public EditAccountDialog(Shell parent, Account account) {
		super(parent);
		editAccount = account;
	}
	
	protected Control createDialogArea(Composite parent) {
		super.createDialogArea(parent);
		if(editAccount.getImapAccount() != null && editAccount.getImapFilter() != null) {
			isWeb = false;
			updateMode();
			username.setText(editAccount.getUsername());
			imapFilter.setText(editAccount.getImapFilter().getName());
			imapAccount.setText(editAccount.getImapAccount().getUsername() + " " +
					"("+editAccount.getImapAccount().getHost()+")");
		} else {
			isWeb = true;
			updateMode();
			username.setText(editAccount.getUsername());
			module.setText(editAccount.getModule());
			username.setEnabled(false);
			module.setEnabled(false);
		}
        return parent;
    }
	
	@Override
	protected Point getInitialSize() {
		return new Point(436, 310);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Account bearbeiten");
	}
	
	protected void updateMode() {
		if(isWeb) {
			imapFilter.setEnabled(false);
			imapAccount.setEnabled(false);
			module.setEnabled(true);
			password.setEnabled(true);
			modeWeb.setSelection(true);
			modeImap.setSelection(false);
		} else {
			module.setEnabled(false);
			imapFilter.setEnabled(true);
			imapAccount.setEnabled(true);
			password.setEnabled(false);
			modeWeb.setSelection(false);
			modeImap.setSelection(true);
		}
		modeImap.setEnabled(false);
		modeWeb.setEnabled(false);
	}
	
	@Override
	protected void okPressed() {
		try {
			if(editAccount != null && editAccount.getImapAccount() == null) {
				editAccount.update(password.getText());
			} else if(editAccount != null && editAccount.getImapAccount() != null) {
				editAccount.updateUsername(username.getText());
			}
			okPressedNormal();
		} catch(ConnectorException e) {
			Perspective.showError("Fehler beim Speichern: "+e.getMessage());
			e.printStackTrace();
		}
		
	}

}
