package de.digineo.invoicecollector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.sdk.objects.Invoice;
import de.digineo.invoicecollector.tables.InvoiceTable;

public class OpenInvoiceHandler extends AbstractHandler {

	/**
	 * Öffnet die Rechnung.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Ausgewähltes Objekt aus der Tabelle ermitteln
		StructuredSelection selection = (StructuredSelection) Activator.getDefault().getWorkbench().
				getActiveWorkbenchWindow().getActivePage().getSelection(InvoiceTable.ID);
		Invoice invoice = (Invoice) selection.getFirstElement();
		invoice.openFile();
		return null;
	}

}
