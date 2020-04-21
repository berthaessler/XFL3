package de.leonso.xfl.evaluators;

import java.io.Serializable;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public abstract class Evaluator implements Serializable {
	private static final long serialVersionUID = 1L;

	private final XflEngine engine;

	public Evaluator(XflEngine engine) {
		this.engine = engine;
	}

	public XflEngine getEngine() {
		return engine;
	}

	public abstract Data evaluate(Expression expression, Context rti) throws Exception;

	/**
	 * Routine zum Aufreaumen
	 * 
	 * @throws Exception
	 */
	public void close() throws Exception {
		// hier leer, kann ueberschrieben werden
	}

}
