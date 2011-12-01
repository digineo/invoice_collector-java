package de.digineo.invoicecollector.sdk.crawler;

public class LoginException extends Exception {

	private static final long serialVersionUID = 1L;

	public LoginException() {
		this("Login fehlgeschlagen");
	}
	
	public LoginException(String msg) {
		super(msg);
	}
	
}
