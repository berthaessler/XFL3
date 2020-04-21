package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.exceptions.EvaluationException;

public class BinaryOperatorEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public BinaryOperatorEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		ArrayList<Expression> elements = expression.getElements();
		if (elements.size() == 1) {
			Expression xTemp = elements.get(0);
			Data ev = xTemp.evaluate(rti);
			String text;
			try {
				text = res.addChild(ev).getText();
			} catch (Throwable e) {
				throw new EvaluationException(getEngine(), xTemp, e);
			}
			res.changeFormula(expression.getTitle() + text);
		} else {
			Expression xTemp = elements.get(0);
			// erstmal bis runter hangeln
			while (xTemp.getSubType() == expression.getSubType()) {
				xTemp = xTemp.getElement(0);
			}
			Data dTemp = xTemp.evaluate(rti);
			String text;
			try {
				text = res.addChild(dTemp).getText();
			} catch (Throwable e) {
				throw new EvaluationException(getEngine(), xTemp, e);
			}
			// linke Seite schonmal eintragen
			res.changeFormula(text);
			while (xTemp != expression) {
				xTemp = xTemp.getParent();
				// rechte Seite evaluieren
				dTemp = xTemp.getElement(1).evaluate(rti);
				try {
					text = res.addChild(dTemp).getText();
				} catch (Throwable e) {
					throw new EvaluationException(getEngine(), xTemp, e);
				}
				res.changeFormula(res.getText() + xTemp.getTitle() + text);
				XflEngine engine = getEngine();
				if (engine.isDebugMode()) {
					// Aufrufer startet das auch, aber so gibt's mehr
					// Zwischenergebnisse
					if (xTemp != expression) {
						engine.debug(xTemp, res);
					}
				}
			}
		}
		return res;
	}

}
