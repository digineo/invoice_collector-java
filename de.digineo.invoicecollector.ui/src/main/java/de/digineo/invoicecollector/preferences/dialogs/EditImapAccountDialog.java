package de.digineo.invoicecollector.preferences.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.preferences.ImapAccountPreferences;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class EditImapAccountDialog extends NewImapAccountDialog {

	private ImapAccount editAccount;
	
	public EditImapAccountDialog(Shell parent, ImapAccount account) {
		super(parent);
		editAccount = account;
	}
	
	protected Control createDialogArea(Composite parent) {
		super.createDialogArea(parent);
		username.setText(editAccount.getUsername());
		host.setText(editAccount.getHost());
		host.setEnabled(false);
		port.setText(editAccount.getPort()+"");
		ssl.setSelection(editAccount.isSsl());
        return parent;
    }
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Imap-Account bearbeiten");
	}
	
	@Override
	protected void okPressed() {
		try {
			editAccount.update(username.getText(),password.getText(),Integer.parseInt(port.getText()), ssl.getSelection());
			// Aktualisieren
			IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(ImapAccountPreferences.ID);
			if(node != null && node.getPage() != null) {
				ImapAccountPreferences ap = (ImapAccountPreferences) node.getPage();
				ap.refresh();
			}
		} catch (NumberFormatException e) {
			Perspective.showError("Fehler beim Speichern: "+e.getMessage());
			e.printStackTrace();
		} catch (ConnectorException e) {
			Perspective.showError("Fehler beim Speichern: "+e.getMessage());
			e.printStackTrace();
		}
		okPressedNormal();
	}

}
