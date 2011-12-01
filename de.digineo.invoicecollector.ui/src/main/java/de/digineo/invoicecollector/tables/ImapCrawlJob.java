package de.digineo.invoicecollector.tables;

import java.util.ArrayList;

import javax.mail.Store;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.digineo.invoicecollector.sdk.crawler.Imap;
import de.digineo.invoicecollector.sdk.objects.Account;
import de.digineo.invoicecollector.sdk.objects.ImapAccount;

/**
 * 
 * Job, der von einem bestimmten Account die Rechnungen crawlt.
 * @author jalyna
 *
 */
public class ImapCrawlJob extends Job {

	private ArrayList<Account> accounts;
	private Throwable catchedException;
	
	public ImapCrawlJob(ArrayList<Account> accounts) {
		super("Rechnungen suchen: "+accounts.get(0).getImapAccount().getUsername());
		this.accounts = accounts;
	}

	/**
	 * Sammelt Rechnungen eines Imap-Accounts und gibt den Status zurück.
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask("Rechnungen suchen...", 100); 
		
		try {
			int i = 0;
			Imap imapCrawler = null;
			Store store = null;
			for(Account account : accounts) {
				imapCrawler = new Imap(account,account.getImapAccount(), account.getImapFilter(),store);
				monitor.worked((1/accounts.size())*100*(1/10));
				
				// Login beim ersten mal
				if(i == 0) {
					imapCrawler.login();
					store = imapCrawler.getStore();
				}
				
				imapCrawler.getInvoicesImap();
				monitor.worked((1/accounts.size())*100*(9/10));
				
				i++;
			}
			// Logout am Ende
			if(imapCrawler != null) imapCrawler.logout();
			
			return Status.OK_STATUS;
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
	
	public ImapAccount getAccount() {
		return accounts.get(0).getImapAccount();
	}

}
