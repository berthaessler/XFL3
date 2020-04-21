package de.leonso.xfl;

public class SysOutDebugger implements XFLDebugger {

	public SysOutDebugger(XflEngine xflEngine) {
	}

	@Override
	public void debug(Expression exp, Data result) {
		System.out.println(exp + ": " + resultToString(result));
	}

	private String resultToString(Data result) {
		try {
			switch (result.getType()) {
			case OBJECT:
				return result.getValue().toString();

			case NULL:
				return "<null>";

			case UNAVAILABLE:
				return "<UNAVAILABLE>";

			default:
				String v = "";
				Object value = result.getValue();
				if (value == null) {
					return "<null>";
				} else if (value instanceof Object[]) {
					v += "[";
					boolean trenn = false;
					Object[] l = (Object[]) value;
					for (Object object : l) {
						v += (trenn ? ", " : "") + object.toString();
						trenn = true;
					}
					v += "]";
				} else {
					v = value.toString();
				}
				return v;
			}

		} catch (Throwable e) {
			return "<error.resolving.value>";
		}
	}

}
