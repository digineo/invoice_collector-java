package de.digineo.invoicecollector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.digineo.invoicecollector.tables.InvoiceTable;
import de.digineo.invoicecollector.views.FilterView;

public class Perspective implements IPerspectiveFactory {
	
	/**
	 * Initialisiert die Perspektive und fügt die Views hinzu
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(true);
		layout.setEditorAreaVisible(false);
		

		// Einfügen der Suche
		layout.addView(FilterView.ID, IPageLayout.TOP | IPageLayout.LEFT, 0.2f, layout.getEditorArea());
		layout.getViewLayout(FilterView.ID).setCloseable(false);
		layout.getViewLayout(FilterView.ID).setMoveable(false);
		
		// Einfügen des Views für die Rechnungen
		layout.addView(InvoiceTable.ID, IPageLayout.BOTTOM, 0.5f, layout.getEditorArea());
		layout.getViewLayout(InvoiceTable.ID).setCloseable(false);
		layout.getViewLayout(InvoiceTable.ID).setMoveable(false);
		
	}
	
	/**
	 * Anzeige eines Fehler durch einen MessageDialog.
	 * @param msg
	 */
	public static void showError(String msg) {
		Shell shell = new Shell(Display.getDefault());
		MessageDialog messageDialog = new MessageDialog(shell, "Fehler", null,
				msg, MessageDialog.ERROR, new String[] { "Schließen" }, 1);
		int answer = messageDialog.open();
		if(answer == 0) messageDialog.close();
	}
}
