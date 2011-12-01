package de.digineo.invoicecollector.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.preferences.dialogs.EditImapAccountDialog;
import de.digineo.invoicecollector.preferences.dialogs.NewImapAccountDialog;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;
import de.digineo.invoicecollector.tables.InvoiceTable;
import de.digineo.invoicecollector.views.FilterView;

/**
 * Einstellungsseite für Accounts.
 */
public class ImapAccountPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "de.digineo.invoicecollector.preferences.imap";
	private org.eclipse.swt.widgets.Table table;
	private TableViewer tableViewer;
	private Button button1;
	private Button button2;
	private Button button3;
	private TableColumn columnHost;
	private ArrayList<ImapAccount> accounts = new ArrayList<ImapAccount>();
	private ImapAccount editAccount;
	private Shell parentShell;

	public ImapAccountPreferences() {
		super(GRID);
	}
	
	/**
	 * Initialisiert die Einstellungen.
	 */
	@Override
	public void init(IWorkbench workbench) {
		setDescription("Hier können Sie alle eingetragenen Accounts verwalten oder Neue hinzufügen.");
	}	
	
	/**
	 * Baut das Einstellungsfenster bestehend aus Tabelle und
	 * Formular zusammen.
	 */
	public Control createContents(Composite parent) {
		
		// Apply-Button entfernen
		this.noDefaultAndApplyButton();
		
		parentShell = parent.getShell();
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		mainComposite.setLayout(gridLayout);
		
		tableViewer = new TableViewer(mainComposite, SWT.BORDER | SWT.PUSH);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = 400;
		data.widthHint = 350;
		table.setLayoutData(data);
		
		TableViewerColumn hostViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		columnHost = hostViewerColumn.getColumn();
		columnHost.setResizable(false);
		columnHost.setWidth(120);
		columnHost.setText("Host");
		
		// Nach Hostnamen sortieren
		columnHost.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<ImapAccount> comp = new Comparator<ImapAccount>() {

					@Override
					public int compare(ImapAccount o1, ImapAccount o2) {
						return o1.getHost().compareTo(o2.getHost());
					}
					
				};
				Collections.sort(accounts,comp);
				tableViewer.setInput(accounts);
			}
			
		});
		
		TableViewerColumn usernameViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn columnUsername = usernameViewerColumn.getColumn();
		columnUsername.setResizable(false);
		columnUsername.setWidth(120);
		columnUsername.setText("Benutzername");
		
		// Nach Username sortieren
		columnUsername.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<ImapAccount> comp = new Comparator<ImapAccount>() {

					@Override
					public int compare(ImapAccount o1, ImapAccount o2) {
						return o1.getUsername().compareTo(o2.getUsername());
					}
					
				};
				Collections.sort(accounts,comp);
				tableViewer.setInput(accounts);
				
			}
			
		});
		
		TableViewerColumn portViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn columnPort = portViewerColumn.getColumn();
		columnPort.setResizable(false);
		columnPort.setWidth(40);
		columnPort.setText("Port");
		
		// Nach Port sortieren
		columnPort.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<ImapAccount> comp = new Comparator<ImapAccount>() {

					@Override
					public int compare(ImapAccount o1, ImapAccount o2) {
						return (o1.getPort() > o2.getPort() ? 1 : -1);
					}
					
				};
				Collections.sort(accounts,comp);
				tableViewer.setInput(accounts);
				
			}
			
		});
		
		
		// Öffnet einen angeklickten Account im Formular.
		// Macht dabei den Usernamen und das Modul unauswählbar
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection)event.getSelection();
				ImapAccount account  = (ImapAccount)selection.getFirstElement();
				if(account == null) return;

				editAccount = account;
				
				button2.setEnabled(true);
				button3.setEnabled(true);
			}
		});
		
		tableViewer.setContentProvider(new AccountsTableContentProvider());
		tableViewer.setLabelProvider(new ImapAccountsTableLabelProvider());
		
		
		
		// Rechte Buttonleiste
		Composite rightComposite = new Composite(mainComposite, SWT.PUSH);
		rightComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		rightComposite.setLayout(gridLayout);
		
		button1 = new Button(rightComposite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL);
		gridData.widthHint = 140;
		button1.setLayoutData(gridData);
		button1.setText("Neu...");
		
		button1.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				NewImapAccountDialog dialog = new NewImapAccountDialog(parentShell);
				dialog.open();
			}
			
		});
		
		button2 = new Button(rightComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.widthHint = 140;
		button2.setLayoutData(gridData);
		button2.setText("Editieren...");
		button2.setEnabled(false);
		
		button2.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent event) {
				if(editAccount == null) return;
				EditImapAccountDialog dialog = new EditImapAccountDialog(parentShell,editAccount);
				dialog.open();
			}
			
		});
		
		button3 = new Button(rightComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.widthHint = 140;
		button3.setLayoutData(gridData);
		button3.setText("Löschen");
		button3.setEnabled(false);
		button3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try {
					if(editAccount == null) return;
					
					MessageDialog messageDialog2 = new MessageDialog(parentShell, "Account wirklich löschen?", null,
					        "Möchten Sie den Imap-Account "+editAccount.getUsername()+" wirklich löschen? " +
					        		"Dabei werden auch alle Filter des Accounts gelöscht.", MessageDialog.WARNING,
					        new String[] { "Account löschen", "Abbrechen" }, 1);
					
					if (messageDialog2.open() == 1) {
						return;
					}
					
					editAccount.remove();
					button2.setEnabled(false);
					button3.setEnabled(false);
					
					tableViewer.setSelection(null);
					editAccount = null;
					
					FilterView filterView = (FilterView) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FilterView.ID);
					filterView.refreshModule();

					InvoiceTable table = (InvoiceTable) Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(InvoiceTable.ID);
					table.refresh();
					
					refresh();
					
				} catch(Exception ex) {
					Perspective.showError("Fehler beim Speichern: "+ex.getMessage());
					ex.printStackTrace();
				}
				
			}
		});
		
		refresh();
		
		return null;
	}
	
	public void refresh(){
		try {
			accounts = Activator.api.getImapAccounts();
			tableViewer.setInput( accounts );
		}
		catch (ApiException e) {
			Perspective.showError("ImapAccounts konnte nicht abgerufen werden:\n" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Reagiert auf Änderungen von Eingabfelder
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		
	}

	/**
	 * Überprüft die Gültigkeit der Eingaben d.h. ob der Pfad existiert.
	 */
	protected void checkState() {
		super.checkState();
		
	}


	@Override
	protected void createFieldEditors() { }
}
