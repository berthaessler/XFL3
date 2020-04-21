package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.CancelException;
import de.leonso.xfl.exceptions.GotoException;
import de.leonso.xfl.exceptions.LabelNotDefinedException;
import de.leonso.xfl.exceptions.ReturnException;

public class RootEvaluator extends BlockEvaluator {
	private static final long serialVersionUID = 1L;

	public RootEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		try {
			return super.evaluate(expression, rti);
		} catch (CancelException e) {
			return e.getData();
		} catch (ReturnException e) {
			return e.getData();
		}

	};

	/**
	 * Fehler verschachteln, damit der Stack aussagekräftiger wird.<br>
	 * Im Root nicht!
	 * 
	 * @return
	 */
	@Override
	protected boolean isEncapsulateEvaluationException() {
		return false;
	}

	@Override
	protected Data evalAndCheckGoto(Expression expression, int currentPos, ArrayList<Expression> elements, Data temp, Context rti) throws Exception {

		try {
			return super.evalAndCheckGoto(expression, currentPos, elements, temp, rti);
		} catch (GotoException e) {
			// wenn wir hier rauskommen, wurde nirgendwo das Label gefunden
			String label = e.getLabel();
			throw new LabelNotDefinedException(getEngine(), expression, label);
		}
	}
}
