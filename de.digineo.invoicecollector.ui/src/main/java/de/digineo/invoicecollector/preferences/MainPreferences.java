package de.digineo.invoicecollector.preferences;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.digineo.invoicecollector.Activator;

/**
 * Einstellungsseite für den Speicher-Pfad
 */
public class MainPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "de.digineo.invoicecollector.preferences.main";
	public static final String FIELD_FOLDER = "folder";

	private StringFieldEditor folderField;

	public MainPreferences() {
		super(GRID);
	}

	public void createFieldEditors() {
		{
			Composite composite = getFieldEditorParent();
			folderField = new DirectoryFieldEditor(FIELD_FOLDER, "Pfad zur Speicherung der Rechnungen", composite);
			folderField.getLabelControl(composite).setText("Speicherpfad");
			folderField.setTextLimit(100);
			folderField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);

			addField(folderField);

			// Spacer
			Label title = new Label(getFieldEditorParent(), SWT.NONE);
			title.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		}
	}
	
	/**
	 * Initialisiert die Einstellungen.
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Geben Sie hier den Pfad zum Rechnungsarchiv an.");
	}
	
	/**
	 * Reagiert auf Änderungen von Eingabfelder
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		
		if (event.getSource() == folderField) {
			// Validieren
			checkState();
		}
	}

	/**
	 * Überprüft die Gültigkeit der Eingaben d.h. ob der Pfad existiert.
	 */
	protected void checkState() {
		super.checkState();
		
		if (folderField.getStringValue() != null && folderField.getStringValue().length() > 0) {
			
			// Prüfen ob Pfad existiert.
			File pathFile = new File(folderField.getStringValue());
			if(!pathFile.exists()) {
				setErrorMessage("Pfad existiert nicht.");
				setValid(false);
				return;
			}
			
			setErrorMessage(null);
			setValid(true);
		}
		else {
			// leerer String
			setErrorMessage("Pfad darf nicht leer sein!");
			setValid(false);
		}
	}
}
