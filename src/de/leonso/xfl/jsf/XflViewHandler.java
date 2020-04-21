package de.leonso.xfl.jsf;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.factory.FactoryLookup;

public class XflViewHandler extends com.ibm.xsp.application.ViewHandlerExImpl {
	
    public XflViewHandler(ViewHandler paramHandler) {
        super(paramHandler);
    }

    @SuppressWarnings({ "deprecation" })
    @Override
    public UIViewRoot createView(FacesContext context,
       String paramString) {
        ApplicationEx app = (ApplicationEx) context.getApplication();
        FactoryLookup facts = app.getFactoryLookup();

        XflBindingFactory lsfac = new XflBindingFactory();
        if(facts.getFactory(lsfac.getPrefix()) == null) {
            facts.setFactory(lsfac.getPrefix(), lsfac);
        }

        return super.createView(context, paramString);
    }

}
