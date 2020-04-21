package de.leonso.xfl.evaluators;

import java.util.ArrayList;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

public class LogicalOperatorEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public LogicalOperatorEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {
		Data res = new Data(expression, rti);
		res.setType(DataType.CODE_BOTH);
		ArrayList<Expression> elements = expression.getElements();
		if (elements.size() == 1) { // NOT-Operator
			Expression xTemp = elements.get(0);
			Data ev = xTemp.evaluate(rti);
			Number value = (Number) res.addChild(ev).getValue();
			res.changeFormula(expression.getTitle() + (value.intValue() == 0 ? "@FALSE" : "@TRUE"));
		} else {
			/*
			 * & (AND), | (OR) Rekursion vermeiden wegen Out Of Stack Space daher versuchen, Ergebnis seriell zu ermitteln
			 */
			Expression xTemp = elements.get(0);
			// erstmal bis runter hangeln
			while ((xTemp.getSubType() == expression.getSubType()) && (xTemp.getTitle().equals("&") || xTemp.getTitle().equals("|"))) {
				xTemp = xTemp.getElement(0);
			}
			res = xTemp.evaluate(rti);
			boolean cond_resolved;
			if (res.getType() != DataType.UNAVAILABLE) {
				String code = res.getText();
				Object value = res.getValue(); // bei @Error-Items = null
				Object ValOhneKomma = value instanceof Number ? ((Number) value).intValue() : code;
				// res.close();
				res = new Data(expression, rti);
				res.setType(DataType.CODE_BOTH);
				res.changeFormula(ValOhneKomma + " | 0"); // Fehler bei falschen
															// Datentypen
															// erzwingen
				cond_resolved = false;

				boolean cond;
				cond = res.isTrue(); // ersten Ausdruck auswerten
				do { // jetzt alle Knoten auswerten
					xTemp = xTemp.getParent(); // das ist der erste log.
												// Operator, könnte auch = this
												// sein !!
					XflEngine engine = getEngine();
					if (cond) { // letzter Operand TRUE?
						if (xTemp.getTitle().equals("|")) { // damit ist alles
															// TRUE, was auch
															// mit OR verknüpft
															// ist
							if (xTemp == expression) { // auf oberster Ebene ist
														// damit alles geklärt
														// a:=b|c;
								cond_resolved = true;
							} else {
								do {
									// Anzeigen, dass sich hier einiges erledigt
									// hat
									if (engine.isDebugMode()) {
										engine.debug(xTemp, res);
									}
									xTemp = xTemp.getParent(); // nach oben
																// hangeln
									if (xTemp == expression) {// schon oben?
										if (expression.getTitle().equals("|")) {
											cond_resolved = true;
										}
									}
								} while (!cond_resolved && xTemp.getTitle().equals("|")); // alle
																							// ORs
																							// abhaken
							} // xTemp == expression
						} // Title == |
					} else if (!cond_resolved) { // temp. Ausdruck false
						if (xTemp.getTitle().equals("&")) { // damit ist alles
															// FALSE, was sonst
															// noch mit &
															// verknüpft ist
							if (xTemp == expression) { // auf oberster Ebene ist
														// damit alles geklärt
								cond_resolved = true;
							} else {
								do {
									// Anzeigen, dass sich hier einiges erledigt
									// hat
									if (engine.isDebugMode()) {
										engine.debug(xTemp, res);
									}
									xTemp = xTemp.getParent(); // nach oben
																// hangeln
									if (xTemp == expression) {// schon oben?
										if (expression.getTitle().equals("&")) {
											cond_resolved = true;
										}
									}
								} while (!cond_resolved && xTemp.getTitle().equals("&")); // alle
																							// ANDs
																							// abhaken
							}
						} // Title = &
					}

					if (!cond_resolved) {
						res = xTemp.getElement(1).evaluate(rti);
						if (res.getType() != DataType.UNAVAILABLE) {
							res.convertToVarItem();
							value = res.getValue();
							ValOhneKomma = value instanceof Number ? ((Number) value).intValue() : code;
							// res.close();
							res = new Data(expression, rti);
							res.setType(DataType.CODE_BOTH);
							// Fehler bei falschen Datentypen erzwingen
							res.changeFormula(ValOhneKomma + " | 0");
						}
						cond = res.isTrue();
						if ((xTemp != expression) && (engine.isDebugMode())) {
							/*
							 * nur wenn wir gerade in einem Unterknoten sind
							 */
							engine.debug(xTemp, res);
						}
					}
				} while ((xTemp != expression) && !cond_resolved && (res.getType() != DataType.UNAVAILABLE));

			} else {
				cond_resolved = true;
			}

			if (res.getType() == DataType.UNAVAILABLE) {
				// v2.93, in SFL kommt hier
				// "incorrect data type for operator: number expected"
				res.changeFormula("\"\"/1"); // ' das wirft auch so einen Fehler
			}
		}

		return res;
	}

}
