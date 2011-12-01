package de.digineo.invoicecollector.preferences.dialogs;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.preferences.ImapAccountPreferences;
import de.digineo.invoicecollector.preferences.ImapFilterPreferences;
import de.digineo.invoicecollector.sdk.objects.ImapFilter;
import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

public class EditImapFilterDialog extends NewImapFilterDialog {

	private ImapFilter editFilter;
	
	public EditImapFilterDialog(Shell parent, ImapFilter filter) {
		super(parent);
		editFilter = filter;
	}
	
	protected Control createDialogArea(Composite parent) {
		super.createDialogArea(parent);
		name.setText(editFilter.getName());
		name.setEnabled(false);
		subject.setText(editFilter.getSubject());
		search.setText(editFilter.getSearch());
		fileName.setText(editFilter.getFileName());
        return parent;
    }
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Imap-Account bearbeiten");
	}
	
	@Override
	protected void okPressed() {
		try {

			Pattern.compile(fileName.getText());
			Pattern.compile(subject.getText());
			
			editFilter.update(subject.getText(), fileName.getText(),search.getText());
			
			// Aktualisieren
			IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(ImapAccountPreferences.ID);
			if(node != null && node.getPage() != null) {
				ImapFilterPreferences ap = (ImapFilterPreferences) node.getPage();
				ap.refresh();
			}
		} catch(PatternSyntaxException ex) {
			Perspective.showError("Fehler beim Parsen des regulären Ausrucks: "+ex.getMessage());
			ex.printStackTrace();
		} catch (NumberFormatException e) {
			Perspective.showError("Fehler beim Speichern: "+e.getMessage());
			e.printStackTrace();
		} catch (ConnectorException e) {
			Perspective.showError("Fehler beim Speichern: "+e.getMessage());
			e.printStackTrace();
		}
		okPressedNormal();
	}

}
