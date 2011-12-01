package de.digineo.invoicecollector.preferences.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.preferences.ImapFilterPreferences;

public class NewImapFilterDialog extends Dialog {
	
	protected final FormToolkit toolkit = new FormToolkit(Display.getDefault());
	protected Text subject;
	protected Text search;
	protected Text name;
	protected Text fileName;
	protected Shell parentShell;

	public NewImapFilterDialog(Shell parent) {
		super(parent);
	}
	
	protected Control createDialogArea(Composite parent) {
		
		parentShell = parent.getShell();
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		
		// Formular zum Bearbeiten von Accounts
		Form form = new Form(parent, SWT.BORDER | SWT.LEFT | SWT.PUSH);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL | SWT.PUSH);
		fillLayout.spacing = 10;
		form.getBody().setLayout(fillLayout);
		Section section1 = toolkit.createSection(form.getBody(),
				Section.EXPANDED | Section.COMPACT);
		Composite composite = new Composite(section1, SWT.BOTTOM | SWT.PUSH);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
		section1.setClient(composite);
		composite.setLayout(new GridLayout(2, false));
		
		
		Label lblName = new Label(composite, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblName, true, true);
		lblName.setText("Filtername");
		
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gridData.widthHint = 360;
		name = new Text(composite, SWT.NONE);
		name.setLayoutData(gridData);
		toolkit.adapt(name, true, true);
		
		Label lblSubject = new Label(composite, SWT.NONE);
		lblSubject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblSubject, true, true);
		lblSubject.setText("Betreff");
		subject = new Text(composite, SWT.NONE);
		subject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,1, 1));
		subject.setText("");
		toolkit.adapt(subject, true, true);
		
		Label lblFileName = new Label(composite, SWT.NONE);
		lblFileName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblFileName, true, true);
		lblFileName.setText("Datei");
		fileName = new Text(composite, SWT.NONE);
		fileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,1, 1));
		fileName.setText("");
		toolkit.adapt(fileName, true, true);
		
		Label lblSearch = new Label(composite, SWT.NONE);
		lblSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		toolkit.adapt(lblSearch, true, true);
		lblSearch.setText("Suche");
		search = new Text(composite, SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false,1, 1);
		gridData.heightHint = 70;
		search.setLayoutData(gridData);
		search.setText("");
		toolkit.adapt(search, true, true);
		
        return parent;
    }
	
	@Override
	protected Point getInitialSize() {
		return new Point(465, 280);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Imap-Account erstellen");
	}
	
	@Override
	protected void okPressed() {
		if(!name.getText().equals("")) {
			try {
				Activator.api.createImapFilter(name.getText(), subject.getText(), fileName.getText(),
						search.getText());
				
				// Aktualisieren
				IPreferenceNode node = Activator.getDefault().getWorkbench().getPreferenceManager().find(ImapFilterPreferences.ID);
				if(node != null && node.getPage() != null) {
					ImapFilterPreferences ap = (ImapFilterPreferences) node.getPage();
					ap.refresh();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		okPressedNormal();
	}
	
	protected void okPressedNormal() {
		super.okPressed();
	}
}
