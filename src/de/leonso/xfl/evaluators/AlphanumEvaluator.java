package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.EvaluatorNotFoundException;
import de.leonso.xfl.exceptions.ReturnException;
import lotus.domino.Document;

public class AlphanumEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public AlphanumEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		Data res = null;
		Document refDoc = null;
		ArrayList<Expression> elements = expression.getElements();
		String title = expression.getTitle();
		XflEngine engine = getEngine();

		if (expression.isObject()) {
			if (expression.isGlobal()) {
				if (engine.hasGlobalObject(title)) {
					res = engine.getGlobalObjectWrapper(title);
				}
			} else if (rti.hasObject(title)) {
				res = rti.getObject(title);
			} else if (engine.hasGlobalObject(title)) {
				res = engine.getGlobalObjectWrapper(title);
			}
			if (res != null) {
				return res;
			}
		}
		// kein Objekt
		if (!expression.isOriginal() && engine.isAliasDefined(title)) {
			String save = title;
			expression.setTitle(engine.getAlias(title)); // alias eintragen
			res = expression.evaluate(rti);
			expression.setTitle(save); // Urzustand herstellen
			return res;
		}

		switch (expression.getSubType()) {
		case NUMBER:
		case STRING:
			return evaluateConstant(expression, rti);

		case FUNCTION:
			if (!expression.isOriginal() && engine.isUDFDefined(title, elements.size())) {
				Expression udf = engine.getUDF(title, elements.size());
				Expression tmpRoot = udf.getElement(1);
				Context tmpRti = engine.createContext(tmpRoot, rti.getRefDoc());
				// Parameter uebergeben
				if (elements.size() > 0) {
					for (int i = 0; i < elements.size(); i++) {
						Expression exp = elements.get(i);
						res = exp.evaluate(rti);
						String name = udf.getElement(0).getElement(i).getTitle();
						if (res.getType() == DataType.OBJECT) {
							tmpRti.setObject(name, res);
						} else {
							tmpRti.setVar(name, res);
						}
					} // elements
				}
				try {
					try {
						Data tmpRes = tmpRoot.evaluate(tmpRti);
						res = new Data(expression, rti);
						res.assignItem(tmpRes.getItem()); // in aktuelle RTI
															// umspeichern
					} catch (ReturnException e) {
						res = e.getData();
					}
				} finally {
					tmpRti.removeAllVars();
					tmpRti.close();
				}
				return res;
			} else {
				// passenden Evaluator suchen
				Evaluator ev = engine.getEvaluator(title, elements.size());
				if (ev == null) {
					throw new EvaluatorNotFoundException(getEngine(), title);
				}

				try {
					return ev.evaluate(expression, rti);
				} catch (EvaluationException e) {
					throw e;
				} catch (Throwable e) {
					throw new EvaluationException(getEngine(), expression, e);
				}

			}

		case VAR:
			if (expression.isUndefine()) {
				if (engine.isUDFDefined(title, elements.size())) {
					engine.removeUDF(title, elements.size());
				}
				res = new Data(expression, rti);
				res.setType(DataType.UNAVAILABLE);
				return res;
			} else if (expression.isGlobal()) {
				if (engine.hasGlobalVar(title)) {
					return engine.getGlobalVar(title);
				} else {
					res = new Data(expression, rti);
					res.setType(DataType.UNAVAILABLE);
					return res;
				}

			} else if (expression.isField()) {
				refDoc = rti.getRefDoc();
				if (refDoc != null) {
					if (refDoc.hasItem(title)) {
						res = new Data(expression, rti);
						res.assignItem(refDoc.getFirstItem(title));
						return res;
					}
				}
			} else if (expression.isEnvironment()) {
				res = new Data(expression, rti);
				res.assignValue(engine.getSession().getEnvironmentValue(title));
				return res;
			} else {
				if (!expression.isOriginal() && engine.isUDFDefined(title, elements.size())) {
					// Neue Instanz des Funktionsbaums
					Expression udf = engine.getUDF(title, elements.size());
					Expression tmpRoot = udf.getElement(1);
					Context tmpRti = engine.createContext(tmpRoot, rti.getRefDoc());
					try {
						try {
							Data tmpRes = tmpRoot.evaluate(tmpRti);
							res = new Data(expression, rti);
							res.assignItem(tmpRes.getItem()); // umspeichern in
																// aktuelle RTI
						} catch (ReturnException e) {
							res = e.getData();
						}
					} finally {
						tmpRti.removeAllVars();
						tmpRti.close();
					}
					return res;
				}

				Evaluator ev = engine.getEvaluator(title, 0); // var kann keine
																// Parameter
																// haben

				if (ev != null) {
					return ev.evaluate(expression, rti);
				} else if (rti.hasVar(title)) {
					return rti.getVar(title);
				} else if (rti.hasObject(title)) {
					return rti.getObject(title);
				} else if (engine.hasGlobalVar(title)) {
					Data globalVar = engine.getGlobalVar(title);
					// Achtung kann auch versehentlich ein OBJECT sein!
					// switch (globalVar.getType()) {
					// case ITEM_VAR:
					// Wir kopieren diese Variable in den lokalen
					// Variablenspeicher
					// res = new Data(expression, rti);
					// res.assignItem(globalVar.getItem());
					// return res;
					// default:
					return globalVar;
					// }
				} else if (engine.hasGlobalObject(title)) {
					return engine.getGlobalObjectWrapper(title);
				}
				refDoc = rti.getRefDoc();
				if (refDoc != null) {
					if (refDoc.hasItem(title)) {
						res = new Data(expression, rti);
						res.assignItem(refDoc.getFirstItem(title));
						return res;
					}
				}

			}

			// weder Formel noch Feld
			res = new Data(expression, rti);
			res.setType(DataType.UNAVAILABLE);
			return res;

		default:
			throw new EvaluatorNotFoundException(getEngine(), expression);
		}

	}

	private Data evaluateConstant(Expression expression, Context rti) throws EvaluationException {
		Data res = new Data(expression, rti);
		String str = expression.getTitle();
		res.assignCodeConstant(DataType.CODE_BOTH, str);
		return res;
	}

}
