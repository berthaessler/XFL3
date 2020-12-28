package de.leonso.xfl.jsf;

import javax.faces.application.Application;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.binding.BindingFactory;
import com.ibm.xsp.util.ValueBindingUtil;

public class XflBindingFactory implements BindingFactory {

	public String getPrefix() {
		return "xfl";
	}

	public MethodBinding createMethodBinding(Application app, String expr, @SuppressWarnings("rawtypes") Class[] obj) {
		String script = ValueBindingUtil.parseSimpleExpression(expr);
		return new XflMethodBinding(script);
	}

	public ValueBinding createValueBinding(Application app, String expr) {
		String script = ValueBindingUtil.parseSimpleExpression(expr);
		return new XflValueBinding(script);
	}

}
