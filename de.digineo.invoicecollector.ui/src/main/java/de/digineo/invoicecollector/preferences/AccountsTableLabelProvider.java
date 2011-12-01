package de.digineo.invoicecollector.preferences;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.digineo.invoicecollector.sdk.objects.Account;

public class AccountsTableLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener arg0) { }

	@Override
	public void dispose() { }

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) { return false; }

	@Override
	public void removeListener(ILabelProviderListener arg0) { }

	@Override
	public Image getColumnImage(Object arg0, int arg1) { return null; }

	/**
	 * Gibt Modul und Accountnamen aus.
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		Account account = (Account) element;
		
		switch(columnIndex) {
		case 0:
			if(account.getImapAccount() == null) return account.getModule();
			else return "Imap";
		case 1:
			return account.getUsername();
		}
		return null;
	}

}
