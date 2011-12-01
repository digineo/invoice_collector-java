package de.digineo.invoicecollector.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.digineo.invoicecollector.Activator;

public class OpenFolderHandler extends AbstractHandler {

	/**
	 * Öffnet das Speicherverzeichnis für heruntergeladene Rechnungen.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Desktop d = Desktop.getDesktop();
			d.open(new File(Activator.api.getFolder()));
		} catch (IOException e) {
			throw new ExecutionException(e.getMessage());
		}
		return null;
	}

}
