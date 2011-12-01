package de.digineo.invoicecollector.sdk.objects;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.digineo.invoicecollector.sdk.persistence.ConnectorException;

/**
 * 
 * Stellt eine allgemeines DataObject dar.
 * @author jalyna
 *
 */
public abstract class DataObject {
	
	protected int id;
	protected String tableName;
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static DateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy");
	public static DateFormat dateFormatYear = new SimpleDateFormat("yyyy");
	
	/**
	 * Extrahiert anhand eines Results die Attribute
	 * @param result
	 */
	public abstract void extractFromResult(ResultSet result) throws ConnectorException;
	
	/**
	 * Initialisiert den Tabellenname anhand des Klassennamens
	 * in Kleinbuchstaben.
	 */
	public DataObject() {
		this.tableName = this.getClass().getSimpleName().toLowerCase();
	}
	
	/**
	 * Initialisiert den Tabellenname anhand des Klassennamens
	 * in Kleinbuchstaben. Ruft zusätzlich die Methode extractFormResult auf.
	 */
	public DataObject(ResultSet result) throws ConnectorException {
		this.tableName = this.getClass().getSimpleName().toLowerCase();
		this.extractFromResult(result);
	}
	
	/**
	 * Gibt die ID des Eintrages zurück.
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Setzt die ID.
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
}
