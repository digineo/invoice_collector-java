package de.digineo.invoicecollector;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.digineo.invoicecollector.preferences.MainPreferences;
import de.digineo.invoicecollector.sdk.API;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.digineo.invoicecollector.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	// Die API aus dem sdk-Bundle
	public static API api;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		
		super.start(context);
		plugin = this;
		api = API.INSTANCE;
		configureApi();
		api.connect();
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			
			/**
			 * Aktualisiert die Einstellungen der API bei jeder Änderung.
			 */
			public void propertyChange(PropertyChangeEvent event) {
				configureApi();
			}
			
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Kopiert die Einstellungen auch in die SDK-API
	 */
	private void configureApi() {
		IPreferenceStore store = plugin.getPreferenceStore();
		
		api.setFolder(store.getString(MainPreferences.FIELD_FOLDER));
		
		try {
			@SuppressWarnings("deprecation")
			URL filePath = Platform.resolve(Platform.find(plugin.getBundle(), new Path(File.separator+"files"+File.separator)));
			api.setFilePath(filePath.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
