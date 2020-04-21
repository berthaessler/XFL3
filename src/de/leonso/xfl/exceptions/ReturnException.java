package de.leonso.xfl.exceptions;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/**
 * kein Fehler im eigentlichen Sinn wird benutzt, um bei @Return() zurueckzunavigieren
 * 
 * @author Bert
 * 
 */
public class ReturnException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	private Data data;

	public ReturnException(XflEngine engine, Expression exp, Data data) {
		super(engine, exp);
		this.data = data;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("return.value", data);
	}
}
