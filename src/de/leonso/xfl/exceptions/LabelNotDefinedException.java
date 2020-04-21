package de.leonso.xfl.exceptions;

import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/**
 * wird geworfen, wenn ein ungueltiges Label angesprungen werden soll
 * 
 * @author Bert
 *
 */
public class LabelNotDefinedException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	private final String label;

	public LabelNotDefinedException(XflEngine engine, Expression expression, String label) {
		super(engine, expression);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String getMessage() {
		return getEngine().getMessage("label.not.defined", label);
	}
	
}
