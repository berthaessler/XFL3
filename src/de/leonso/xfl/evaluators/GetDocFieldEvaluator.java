package de.leonso.xfl.evaluators;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import lotus.domino.Document;
import lotus.domino.Item;

public class GetDocFieldEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public GetDocFieldEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		String unid = (String) expression.getElement(0).evaluate(rti).getValue();
		Data field = expression.getElement(1).evaluate(rti);

		Data res = new Data(expression, rti);
		Document refDoc = rti.getRefDoc();
		Document doc = refDoc == null ? null : refDoc.getParentDatabase().getDocumentByUNID(unid);

		if (doc == null) {
			res.setType(DataType.CODE_BOTH);
			res.addChild(field);
			field.convertToVarItem();
			res.setText("@GETDOCFIELD({" + unid + "}; " + field.convertToVarItem().getText() + ")");
		} else {
			Item item = doc.getFirstItem((String) field.getValue());
			if (item == null) {
				// wenn Feld nicht da, dann leeres Ergebnis
				res.assignValue("");
			} else {
				res.assignItem(item);
			}
		}

		return res;

	}

}
