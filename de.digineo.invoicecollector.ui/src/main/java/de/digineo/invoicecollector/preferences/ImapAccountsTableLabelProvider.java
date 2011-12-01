package de.digineo.invoicecollector.preferences;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.digineo.invoicecollector.sdk.objects.ImapAccount;

public class ImapAccountsTableLabelProvider implements ITableLabelProvider {

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
		ImapAccount account = (ImapAccount) element;
		
		switch(columnIndex) {
		case 0:
			return account.getHost();
		case 1:
			return account.getUsername();
		case 2:
			return account.getPort()+"";
		}
		return null;
	}

}
