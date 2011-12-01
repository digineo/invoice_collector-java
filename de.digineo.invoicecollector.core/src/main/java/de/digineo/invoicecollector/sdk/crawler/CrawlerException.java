package de.digineo.invoicecollector.sdk.crawler;

public class CrawlerException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public CrawlerException(Throwable cause){
		super(cause);
	}
	
	public CrawlerException(String msg, Throwable cause){
		super(msg, cause);
	}
	
	public CrawlerException(String msg) {
		super(msg);
	}
	
}
