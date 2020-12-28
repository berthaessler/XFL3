package de.leonso.xfl.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodNotFoundException;
import javax.script.ScriptException;

import com.ibm.xsp.binding.MethodBindingEx;

public class XflMethodBinding extends MethodBindingEx {
	private String data;

	public XflMethodBinding() {
		super();
	}

	public XflMethodBinding(String expr) {
		super();
		this.data = expr;
	}

	@Override
	public Object invoke(FacesContext context, Object[] obj) throws EvaluationException, MethodNotFoundException {
		try {
			return XflUtilsJsf.getXflEngine().eval(data);
		} catch (ScriptException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public Class<?> getType(FacesContext context) throws MethodNotFoundException {
		return null;
	}

}
