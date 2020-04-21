package de.leonso.xfl.evaluators;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;
import lotus.domino.Document;

public class SetDocFieldEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	public SetDocFieldEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		String unid = (String) expression.getElement(0).evaluate(rti).getValue();
		Data fieldData = expression.getElement(1).evaluate(rti);
		Data valueData = expression.getElement(2).evaluate(rti);

		Data res = new Data(expression, rti);
		Document refDoc = rti.getRefDoc();
		Document doc = refDoc == null ? null : refDoc.getParentDatabase().getDocumentByUNID(unid);

		if (doc == null) {
			res.setType(DataType.CODE_BOTH);
			res.addChild(fieldData);
			fieldData.convertToVarItem();
			res.setText("@SETDOCFIELD({" + unid + "}; " + fieldData.convertToVarItem().getText() + ", " + res.addChild(valueData).convertToVarItem().getText()
					+ ")");
		} else {
			Object value = valueData.getValue();
			doc.replaceItemValue((String) fieldData.getValue(), value);
			doc.save();
			res.assignValue(value);
		}

		return res;

	}

}
