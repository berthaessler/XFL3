package de.leonso.xfl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

	public static Object invokeMethod(Object o, String methodName, Object... params) throws Exception {
		Method method = getMethod(o, methodName, params);
		return invokeMethod(o, method, params);
	}

	public static Object invokeMethod(Object o, Method method, Object... params) throws Exception {
		Object[] args = params; // mit Kopie arbeiten
		Object inv;
		try {
			// ggfs. Typen angleichen
			Class<?>[] parameterTypes = method.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Class<?> mClass = parameterTypes[i];
				Object par = args[i];
				if (par == null) {
					// nichts zu tun, einfach als NULL weitergeben
				} else if (!mClass.equals(par.getClass())) {
					if (mClass.equals(Boolean.TYPE) || mClass.equals(Boolean.class)) {
						Number n = (Number) par;
						args[i] = (!n.equals(0));
					} else if (mClass.equals(String[].class) && par.getClass().equals(Object[].class)) {
						Object[] arg = (Object[]) par;
						String[] korr;
						try {
							korr = Arrays.copyOf(arg, arg.length, String[].class);
						} catch (Throwable e) {
							korr = new String[arg.length];
							for (int k = 0; k < arg.length; k++) {
								Object v = arg[k];
								if (v.getClass().equals(Double.class)) {
									Double d = (Double) v;
									korr[k] = String.valueOf(d.intValue());
								} else {
									korr[k] = v.toString();
								}
							}
						}
						args[i] = korr;
					}
				}
			}
			inv = method.invoke(o, args);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			throw (cause == null || !(cause instanceof Exception)) ? e : (Exception) cause;
		}

		return inv;
	}

	public static Method getMethod(Object o, String methodName, Object... params) throws Exception {
		List<Object> arr = new ArrayList<Object>();
		for (Object p : params) {
			arr.add(p);
		}
		Class<?>[] argsClasses = new Class<?>[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			argsClasses[i] = arr.get(i).getClass();
		}
		return getMethod(o.getClass(), methodName, argsClasses);
	}

	public static Method getMethod(Class<?> myclass, String methodName, Class<?>[] parameterClasses) throws Exception {

		Method method = null;
		Method[] declaredMethods = null;
		try {
			declaredMethods = myclass.getDeclaredMethods();
		} catch (SecurityException e1) {
			throw new Exception("can.not.access.declared.methods.in.class " + myclass.getName(), e1);
		} catch (NoClassDefFoundError e) {
			// Fehler trat auf, als class Session unter R8.5 durchsucht wurde
			// und Signatur der Methode
			// die Klasse NotesCalendar enthielt, die es erst seit R9 gibt.
			throw new Exception("Fehler beim Ermitteln der Methoden der Klasse '" + myclass.getName() + "'", e);
		}
		try {
			for (Method m : declaredMethods) {
				// Formeln sind caseinsensitiv
				if (m.getName().equalsIgnoreCase(methodName)) {
					// die Signatur muss aber stimmen...
					// vielleicht sind die Klassen zu konkret
					// z.B. definierte Methode: method(String, Object),
					// Parameter aktuell (String, String)
					Class<?>[] parameterTypes = m.getParameterTypes();
					if (parameterTypes.length == parameterClasses.length) {
						for (int i = 0; i < parameterTypes.length; i++) {
							Class<?> methodClass = parameterTypes[i];
							Class<?> paramClass = parameterClasses[i];
							if (paramClass == null) { // Null-Werte können wir einfach auf die Zielklasse casten
								parameterClasses[i] = methodClass;
							} else {
								if (doClassesMatch(methodClass, paramClass)) {
									parameterClasses[i] = methodClass;
								} else {
									m = null; // die Methode passt schonmal nicht
									break; // wir brauchen nicht weiterzusuchen
								}
							}
						}
						if (m != null) {
							return m;
						}
					}
				}
			}

			if (!Object.class.equals(myclass)) {
				method = getMethod(myclass.getSuperclass(), methodName, parameterClasses);
			}

		} catch (Throwable e) {
		}

		if (method == null) {
			// vielleicht sind die Parameter nur geringfuegig anders,
			// z.B. Nummern statt Boolean
			for (Method m : declaredMethods) {
				// Formeln sind caseinsensitiv
				if (m.getName().equalsIgnoreCase(methodName)) {
					Class<?>[] parameterTypes = m.getParameterTypes();
					if (parameterTypes.length == parameterClasses.length) {
						boolean valid = true;
						for (int i = 0; i < parameterClasses.length; i++) {
							Class<?> valClass = parameterClasses[i];
							Class<?> type = parameterTypes[i];
							if (valClass == null) {
								// Null-Werte können wir einfach auf die
								// Zielklasse casten
							} else if (doClassesMatch(type, valClass)) {
								// OK
							} else if (type.equals(int.class) && valClass.equals(Integer.class)) {
							} else if (type.equals(boolean.class) && valClass.equals(Integer.class)) {
								// kann schon mal als Zahl reinkommen
								// auch OK
							} else if (type.equals(Number.class) && valClass.equals(Integer.class)) {
							} else if (type.equals(String[].class) && valClass.equals(Object[].class)) {
								// Methode erwartet List, Wert ist Array
							} else {
								valid = false;
								break;
							}
						}
						if (valid) {
							return m;
						}
					}
				}
			}
		}
		return method;
	}

	private static Map<String, Boolean> doClassesMatchCache = new HashMap<String, Boolean>();

	private static boolean doClassesMatch(Class<?> class1, Class<?> class2) {

		String key = class1 + "~" + class2;
		Boolean res = doClassesMatchCache.get(key);
		if (res != null) {
			return res;
		}

		if (class2 == null) {
			res = false;
		} else if (class2.equals(class1)) {
			res = true;
		} else if (class2.equals(Object.class)) {
			res = false;
		} else if (doClassesMatch(class1, class2.getSuperclass())) {// naechster Versuch mit Oberklasse
			res = true;
		} else {
			res = false;
			// wir versuchen noch die Interfaces
			Class<?>[] interfaces = class2.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				Class<?> interf = interfaces[i];
				if (doClassesMatch(class1, interf)) {
					res = true;
					break;
				}
			}
		}
		doClassesMatchCache.put(key, res);
		return res;
	}
}
