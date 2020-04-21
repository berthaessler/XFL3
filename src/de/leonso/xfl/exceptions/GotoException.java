package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/**
 * kein Fehler, dient der Uebertragung von Sprungmarken 
 * 
 * @author Bert
 *
 */
public class GotoException extends EvaluationException {
	private static final long serialVersionUID = 1L;
	
	private final String label;
	
	public GotoException(XflEngine engine, Expression expression, String label) {
		super(engine, expression);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
