package de.digineo.invoicecollector.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.tables.InvoiceTable;

public class FilterView extends ViewPart  {
	
	public static String ID = "de.digineo.invoicecollector.FilterView";
	public static String DEFAULT_TEXT = "Nach Rechnungsnr. suchen..."; 
	private final FormToolkit toolkit = new FormToolkit(Display.getDefault());
	private Combo module;
	private Text startDate;
	private Text endDate;
	private Text search;
	
	
	public FilterView() { }

	@Override
	public void createPartControl(Composite parent) {
		
		Form form = new Form(parent, SWT.BORDER | SWT.TOP | SWT.PUSH);
		form.setLayoutData("North");
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL | SWT.PUSH);
		fillLayout.spacing = 5;
		form.getBody().setLayout(fillLayout);
		Section section1 = toolkit.createSection(form.getBody(),
				Section.EXPANDED | Section.COMPACT);
		Composite composite = new Composite(section1, SWT.TOP | SWT.PUSH);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
		section1.setClient(composite);
		composite.setLayout(new GridLayout(8, false));
		
		
		module = new Combo(composite, SWT.NONE);
		module.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		refreshModule();
		
		
		module.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				search();
			}
			
		});

		toolkit.adapt(module, true, true);
		
		startDate = new Text(composite, SWT.NONE);
		startDate.setLayoutData(new GridData(SWT.NONE, SWT.LEFT, false, false, 1, 1));
		toolkit.adapt(startDate, true, true);
		Calendar calendar2 = new GregorianCalendar();
		calendar2.setTime(calendar2.getTime());
		startDate.setText("01.01."+calendar2.get(Calendar.YEAR));
		startDate.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent event) {
				if(event.keyCode == 13) search();
			}

			@Override
			public void keyReleased(KeyEvent arg0) { }
			
		});
		
		Label lblStart = new Label(composite, SWT.NONE);
		lblStart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblStart, true, true);
		lblStart.setText(" bis ");

		
		endDate = new Text(composite, SWT.NONE);
		endDate.setLayoutData(new GridData(SWT.NONE, SWT.LEFT, false, false, 1, 1));
		toolkit.adapt(endDate, true, true);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		endDate.setText("01.01."+calendar.get(Calendar.YEAR));
		endDate.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent event) {
				if(event.keyCode == 13) search();
			}

			@Override
			public void keyReleased(KeyEvent arg0) { }
			
		});
		
		search = new Text(composite, SWT.NONE);
		search.setLayoutData(new GridData(SWT.NONE, SWT.RIGHT, false, false, 1, 1));
		toolkit.adapt(search, true, true);
		search.setText("Nach Rechnungsnr. suchen...");
		
		/**
		 * Sorgt dafür, dass bei Eingabe von Enter das Formular 
		 * abgeschickt wird.
		 */
		search.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				search.setText("");
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if(search.getText().equals("")) search.setText(DEFAULT_TEXT);
				
			}
			
		});
		
		
		/**
		 * Sorgt dafür, dass bei Eingabe von Enter das Formular 
		 * abgeschickt wird.
		 */
		search.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent event) {
				if(event.keyCode == 13) search();
			}

			@Override
			public void keyReleased(KeyEvent arg0) { }
			
		});
		
		Button btnNewButton = new Button(composite, SWT.RIGHT);
		btnNewButton.setLayoutData(new GridData(SWT.NONE, SWT.RIGHT, true, false,
				1, 1));
		toolkit.adapt(btnNewButton, true, true);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				search();
			}
		});
		btnNewButton.setText("Anzeigen");
	}

	@Override
	public void setFocus() { }
	
	/**
	 * Startet die Suche anhand der Formularfelder.
	 */
	private void search() {
		try {
			InvoiceTable table = (InvoiceTable) getSite().getPage().findView(InvoiceTable.ID);
			Date start = new SimpleDateFormat("dd.MM.yyyy").parse(startDate.getText());
			Date end = new SimpleDateFormat("dd.MM.yyyy").parse(endDate.getText());
	
			if(module.getText().equals("<Alle Rechnungen>")) {
				table.refresh(start,end,search.getText());
			}
			else {
				table.refresh(module.getText(),start,end,search.getText());
			}
			
		} catch(ParseException e) {
			Perspective.showError(e.getMessage());
		}
	}
	
	/**
	 * Aktualisiert alle eingetragenen Accounts im Select.
	 * @see AccountPreferences
	 */
	public void refreshModule() {
		module.removeAll();
		module.add("<Alle Rechnungen>");
		String name;
		ArrayList<Account> accounts;
		try {
			accounts = API.INSTANCE.getAccounts();
			for(Account acc : accounts) {
				if(acc.getImapAccount() == null) name = acc.getUsername()+" ("+acc.getModule()+")";
				else name = acc.getUsername()+" (Imap)";
				module.add(name);
				if(module.getText().equals("")) module.setText(name);
			}
		} catch (ApiException e) {
			Perspective.showError("Fehler beim Laden der Accounts: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
