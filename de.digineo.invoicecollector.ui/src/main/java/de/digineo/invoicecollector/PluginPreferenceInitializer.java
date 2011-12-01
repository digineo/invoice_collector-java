package de.digineo.invoicecollector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;

import de.digineo.invoicecollector.preferences.MainPreferences;

/**
 * 
 * Legt die Default-Einstellungen fest
 *
 */
public class PluginPreferenceInitializer extends AbstractPreferenceInitializer {
	
	/**
	 * Setzt die Standardeinstellungen.
	 */
	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		// Speicherpfad setzen
		Location location = Platform.getInstanceLocation();
		store.setDefault(MainPreferences.FIELD_FOLDER, location.getURL().getPath());
		// TODO API.folder = location.getURL().getPath();
	}

}
