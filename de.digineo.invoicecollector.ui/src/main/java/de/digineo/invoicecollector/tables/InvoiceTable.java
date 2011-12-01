package de.digineo.invoicecollector.tables;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import de.digineo.invoicecollector.Activator;
import de.digineo.invoicecollector.Perspective;
import de.digineo.invoicecollector.sdk.API;
import de.digineo.invoicecollector.sdk.ApiException;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.Invoice;
import de.digineo.invoicecollector.views.FilterView;

public class InvoiceTable extends ViewPart {

	public static final String ID = "de.digineo.invoicecollector.InvoiceTable";
	public static final Image PDF = Activator.getImageDescriptor(
			"icons/icons/document-pdf-text.png").createImage();
	public static final DateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");

	private org.eclipse.swt.widgets.Table table;
	private TableViewer tableViewer;
	private ArrayList<Invoice> invoices = new ArrayList<Invoice>();
	/** Aktuelle Summe */
	private Double sum = 0.0;

	/**
	 * Registriert einen JobChangeListener, um ggf. gespeicherte Exceptions in
	 * die UI zu geben.
	 */
	public InvoiceTable() {
		Job.getJobManager().addJobChangeListener(new IJobChangeListener() {

			@Override
			public void aboutToRun(IJobChangeEvent arg0) {
			}

			@Override
			public void awake(IJobChangeEvent arg0) {
			}

			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult() == Status.CANCEL_STATUS
						&& event.getJob() instanceof CrawlJob) {
					final CrawlJob crawlJob = (CrawlJob) event.getJob();

					UIJob uiJob = new UIJob("Rechnungen speichern...") {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							Perspective
									.showError("Fehler beim laden der Rechnungen von "
											+ crawlJob.getAccount()
													.getUsername()
											+ " "
											+ "("
											+ crawlJob.getAccount().getModule()
											+ "): "
											+ crawlJob.getCatchedException()
													.getMessage());
							return Status.OK_STATUS;
						}

					};

					uiJob.schedule();
				} else if (event.getResult() == Status.CANCEL_STATUS
						&& event.getJob() instanceof ImapCrawlJob) {
					final ImapCrawlJob crawlJob = (ImapCrawlJob) event.getJob();

					UIJob uiJob = new UIJob("Rechnungen speichern...") {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							Perspective
									.showError("Fehler beim laden der Rechnungen von "
											+ crawlJob.getAccount()
													.getUsername()
											+ " "
											+ "(Imap): "
											+ crawlJob.getCatchedException()
													.getMessage());
							return Status.OK_STATUS;
						}

					};

					uiJob.schedule();
				}
			}

			@Override
			public void running(IJobChangeEvent arg0) {
			}

			@Override
			public void scheduled(IJobChangeEvent arg0) {
			}

			@Override
			public void sleeping(IJobChangeEvent arg0) {
			}

		});
	}

	@Override
	public void createPartControl(Composite parent) {

		// Tabelle erstellen
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.PUSH);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// dafür sorgen, dass der ausgewählte Eintrag von anderen Views
		// abgerufen werden kann
		getSite().setSelectionProvider(tableViewer);

		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		table.setMenu(menuManager.createContextMenu(table));

		getSite().registerContextMenu("de.digineo.tablemenu", menuManager,
				tableViewer);
		getSite().setSelectionProvider(tableViewer);

		TableViewerColumn pdfViewerColumn = new TableViewerColumn(tableViewer,
				SWT.CENTER);
		TableColumn pdfColumn = pdfViewerColumn.getColumn();
		pdfColumn.setResizable(false);
		pdfColumn.setWidth(50);
		pdfColumn.setText(" ");
		pdfColumn.setAlignment(SWT.CENTER);

		TableViewerColumn moduleViewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn moduleColumn = moduleViewerColumn.getColumn();
		moduleColumn.setResizable(false);
		moduleColumn.setWidth(200);
		moduleColumn.setText("Modul");

		// Nach Modul sotieren
		moduleColumn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<Invoice> comp = new Comparator<Invoice>() {

					@Override
					public int compare(Invoice o1, Invoice o2) {
						return o1.getAccount().getModule()
								.compareTo(o2.getAccount().getModule());
					}

				};
				Collections.sort(invoices, comp);
				tableViewer.setInput(invoices);

			}

		});

		TableViewerColumn numberViewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn numberColumn = numberViewerColumn.getColumn();
		numberColumn.setResizable(false);
		numberColumn.setWidth(200);
		numberColumn.setText("RechnungsNr.");

		// Nach Rechnungsnr. sortieren
		numberColumn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<Invoice> comp = new Comparator<Invoice>() {

					@Override
					public int compare(Invoice o1, Invoice o2) {
						return o1.getNumber().compareTo(o2.getNumber());
					}

				};
				Collections.sort(invoices, comp);
				tableViewer.setInput(invoices);

			}

		});

		TableViewerColumn dateViewerColumn = new TableViewerColumn(tableViewer,
				SWT.NONE);
		TableColumn dateColumn = dateViewerColumn.getColumn();
		dateColumn.setResizable(false);
		dateColumn.setWidth(200);
		dateColumn.setText("Datum");

		// Nach Datum sortieren
		dateColumn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<Invoice> comp = new Comparator<Invoice>() {

					@Override
					public int compare(Invoice o1, Invoice o2) {
						if (o1.getDate().after(o2.getDate()))
							return 1;
						return -1;
					}

				};
				Collections.sort(invoices, comp);
				tableViewer.setInput(invoices);

			}

		});

		TableViewerColumn amountViewerColumn = new TableViewerColumn(
				tableViewer, SWT.RIGHT);
		TableColumn amountColumn = amountViewerColumn.getColumn();
		amountColumn.setResizable(false);
		amountColumn.setWidth(150);
		amountColumn.setText("Betrag");

		// Nach Betrag sortieren
		amountColumn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Comparator<Invoice> comp = new Comparator<Invoice>() {

					@Override
					public int compare(Invoice o1, Invoice o2) {
						if (o1.getAmount() > o2.getAmount())
							return 1;
						return -1;
					}

				};
				Collections.sort(invoices, comp);
				tableViewer.setInput(invoices);

			}

		});

		tableViewer.setContentProvider(new InvoiceTableContentProvider());
		tableViewer.setLabelProvider(new InvoiceTableLabelProvider());

		// Pdf-Datei bei Doppelklick öffnen.
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {

				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				Invoice invoice = (Invoice) selection.getFirstElement();

				invoice.openFile();

			}
		});

		refresh();

	}

	@Override
	public void setFocus() {
	}

	/**
	 * Kalkuliert anhand der geladenen Rechnungen die Gesamtsumme und gibt sie
	 * unter dem Viewnamen aus.
	 */
	private void calcSum() {
		sum = 0.0;
		for (Invoice invoice : invoices) {
			if (invoice.getAmount() != null)
				sum += invoice.getAmount();
		}
		sum = Math.round(sum * 100) / 100.;
		setPartName("Rechnungen (" + invoices.size() + ") | "
				+ API.getEuro(sum) + " gesamt");
	}

	/**
	 * Lädt alle Rechnungen in die Tabelle
	 */
	public void refresh() {
		try {
			invoices = Activator.api.getInvoices();
			tableViewer.setInput(invoices);
		} catch (ApiException e) {
			Perspective.showError("Anfragen konnte nicht abgerufen werden:\n"
					+ e.getMessage());
			e.printStackTrace();
		}
		calcSum();
	}

	/**
	 * Sucht anhand von einem ausgewählten Account, einem Start- und einem
	 * Enddatum alle Rechungen.
	 * 
	 * @param filter
	 *            Account in der Form "Modul (Benutzername)"
	 * @param start
	 *            Startdatum
	 * @param end
	 *            Enddatum
	 * @param search
	 *            Suchstring, Infix der Rechnungsnummer.
	 */
	public void refresh(String filter, Date start, Date end, String search) {
		try {
			int moduleStart = filter.indexOf('(');
			int moduleEnd = filter.indexOf(')');
			String module = filter.substring(moduleStart + 1, moduleEnd);
			String username = filter.substring(0, moduleStart - 1);
			if (search.equals("") || search.equals(FilterView.DEFAULT_TEXT)) {
				invoices = Activator.api.getInvoices(module, username,
						isoDate.format(start), isoDate.format(end));
			} else {
				invoices = Activator.api.getInvoices(module, username,
						isoDate.format(start), isoDate.format(end), search);
			}
			tableViewer.setInput(invoices);
		} catch (ApiException e) {
			Perspective.showError("Anfragen konnte nicht abgerufen werden:\n"
					+ e.getMessage());
			e.printStackTrace();
		}
		calcSum();
	}

	/**
	 * Sucht über alle Accounts hinweg Rechnungen mit einem Suchstring, die
	 * zwischen einem Start- und einem Enddatum liegen.
	 * 
	 * @param start
	 *            Startdatum
	 * @param end
	 *            Enddatum
	 * @param search
	 *            Suchstring, Infix der Rechnungsnr.
	 */
	public void refresh(Date start, Date end, String search) {
		try {
			if (search.equals("") || search.equals(FilterView.DEFAULT_TEXT)) {
				invoices = Activator.api.getInvoices(isoDate.format(start),
						isoDate.format(end));
			} else {
				invoices = Activator.api.getInvoices(isoDate.format(start),
						isoDate.format(end), search);
			}
			tableViewer.setInput(invoices);
		} catch (ApiException e) {
			Perspective.showError("Anfragen konnte nicht abgerufen werden:\n"
					+ e.getMessage());
			e.printStackTrace();
		}
		calcSum();
	}

	/**
	 * Lädt alle Rechnungwn zwischen einem Start- und Enddatum.
	 * 
	 * @param start
	 *            Startdatum
	 * @param end
	 *            Enddatum
	 */
	public void refresh(Date start, Date end) {
		try {
			invoices = Activator.api.getInvoices(isoDate.format(start),
					isoDate.format(end));
			tableViewer.setInput(invoices);
		} catch (ApiException e) {
			Perspective.showError("Anfragen konnte nicht abgerufen werden:\n"
					+ e.getMessage());
			e.printStackTrace();
		}
		calcSum();
	}

	public void crawlInvoices() {
		crawlInvoices(null);
	}

	/**
	 * Crawlt von einem bestimmten oder allen Accounts neue Rechnungen. Dabei
	 * wird pro Account ein neuer Job angelegt.
	 * 
	 * @param account
	 *            (Optional, wenn null dann alle)
	 */
	public void crawlInvoices(Account account) {

		if (account != null) {
			createJob(account);
			return;
		}

		Job job = new Job("Laden der Accounts") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Accounts laden...", 100);
				HashMap<Integer, ArrayList<Account>> imapAccounts = new HashMap<Integer, ArrayList<Account>>();

				try {
					monitor.worked(10);
					ArrayList<Account> accounts = API.INSTANCE.getAccounts();
					monitor.worked(100);
					done(Status.OK_STATUS);

					for (Account acc : accounts) {
						if (acc.getImapAccount() == null)
							createJob(acc);
						else {
							if (!imapAccounts.containsKey(acc.getImapAccount()
									.getId()))
								imapAccounts.put(acc.getImapAccount().getId(),
										new ArrayList<Account>());
							imapAccounts.get(acc.getImapAccount().getId()).add(
									acc);
						}
					}

					for (ArrayList<Account> acc : imapAccounts.values()) {
						ImapCrawlJob job = new ImapCrawlJob(acc);
						job.setUser(true);
						job.schedule();
					}

				} catch (ApiException e2) {
					e2.printStackTrace();
				}

				return Status.OK_STATUS;
			}

		};

		job.setUser(true);
		job.schedule();

	}

	/**
	 * Erstellt anhand eines Accounts einen neuen CrawlJob.
	 * 
	 * @param acc
	 *            Account
	 */
	private void createJob(final Account acc) {
		CrawlJob job = new CrawlJob(acc);
		job.setUser(true);
		job.schedule();
	}

}
