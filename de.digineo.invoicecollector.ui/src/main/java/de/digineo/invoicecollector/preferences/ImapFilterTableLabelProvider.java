package de.digineo.invoicecollector.preferences;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.digineo.invoicecollector.sdk.objects.ImapFilter;

public class ImapFilterTableLabelProvider implements ITableLabelProvider {

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
	 * Gibt Name des Filters an.
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		ImapFilter filter = (ImapFilter) element;
		
		switch(columnIndex) {
		case 0:
			return filter.getName();
		}
		return null;
	}

}
