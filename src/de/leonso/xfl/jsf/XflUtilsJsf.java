package de.leonso.xfl.jsf;

import de.leonso.core.ui.jsf.UtilsJsf;
import de.leonso.xfl.XflEngine;
import lotus.domino.Database;

public class XflUtilsJsf {

	public static XflEngine getXflEngine() {
		XflEngine xflEngine = null;
		try {
			xflEngine = (XflEngine) UtilsJsf.resolveVariable("xflEngine");
		} catch (Exception ignore) {
		}
		if (xflEngine == null) {
			try {
				xflEngine = (XflEngine) UtilsJsf.getViewScope().get("xflEngine");
			} catch (Exception ignore) {
			}
		}
		if (xflEngine != null) {
			// lebt die Datenbank noch?
			try {
				Database currentDatabase = xflEngine.getCurrentDatabase();
				currentDatabase.isOpen();
			} catch (Throwable e) {
				try {
					xflEngine.close();
				} catch (Exception ignore) {
				}
				UtilsJsf.getViewScope().remove("xflEngine");
				xflEngine = null;
			}
		}
		if (xflEngine == null) {
			xflEngine = new de.leonso.xfl.jsf.XflJsfEngine();
			UtilsJsf.getViewScope().put("xflEngine", xflEngine);
		}
		return xflEngine;
	}

}
