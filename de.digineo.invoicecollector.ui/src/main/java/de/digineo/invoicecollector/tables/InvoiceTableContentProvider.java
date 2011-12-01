package de.digineo.invoicecollector.tables;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class InvoiceTableContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() { }

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) { }
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object[] getElements(Object inputElement) {
		return ((List) inputElement).toArray();
	}

}
