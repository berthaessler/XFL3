package de.leonso.xfl.exceptions;

import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;

public class CancelException extends EvaluationException {
	private static final long serialVersionUID = 1L;

	public CancelException(Expression exp, Context rti) {
		super(exp.getEngine(), exp);
	}

	private Data data = null;

	// das wird als Ergebnis der Formel zurückgegeben
	public Data getData() {
		if (data == null) {
			data = new Data(getEngine());
			try {
				data.assignValue(-1);// wie in LotusScript
			} catch (Exception ignore) {
			}
		}
		return data;
	}

}
