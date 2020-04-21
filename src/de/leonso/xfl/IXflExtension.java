package de.leonso.xfl;

import java.io.Serializable;

public interface IXflExtension extends Serializable {

	public abstract String getFunctionNames();

	public abstract Object evaluate(Context context, String formulaName, Object... args) throws Exception;

}
