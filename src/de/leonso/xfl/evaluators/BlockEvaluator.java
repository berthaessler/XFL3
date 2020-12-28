package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.CancelException;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.GotoException;
import de.leonso.xfl.exceptions.LabelNotDefinedException;
import de.leonso.xfl.exceptions.ReturnException;

/**
 * Evaluator fuer @Do(), Root ...
 * 
 * @author Bert
 *
 */
public class BlockEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public BlockEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {

		ArrayList<Expression> elements = expression.getElements();

		Data temp = new Data(expression, context);
		temp.setType(DataType.CODE_BOTH);

		Data res = null;

		String label = getEngine().getSearchLabel();
		int iStart = 0; // evtl. Vorbelegung durch laufenden GOTO-Befehl
		if (label != null) {
			if (expression.hasLabel(label)) {
				iStart = expression.getLabelPosition(label) + 1; // instruction
																	// pointer
																	// vorbelegen
				getEngine().setSearchLabel(null);
			} else {
				throw new LabelNotDefinedException(getEngine(), expression, label);
			}
		}

		res = processBlock(expression, elements, temp, iStart, context);

		return res;
	}

	protected Data processBlock(Expression expression, ArrayList<Expression> elements, Data defaultData, int iStart, Context context) throws Exception {

		Data res = null;

		for (int i = iStart; i < elements.size(); i++) {

			try {
				res = evalAndCheckGoto(expression, i, elements, defaultData, context);
				// wir muessen sicherstellen, dass auch wirklich die Formel
				// ausgefuehrt wird und nicht bloss der Code erzeugt wird
				res.convertToVarItem();
			} catch (InstructionPointerChangedException e) {
				i = e.getNewInstructionPointer(); // instruction pointer
													// umsetzen
			}

		}
		return res;
	}

	protected Data evalAndCheckGoto(Expression expression, int currentPos, ArrayList<Expression> elements, Data temp, Context context) throws Exception {

		Data res;
		Expression exp = elements.get(currentPos);

		try {
			res = exp.evaluate(context);
			// temp.addChild(res);

		} catch (GotoException e) {
			String label = e.getLabel();
			if (expression.hasLabel(label)) {
				// instruction pointer umsetzen
				throw new InstructionPointerChangedException(expression.getLabelPosition(label));
			}
			// an uebergeordneten Ausdruck weitergeben
			throw e;
		} catch (CancelException e) {
			throw e;
		} catch (ReturnException e) {
			throw e;
		} catch (EvaluationException e) {
			// im Stacktrace die Formelbloecke schrittweise anzeigen
			// wenn wir aber schon im Root sind, nicht
			if (isEncapsulateEvaluationException()) {
				throw new EvaluationException(getEngine(), expression, e);
			} else {
				throw e;
			}
		}

		return res;
	}

	/**
	 * Fehler verschachteln, damit der Stack aussagekräftiger wird.
	 * 
	 * @return
	 */
	protected boolean isEncapsulateEvaluationException() {
		return true;
	}

	protected class InstructionPointerChangedException extends Exception {
		private static final long serialVersionUID = 1L;

		private final int index;

		public int getNewInstructionPointer() {
			return index;
		}

		public InstructionPointerChangedException(int index) {
			super();
			this.index = index;
		}

	}
}
