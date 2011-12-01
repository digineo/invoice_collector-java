package de.digineo.invoicecollector.tables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.digineo.invoicecollector.sdk.crawler.Crawler;
import de.digineo.invoicecollector.sdk.objects.Account;

/**
 * 
 * Job, der von einem bestimmten Account die Rechnungen crawlt.
 * @author jalyna
 *
 */
public class CrawlJob extends Job {

	private Account account;
	private Throwable catchedException;
	
	public CrawlJob(Account account) {
		super((account.getImapAccount() == null ? "Rechnungen suchen: "+account.getUsername() + " ("+account.getModule()+")" : "Rechnungen suchen: "+account.getUsername()));
		this.account = account;
	}

	/**
	 * Sammelt Rechnungen und gibt den Status zurück.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask("Rechnungen suchen...", 100);

		try {
			Class<?> cl = Class.forName(Crawler.PACKAGE_ID+"."+account.getModule()); 
			Constructor<?> construct = cl.getConstructor(Class.forName("java.lang.String"), Class.forName("java.lang.String"), Class.forName("de.digineo.invoicecollector.sdk.objects.Account"));
			Method method = cl.getMethod("getInvoices");
			Crawler object = (Crawler) construct.newInstance(account.getUsername(), account.getPassword(), account);
			monitor.worked(10);
			method.invoke(object);
			monitor.worked(90);
			return Status.OK_STATUS;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			catchedException = e.getCause();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			catchedException = e;
		} catch (Exception e) {
			e.printStackTrace();
			catchedException = e;
		} 
		
		return Status.CANCEL_STATUS;
	}

	/**
	 * Gibt die geworfene Exception zurück.
	 * @return
	 */
	public Exception getCatchedException() {
		return (Exception) catchedException;
	}
	
	/**
	 * Gibt den zugehörigen Account zurück.
	 * @return
	 */
	public Account getAccount() {
		return account;
	}
}
