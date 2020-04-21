package de.leonso.xfl.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.script.ScriptException;

import com.ibm.xsp.binding.ValueBindingEx;

public class XflValueBinding extends ValueBindingEx {
	private String data;

	public XflValueBinding(String data) {
		this.data = data;
	}

	@Override
	public Object getValue(FacesContext context) throws EvaluationException, PropertyNotFoundException {
		try {
			return XflUtilsJsf.getXflEngine().eval(data);
		} catch (ScriptException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public void setValue(FacesContext context, Object obj) throws EvaluationException, PropertyNotFoundException {
	}

	@Override
	public boolean isReadOnly(FacesContext context) throws EvaluationException, PropertyNotFoundException {
		return true;
	}

	@Override
	public Class getType(FacesContext context) throws EvaluationException, PropertyNotFoundException {
		return Object.class;
	}
}
