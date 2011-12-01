package de.digineo.invoicecollector.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.tables.InvoiceTable;

public class RefreshHandler extends AbstractHandler {

	/**
	 * Crawlt alle eingetragenen Accounts und fügt ggf. neue
	 * Rechungen hinzu.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InvoiceTable table = (InvoiceTable) Activator.getDefault().getWorkbench().
				getActiveWorkbenchWindow().getActivePage().findView(InvoiceTable.ID);
		table.crawlInvoices();
		table.refresh();
		return null;
	}

}
