package de.digineo.invoicecollector.tables;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.objects.DataObject;
import de.digineo.invoicecollector.sdk.objects.Invoice;

public class InvoiceTableLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener arg0) { }

	@Override
	public void dispose() { }

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) { return false; }

	@Override
	public void removeListener(ILabelProviderListener arg0) { }

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Invoice invoice = (Invoice) element;
		
		switch(columnIndex) {
		case 0:
			return null;
		case 1:
			if(!invoice.isImap()) return invoice.getAccount().getModule()+" ("+invoice.getAccount().getUsername()+")";
			else return invoice.getAccount().getUsername()+" (Imap)";
		case 2:
			return invoice.getNumber();
		case 3:
			return DataObject.dateFormat2.format(invoice.getDate());
		case 4:
			if(invoice.getAmount() == null) return "";
			else return API.getEuro(invoice.getAmount());
		}
		
		return null;
	}
	
	/**
	 * Gibt das Bild für Pdfs zurück.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if(columnIndex != 0) return null;
		Invoice invoice = (Invoice) element;
		if(invoice.getOriginalFile().exists()) {
			return InvoiceTable.PDF;
		}
		return null;
	}
	
	

}
