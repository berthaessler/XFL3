package de.leonso.xfl.evaluators;

import java.util.ArrayList;
import java.util.List;

import de.leonso.xfl.Data;
import de.leonso.xfl.DataType;
import de.leonso.xfl.Expression;
import de.leonso.xfl.Context;
import de.leonso.xfl.XflEngine;

/**
 * Ausfuehrung des Ausdrucks via Notes-Evaluate
 * 
 * @author Bert
 *
 */
public class NotesFormulaEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;

	// Funktionen, die direkt auf dem uebergebenen doc ausgefuehrt werden
	// muessen
	private static List<String> refDocFunctions = null;

	public NotesFormulaEvaluator(XflEngine engine) {
		super(engine);
		if (refDocFunctions == null) {
			refDocFunctions = new ArrayList<String>();

			String[] split = ("MAILSEND,DBLOOKUP,DBCOLUMN,DBCOMMAND,SETPROFILEFIELD,GETPROFILEFIELD,"
					+ "ADDTOFOLDER,Authors,DOCUMENTUNIQUEID,CREATED,MODIFIED,ACCESSED,"

					+ "ATTACHMENTLENGTH,ATTACHMENTNAMES,ATTACHMENTS," // direkte
																		// Funktionen
																		// (parameterlos)
					+ "DocFields,DocLength,HardDeleteDocument,NoteID," + "UndeleteDocument,UserAcces,V4UserAccess,UserPrivileges,UserRoles").split(",");

			for (String f : split) {
				refDocFunctions.add(f.toUpperCase());
				refDocFunctions.add("@" + f.toUpperCase()); // auch mit @
															// vorneweg
			}
		}
	}

	@Override
	public Data evaluate(Expression expression, Context rti) throws Exception {

		ArrayList<Expression> elements = expression.getElements();
		String title = expression.getTitle();

		Data res = new Data(expression, rti);
		res.setType(refDocFunctions.contains(title) ? DataType.CODE_REF : DataType.CODE_BOTH);

		StringBuilder sb = new StringBuilder();

		// man kann auch alle @ weglassen. Dann muessen sie aber spaetestens
		// hier wieder ran
		if (elements.size() > 0 && !title.startsWith("@")) {
			sb.append("@");
		}
		sb.append(title);

		int i = 0;
		for (i = 0; i < elements.size(); i++) {
			Expression e = elements.get(i);
			if (i > 0) {
				sb.append(" ; ");
			} else {
				sb.append("(");
			}
			Data dTemp = e.evaluate(rti);
			dTemp = res.addChild(dTemp); // evtl. wird dabei Kopie erzeugt
			sb.append(dTemp.getText()); // Bei TRUE wird alles als Text geholt
		}
		if (i > 0) {
			sb.append(")");
		}
		res.setText(sb.toString());

		return res;

	}

}
