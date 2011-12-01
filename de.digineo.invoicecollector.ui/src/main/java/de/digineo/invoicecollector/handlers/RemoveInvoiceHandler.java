package de.digineo.invoicecollector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.sdk.objects.Invoice;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;
import de.digineo.invoicecollector.tables.InvoiceTable;

public class RemoveInvoiceHandler extends AbstractHandler {

	/**
	 * Löscht eine Rechnung
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Ausgewähltes Objekt aus der Tabelle ermitteln
		StructuredSelection selection = (StructuredSelection) Activator.getDefault().getWorkbench().
				getActiveWorkbenchWindow().getActivePage().getSelection(InvoiceTable.ID);
		Invoice invoice = (Invoice) selection.getFirstElement();
		try {
			invoice.remove();
		} catch (ConnectorException e) {
			Perspective.showError("Fehler beim Löschen: "+e.getMessage());
			e.printStackTrace();
		}
		InvoiceTable table = (InvoiceTable) Activator.getDefault().getWorkbench().
				getActiveWorkbenchWindow().getActivePage().findView(InvoiceTable.ID);
		table.refresh();
		return null;
	}

}
