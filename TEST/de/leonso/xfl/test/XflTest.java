package de.leonso.xfl.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import de.leonso.core.notes.LINotesFactoryProvider;
import de.leonso.core.notes.LNativeNotesSessionProvider;
import de.leonso.core.notes.LSessionProviderNotesExtern;
import de.leonso.core.notes.LSessionProviderNotesJsf;
import de.leonso.core.notes.api.NotesFactory;
import de.leonso.core.notes.api.SessionWrapper;
import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;
import de.leonso.xfl.XflEngineManager;
import de.leonso.xfl.XflExtension;
import de.leonso.xfl.exceptions.CompilerException;
import de.leonso.xfl.exceptions.ErrorException;
import de.leonso.xfl.exceptions.EvaluationException;
import de.leonso.xfl.exceptions.LabelNotDefinedException;
import de.leonso.xfl.exceptions.ObjectVariableNotSetException;
import de.leonso.xfl.exceptions.SubscriptOutOfRangeException;
import de.leonso.xfl.jsf.XflJsfEngine;
import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.RichTextItem;
import lotus.domino.Session;

public class XflTest {
	/* @formatter:off */
	
	static XflEngine engine = null;

	@Rule
	public TestWatcher watcher = new TestWatcher() {
		@Override
		public void starting(Description description) {
			System.out
			.printf("----- %s() -----\n", description.getMethodName());
		}
	};

	static Document refDoc;
	
	
	@BeforeClass
	public static void setup() throws Exception {
		
		class XflTestNotesFactoryProvider implements LINotesFactoryProvider {
			private static final long serialVersionUID = 1L;
			
			private LSessionProviderNotesExtern sessionProvider = new LSessionProviderNotesExtern("Server2", "leonso\\beispiel.nsf");
			
			@Override
			public SessionWrapper getSession() {
				return getNotesFactory().getSession();
			}
			
			@Override
			public SessionWrapper getSessionAsSigner() {
				return getNotesFactory().getSessionAsSigner();
			}
			
			@Override
			public boolean isWebSession() {
				return false;
			}
			
			@Override
			public void close() {
				if (!closed) {
					sessionProvider.close();
					sessionProvider = null;
					if (notesFactory != null) {
						notesFactory.close();
						notesFactory = null;
					}
					closed = true;
				}
			}
			
			private boolean closed = false;
			
			@Override
			public boolean isClosed() {
				return closed;
			}
			
			private NotesFactory notesFactory;
			
			@Override
			public NotesFactory getNotesFactory() {
				if (notesFactory == null) {
					notesFactory = new NotesFactory(sessionProvider);
				}
				return notesFactory;
			}
			
		}

		XflTestNotesFactoryProvider factoryProvider = new XflTestNotesFactoryProvider();
		ScriptEngineManager manager = new XflEngineManager(factoryProvider);
		refDoc = factoryProvider.getSession().getCurrentDatabase().createDocument();
		engine = (XflEngine) manager.getEngineByName("xfl");
		engine.setLicenseKey("2FU.03E.PUH");
		
		engine.addExtension(new XflExtension() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getFunctionNames() {
				return "@Print";
			}
			
			@Override
			public Object evaluate(Context context, String formulaName, Object... args) throws Exception {
				Object msg = args[0];
				System.out.println(msg);
				return msg;
			}

		});
	}

	@AfterClass
	public static void close() throws Exception {
		engine.close();
	}


	@Test
	public void compileMath() throws Exception {
		String code = "2 * (1 + 20)";
		Expression ex = engine.compile(code);
		System.out.println(ex);
	}

	@Test
	public void compileFunction() throws Exception {
		String code = "f(2)";
		Expression ex = engine.compile(code);
		System.out.println(ex);
		assertEquals("f(2)", ex.toString());
	}

	@Test
	public void compileDate() throws Exception {
		String code = "[01.02.2014]";
		Expression ex = engine.compile(code);
		System.out.println(ex);
		assertEquals("[01.02.2014]", ex.getElement(0).toString());
	}

	@Test
	public void math() throws Exception {
		String code = "2 * (1 + 20)";
		System.out.println(code);
		Number value = (Number) engine.eval(code);
		System.out.println("result: " + value);
		assertEquals(42, value.intValue());
	}
	
	@Test
	public void operators() throws Exception {
		testCode("1+2*3", 7);
	}

	@Test
	public void compileList() throws Exception {
		String code = "'1' : {3}";
		Expression ex = engine.compile(code);
		System.out.println(ex);
	}

	@Test
	public void listIndex() throws Exception {
		String code = "_a := {1} : {3} : {5}; _a[2]";
		testCode(code, "3");
		code = "_a := {1} : {3} : {5}; _a[-3]";
		testCode(code, "1");
	}

	@Test
	public void listIndexNegativ() throws Exception {
		String code = "_a := {1} : {3} : {5}; _a[-3]";
		testCode(code, "1");
	}

	@Test(expected = SubscriptOutOfRangeException.class)
	public void listOutOfBounds1() throws Throwable {
		String code = "_a := {1} : {3} : {5}; _a[4]";
		System.out.println(code);
		try {
			engine.eval(code);
		} catch (EvaluationException e) {
			throw e.getRootCause();
		} 
	}

	@Test
	public void uninitializedArrays() throws Throwable {
		String code = "a := _uninitialized : {1}; @Implode(a; {#})";
		Object ev = evaluateCode(code);
		assertEquals("#1", ev);
	}
		
	@Test(expected = SubscriptOutOfRangeException.class)
	public void listOutOfBounds2() throws Throwable {
		String code = "_a := {1} : {3} : {5}; _a[-4]";
		System.out.println(code);
		try {
			engine.eval(code);
		} catch (EvaluationException e) {
			throw e.getRootCause();
		} 
	}

	@Test
	public void listOfEmptyString() throws Throwable {
		String code = "_a := {}; _a[1]";
		testCode(code, "");
	}
	
	@Test
	public void subscript() throws Throwable {
		String code = "GLOBAL axy := '1':'2':'3'; axy[2] := {Hallo}; @Implode(axy; {-})";
		testCode(code, "1-Hallo-3");
		code = "axz := '1':'2':'4'; axz[2] := {Hallo}; @Implode(axz; {-})";
		testCode(code, "1-Hallo-4");
	}
	
	@Test
	public void vieleKlammern() throws Exception {
		String code = "(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+" +
				"(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+1))))))))))))))))))))))))))))))))))))))))";
		Object ev = evaluateCode(code);
		assertEquals(41, ((Number) ev).intValue());
	}

	@Test
	public void label() throws Exception {
		String code = "@Goto(a);@return({b});"
				+ "LABEL a;"
				+ "@Return({a})";
		testCode(code, "a");
	}

	@Test
	public void gotoSchleife() throws Exception {
		String code = "_i := 1;"
				+ "@Goto(a);"
				+ "LABEL b;"
				+ "_i := _i + 1;"
				+ ""
				+ "LABEL a;"
				+ "@If(_i < 10 ; @Goto(b); _i);";
		testCode(code, 10);
	}

	@Test
	public void gotoInFor() throws Exception {
		String code = "@For(_k:=1; _k<5; _k:=_k+1;"
				+ "@Print({Lauf: } + @Text(_k));_i := 1;"
				+ "@Goto(a);"
				+ "LABEL b;"
				+ "_i := _i + 1;"
				+ ""
				+ "LABEL a;"
				+ "@If(_i < 10 ; @Goto(b); {}));"
				+ "_i";
		testCode(code, 10);
	}

	@Test
	public void gotoInForReturn() throws Exception {
		String code = "@For(_k:=1; _k<5; _k:=_k+1;"
				+ "@Print({Lauf: } + @Text(_k));"
				+ ""
				+ "_i := 1;@Goto(a);"
				+ "LABEL b;"
				+ "_i := _i + 1;"
				+ "@Return(_i);"
				+ ""
				+ "LABEL a;"
				+ "@If(_i < 10 ; @Goto(b); {}));"
				+ "_i";
		testCode(code, 2);
	}
	
	@Test
	public void gosub() throws Exception {
		String code = "@return(@Gosub(a)+ '!');"
				+ "LABEL a;"
				+ "@Return({hallo})";
		testCode(code, "hallo!");

		code = "@Goto(b);"
				+ "LABEL a;"
				+ "@Return({hallo});"
				+ "LABEL b;"
				+ "_a := @Gosub(a)";
		testCode(code, "hallo");

	}


	@Test
	public void ret() throws Exception {
		String code = "@return(@Gosub(a));"
				+ "LABEL a;"
				+ "@Return({hallo});";
		testCode(code, "hallo");	
	}

	@Test
	public void gosub2() throws Exception {
		String code = "@Goto(b);"
				+ "LABEL a;"
				+ "@Return({hallo } + _name);"
				+ "LABEL b;"
				+ "_name := {Hugo};"
				+ "_a := @Gosub(a);"
				+ "@Print(_a);"
				+ "_name := {Horst};"
				+ "@Print(@Gosub(a))";
		testCode(code, "hallo Horst");	
	}

	@Test(expected = LabelNotDefinedException.class)
	public void labelUnknown() throws Throwable {
		String code = "@Goto(a1);"
				+ "LABEL a;"
				+ "@Return({a})";
		System.out.println(code);
		try {
			engine.eval(code);
		} catch (EvaluationException e) {
			System.out.println(e.getMessage());
			throw e;
		} 
	}

	@Test
	public void notesFormula() throws Exception {
		String code = "_a := {Hallo}; "
				+ "@Left(_a; {o})";
		testCode(code, "Hall");
	}

	@Test
	public void ifTest() throws Exception {
		testCode("@If(1>2; {a}; 1<0; {b}; {c})", "c");
		testCode("@If(1>0; {a}; 1<0; {b}; {c})", "a");
		testCode("@If(1>2; {a}; 1<2; {b}; {c})", "b");
		testCode("@If(1>0; {a})", "a");
		testCode("@If(1>2; {a})", 0);
	}

	protected Object evaluateCode(String code, Document doc) throws Exception {
		System.out.println(code);
		Object value = engine.eval(code, doc);
		if (value == null) {
			return null;
		}
		String out = "";
		if (value instanceof Object[]) {
			out += "[";
			boolean trenn = false;
			Object[] l = (Object[]) value;
			for (Object object : l) {
				out += (trenn ? ", " : "") + object.toString();
				trenn = true;
			}
			out += "]";
		} else {
			out = value.toString();
		}
		System.out.println("result: " + out);
		return value;
	}
	protected Object evaluateCode(String code) throws Exception {
		return evaluateCode(code, refDoc);
	}

	protected void testCode(String code, Object expected) throws Exception {
		testCode(code, expected, refDoc);
	}

	protected void testCode(String code, Object expected, Document doc) throws Exception {
		Object ev = evaluateCode(code, doc);
		assertEquals(expected, ev);
	}

	@Test
	public void forTest() throws Exception {
		testCode("_a := {};"
				+ "@For(i:=0; i<10; i := i + 1;"
				+ "_a := _a + {x}"
				+ ");"
				+ "_a", "xxxxxxxxxx");
	}


	@Test
	public void forMitGoto() throws Exception {
		testCode("_a := {};"
				+ "@For(i:=0; i<10; i := i + 1;"
				+ "@If(i=5; @Goto(lab);{});"
				+ "_a := _a + {x};"
				+ "LABEL lab"
				+ ");"
				+ "_a", "xxxxxxxxx");
	}


	@Test
	public void forMitGosub() throws Exception {
		testCode("_a := {};"
				+ "@For(i:=0; i<3; i := i + 1;"
				+ "@If(i=2; (ret := 1;@Gosub(lab);ret := 0);{});"
				+ "LABEL lab;"
				+ "_a := _a + {x};"
				+ "@If(ret=1; @Return({});{})"
				+ ");"
				+ "_a", "xxxx");
	}

	@Test
	public void assign() throws Exception {
		testCode("_a := {};", "");
	}


	@Test
	public void whileTest() throws Exception {
		testCode("_a := {};"
				+ "i := 0;"
				+ "@while(i<3;"
				+ "i := i + 1;"
				+ "_a := _a + {x}"
				+ ");"
				+ "_a", "xxx");
	}


	@Test
	public void bindings() {

		// bind a to 10 and b to 5
		engine.put("a", 10);
		engine.put("b", 5);
		// get bound values
		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		Object a = bindings.get("a");
		Object b = bindings.get("b");
		System.out.println("a = " + a);
		System.out.println("b = " + b);
		// use the bound values in calculations
		try {
			Object result = engine.eval("c := a + b;");
			System.out.println("a + b = " + result);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void object() throws Exception {
		engine.put("a", new Dummy());
		testCode("a.foo()", "hallo");
	}
	
	@Test
	public void object2() throws Exception {
		engine.put("a", new Dummy());
		testCode("a.foo2({teST})", "test");
		testCode("a.foo2(123)", 246);
		testCode("a.foo2(a.FOO2(123))", 492);
	}
	
	@Test
	public void object3() throws Exception {
		engine.put("a", new Dummy());
		testCode("x:=a.getit.FOO2(123); x-1", 245);
	}
	
	@Test
	public void globalObject() throws Exception {
		engine.put("a", new Dummy());
		testCode("GLOBAL OBJECT b := a.getit();"
				+ "b.FOO2(123);", 246);
		Object test = engine.get("b");
		System.out.println("global b: " + test);
		assertEquals("1", test instanceof Dummy ? "1" : "");
	}
	
	public class Dummy {
		public String foo() {
			return "hallo";
		}
		public String foo2(String t) {
			return t.toLowerCase();
		}
		public Number foo2(Number t) {
			return t.intValue() * 2;
		}
		public Dummy getIt() {
			return this;
		}
	}
	
	@Test
	public void set() throws Exception {
		testCode("ab := {t};"
				+ "@set({a}+{b}; ab + {est});"
				+ "ab;", "test");
	}
	
	@Test
	public void get() throws Exception {
		testCode("ab := {test};"
				+ "@get({a}+{b});", "test");
	}
	
	@Test
	public void setGlobal() throws Exception {
		testCode("ab := {t};"
				+ "@setglobal({a}+{b}; ab + {est});"
				+ "ab + GLOBAL ab;", "ttest");
	}
	
	@Test
	public void getGlobal() throws Exception {
		testCode("GLOBAL ab := {test};"
				+ "@getglobal({a}+{b});", "test");
	}

	@Test
	public void eval() throws Exception {
		testCode("{t} + @Eval({'est'});", "test");
	}

	@Test(expected = ErrorException.class)
	public void error() throws Throwable {
		String code = "@If(@True; @Error(123; {ganz schlimmer Fehler}); {});{1}";
		try {
			engine.eval(code);
		} catch (EvaluationException e) {
			throw e.getRootCause();
		}
	}

	@Test
	public void transformList() throws Exception {
		testCode("l := {a}:{b};"
				+ "@Implode(@Transform(l; {e}; e:(e+{x})); {,})", "a,ax,b,bx");
	}
	
	@Test
	public void transformString() throws Exception {
		testCode("@Transform({Hallo}; {s}; @Uppercase(s));", "HALLO");
	}

	@Test
	public void define() throws Exception {
		testCode("DEFINE f(x) := 2 + x*x;"
				+ "F(5)", 27);
	}

	@Test
	public void defineOhneParameter() throws Exception {
		testCode("DEFINE @Test():= @Text([01.01.2016]);"
				+ "@Test", "01.01.2016");
	}

	@Test
	public void define2() throws Exception {
		testCode("FIELD ABC := {Test};"
				+ "Define @Test():= (@GetField({ABC}) + ABC); @TEST",
				"TestTest");
	}
	
	@Test
	public void extension() throws Exception {
		engine.addExtension(new XflExtension() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getFunctionNames() {
				return "@LogText";
			}
			
			@Override
			public Object evaluate(Context context, String formulaName, Object... args) throws Exception {
				String arg1 = (String) args[0];
				System.out.println("LogText: " + arg1);
				return arg1.toUpperCase();
			}
		});
		
		testCode("a:=@LogText({Hallo})", "HALLO");
	}

	@Test
	public void trueTest() throws Exception {
		testCode("@True", 1);
	}

	@Test
	public void klammerUndOperator() throws Exception {
		testCode("(2+3)*4", 20);
	}
	
	@Test
	public void evalLeer() throws Exception {
		testCode("", null);
	}
	
	@Test
	public void dbTitle() throws Exception {
		testCode("@DBTitle", "Beispiele / Tests");
	}
	
	//@Test
	@SuppressWarnings({ "rawtypes", "unused" })
	public void xxxNew() throws Exception {
		
		Database db = engine.getCurrentDatabase();
		Session session = engine.getSession();
		Document pdoc = db.getProfileDocument("XFLOnServerExecuter", session.getUserName());
		Agent agent = db.getAgent("(XFLOnServer)");

		pdoc.replaceItemValue("$Action", "getUDF");
		pdoc.save();
		String noteID = pdoc.getNoteID();
		agent.runOnServer(noteID);

		pdoc.recycle();
		
		pdoc = db.getDocumentByID(noteID);
	//	String res = pdoc.getItemValueString("$val_$Result$");
//		System.out.println(res);
		Vector res = pdoc.getItemValue("$udf");
		
		
		engine.setGlobalVar("a", "12");
		String code = "@Formel({test}; a)";
		
		Map<String, Data> all = engine.getAllGlobalVars();
		for (java.util.Map.Entry<String, Data> e : all.entrySet()) {
			String name = e.getKey();
			Data value = e.getValue();
			pdoc.replaceItemValue("$global_" + name, value.getValue());
		}
		
		// pdoc.replaceItemValue("$RefID", ""); // ID zum doc
		
		pdoc.replaceItemValue("$Action", "Execute");
		pdoc.replaceItemValue("$Code", code);
		pdoc.save();
		pdoc.recycle();
		agent.runOnServer(noteID);
		
		pdoc = db.getDocumentByID(noteID);
		String err = pdoc.getItemValueString("$Error");
		String ret = pdoc.getItemValueString("$Return");
		
		pdoc.replaceItemValue("$Action", "Remove");
		pdoc.save();
		agent.runOnServer(noteID);

		agent.recycle();
		pdoc.recycle();
		
		db.recycle();
	}
	
	@Test
	public void lotusScript() throws Exception {
		engine.addLotusScriptExtensions();
		String code = "@Formel({test}; {33})";
		testCode(code, "hallo 33");
	}
	
	@Test
	public void execute() throws Exception {
		engine.addLotusScriptExtensions();
		engine.setGlobalVar("var1", "test");
		String code = "@Execute({Dim s as String\n"
				+ "s = xflgetglobalvar(|var1|)\n"
				+ "Print |var1: | & s\n"
				+ "call XFLSetGlobalVar(|Var2|, s + s)});"
				+ "GLOBAL var2";
		testCode(code, "testtest");
		
		engine.setGlobalVar("var1", 123);
		code = "@Execute({Dim s as Integer\n"
				+ "s = xflgetglobalvar(|var1|)\n"
				+ "call XFLSetGlobalVar(|Var2|, s + s)});"
				+ "GLOBAL var2";
		testCode(code, 246);
		
		code = "GLOBAL var1 := {a}:{b};@Execute({Dim v as Variant\n"
				+ "v = xflgetglobalvar(|var1|)\n"
				+ "Print |v(0): | & v(0)\n"
				+ "Print |v(1): | & v(1)\n"
				+ "call XFLSetGlobalVar(|Var2|, v(0) & v(1))});"
				+ "GLOBAL var2";
		testCode(code, "ab");
		
	}
	
	@Test
	public void logicalOperatorError() throws Exception {
		String code = "a := {a}; b := @True; @Text(a & b)";
		testCode(code, "Incorrect data type for operator or @Function: Number expected");
	}
	
	@Test
	public void logicalOperatorError2() throws Exception {
		String code = "a := {a}; b := @True; @if(a & b; 1; 2)";
		testCode(code, 2);
	}
	
	@Test
	public void not() throws Exception {
		String code = "!@Contains({abc}; {d})";
		testCode(code, 1);
		code = "@If(!@Contains({abc}; {d}) ; @True ; @False)";
		testCode(code, 1);
		
		code = "!@Contains({abc}; {a})";
		testCode(code, 0);
		code = "@If(!@Contains({abc}; {a}) ; @True ; @False)";
		testCode(code, 0);
	}
	
	@Test
	public void compileNotKlammer() throws Exception {
		String code = "@If(!(_a = b) | (_a = 1); {x}; {y})";
		Expression ex = engine.compile(code);
		System.out.println(ex);
	}
	
	@Test(expected = CompilerException.class)
	public void compileError() throws Exception {
		String code = "Das @Ist @Kein korrekter Code";
		try {
			engine.compile(code);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		}
	
	@Test
	public void wrongOperator() throws Exception {
		String code = "@Text({test} & implode({a}:{b}; {_}))";
		testCode(code, "Incorrect data type for operator or @Function: Number expected");
		
		code = "@Text({test} + implode({a}:{b}; {_}))";
		testCode(code, "testa_b");
	}
	
	@Test
	public void wrongOperator2() throws Exception {
		String code = "text(\"a:\" & x);";
		testCode(code, "Incorrect data type for operator or @Function: Number expected");
	}
	
	@Test(expected = ObjectVariableNotSetException.class)
	public void objectVariableNotSet() throws Throwable {
		try {
			evaluateCode("myObject.method");
		} catch (EvaluationException e) {
			throw e.getRootCause();
		}
	}
	
	@Test
	public void nullCode() throws Exception {
		String code = null;
		testCode(code, null);
	}
	
	@Test
	public void emptyCode() throws Exception {
		String code = "";
		testCode(code, null);
	}
	
	@Test
	public void unDefinedVar() throws Exception {
		String code = "_var";
		testCode(code, "");
		code = "_var := '1'; _var := @Deletefield;_var";
		testCode(code, "");
		code = "_var";
		testCode(code, "");
	}
	
	@Test
	public void richText() throws Exception {
		RichTextItem body = refDoc.createRichTextItem("Body");
		body.appendText("test");
		String code = "body";
		testCode(code, "test");
	}
	
	@Test(expected = CompilerException.class)
	public void langeFormel() throws Exception {
		InputStream stream = this.getClass().getResourceAsStream("formel.mac");
		Scanner scanner = new Scanner(stream, "UTF-8");
		try {
			String code = scanner.useDelimiter("\\A").next();
			testCode(code, "");
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		} finally {
			scanner.close();
		}
	}
	
	@Test
	public void isNothing() throws Exception {
		String code = "OBJECT item := doc.getFirstItem({nix});"
				+ "@If(@IsNothing(item) ; {nothing} ; item.text)";
		testCode(code, "nothing");
	}
	
	@Test
	public void mailSend() throws Exception {
		String code = "_sendTo := {Bert Häßler/Leonso};"
				+ "@MailSend(_SendTo ; {}; {}; {Mail aus JUnit-Test XFL});"
				+ "{}";
		testCode(code, "");
	}

	@Test
    public void getProfileField() throws Exception {
        String code = "@GetProfileField({svz}; {$name})";
        testCode(code, "$profile_003svz_");
	}

    @Test
    public void dbColumn() throws Exception {
        String code = "@Implode(@Subset({} : @Text(@DbColumn({}:{noCache};{}:{};{config}; 1)); 3); {~})";
        String res = "~dynsApplikationsID~DynsLicenseKey";
        testCode(code, res);
    }

    // führt zum Crash !!!
  //  @Test
    public void dbSearch() throws Exception {
        String code = "OBJECT db := doc.ParentDatabase();"
        		+ "searchf := {FORM = 'DynsRecord'};"
        		+ "object col := db.search(searchf; @Nothing; 10);"
        		+ "Object d := col.getfirstdocument;"
        		+ "@While(@IsNothing(d) = @False;"
        		+ "_test := d.getItemValue('Subject');"
        		+ "@Print(test);"
        		+ "Object d := col.getnextdocument(d)"
        		+ ");"
        		+ "col.count";
        testCode(code, "");
    }
    
    @Test
    public void isJavaRuntime() throws Exception {
    	  String code = "@IsJavaRuntime";
          testCode(code, 1);
    }
    
    @Test
    public void isLotusScriptRuntime() throws Exception {
    	  String code = "@IsLotusScriptRuntime";
          testCode(code, 0);
    }
    
    @Test
    public void version() throws Exception {
    	  String code = "@XFLVersion";
          testCode(code, XflEngine.VERSION);
    }
    
    @Test
    public void umlauteImBezeichner() throws Exception {
    	String code = "_varäöü%$§ := {umläute}; _varäöü%$§";
        testCode(code, "umläute");
    }
    
    @Test
    public void newLine() throws Exception {
    	String code = "_nl := @NewLine; _t := {a} + @NewLine + {b}; @Left(_t; _nl)";
        testCode(code, "a");
    }
    
    @Test
    public void logText() throws Exception {
    	
    	engine.addExtension(new XflExtension() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getFunctionNames() {
				return "@LogText";
			}
			
			@Override
			public Object evaluate(Context context, String formulaName, Object... args) throws Exception {
				Object obj = args[0];
				System.out.println(obj);
				return null;
			}
		});
    	
    	String code = "@If(EMPF_OE_SCHL != {0290006};"
    			+ "(@LogText({Ereignis wird von empfangender OE } + EMPF_OE_SCHL + { verarbeitet.});"
    			+ "{@Formel(StatusSetzen; manuelle Bearbeitung)};"
    			+ "@Return({})"
    			+ ");{})";
        testCode(code, "");
    }
    
    @Test
    public void notKlammer() throws Exception {
    	String code = "_a := {1}; @If(!(_a = {1}) | @True; {2}; {3})";
    	testCode(code, "2");
    }
    
    @Test
    public void right() throws Exception {
    	String code = "@right({abc}; {b}) + right({abc}; {a})";
    	testCode(code, "cbc");
    }
    
    @Test
    public void vergleichMitUnavailable() throws Exception {
    	String code = "@If(FeldNichtDa={1}; @True; @False);";
    	testCode(code, 0);
    }
    
    @Test
    public void vergleichErrorVar() throws Exception {
    	String code = "_persNr := {123123123};"
    			+ "_DatumUndEmail := @DBLookup({}:{}; @DBName; {EreignisseNachDatumUndEmail}; _persNr; 3);"
    			+ "_erg := @If(@IsError(_DatumUndEmail); {error}; _DatumUndEmail !={}; @Subset(_DatumUndEmail; 1); {});";
    	testCode(code, "error");
    }
    
    @Test
    public void vergleichErrorVar2() throws Exception {
    	String code = "_persNr := {123123123};"
    			+ "_DatumUndEmail := @DBLookup({}:{}; @DBName; {EreignisseNachDatumUndEmail}; _persNr; 3);"
    			+ "_erg := @If(_DatumUndEmail !={} & !@IsError(_DatumUndEmail); @Subset(_DatumUndEmail; 1); {error});";
    	testCode(code, "error");
    }
    
    @Test
    public void htmlDecode() throws Exception {
    	String code = "@HtmlDecode({<p>Andr&#233;</p>})";
    	testCode(code, "André");
    }
    
    @Test
    public void largeStrings() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("_a := {");
    	String l = new String(new char[3000]).replace("\0", "a");
    	sb.append(l);
    	sb.append("}; _a+{b}");
    	    	
    	testCode(sb.toString(), l + "b");
    }
    
    @Test
    public void largeStringsWithOperator() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("_a := {");
    	String l = new String(new char[1990]).replace("\0", "a");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("} + {");
    	sb.append(l);
    	sb.append("}; _a+_a");
    	    	
    	testCode(sb.toString(), l + l + l + l + l + l + l + l + l + l + l + l + l + l);
    }
    
    @Test
    public void tryCatch1() throws Exception {
    	engine.setGlobalObject("session", engine.getSession());
    	String code = "@Try(CALL session.sendConsoleCommand({serverXY}; {sh ta})); {a}";
    	testCode(code, "a");
    }
    
    @Test
    public void tryCatch2() throws Exception {
    	engine.setGlobalObject("session", engine.getSession());
    	String code = "@Try(session.sendConsoleCommand({serverXY}; {sh ta}); {e}); e;";
    	testCode(code, "NotesException: Notes error: Unable to find path to server. Check that your network connection is working. If you have a working connection, go to Preferences - Notes Ports and click Trace to discover where it breaks down. in Ausdruck [session.sendConsoleCommand({serverXY}; {sh ta})]");
    }
    
    @Test
    public void tryCatch3() throws Exception {
    	engine.setGlobalObject("session", engine.getSession());
    	String code = "@Try((CALL session.sendConsoleCommand({serverXY}; {sh ta}); {a}); {e}; (@Print(e);{b}));";
    	testCode(code, "b");
    }
    
    @Test
    public void tryCatch4() throws Exception {
    	engine.setGlobalObject("session", engine.getSession());
    	String code = "@Try((CALL session.sendConsoleCommand({serverXY}; {sh ta}); {a}); {e}; {b}; {c});";
    	testCode(code, "c");
    }

    @Test
    public void runAgent() throws Exception {
    	String code = "@RunAgent({test})";
    	testCode(code, "");
    }

    @Test
    public void runAgent2() throws Exception {
    	String code = "@Try(@RunAgent({test2}); {e}; {Fehler})";
    	testCode(code, "Fehler");
    }
    
    @Test
    public void isNewDoc() throws Exception {
		Document testDoc = refDoc.getParentDatabase().createDocument();
		String code = "@If(@IsNewDoc; {new}; {not new})";
    	testCode(code, "new", testDoc);
    	testDoc.save();
    	testCode(code, "not new", testDoc);
    	testDoc.remove(true);
    }
    
    @Test
    public void performance() throws Exception {
    	String code = "@Print({Start});@For(i := 1; i <= 1000; i := i + 1;@If(@Modulo(i; 100) = 0; @Print(i); {}));@Print({fertig});{}";
    	testCode(code, "");
    }
    
    @Test
    public void base64Encode() throws Exception {
    	String code = "@ReplaceSubstring(@Base64Encode({Mein Text}); @NewLine; {})";
    	testCode(code, "TWVpbiBUZXh0");
    }
    
    @Test
    public void base64Decode() throws Exception {
    	String code = "@Base64Decode({TWVpbiBUZXh0})";
    	testCode(code, "Mein Text");
    }
    
    @Test
    public void promptOK() throws Exception {
    	String code = "@Prompt([OK]; {Titel}; {bla bla bla})";
    	testCode(code, 1);
     }
    
    @Test
    public void promptYesNo() throws Exception {
    	String code = "@Prompt([YESNO]; {Titel}; {Bitte auf 'Ja' klicken})";
    	testCode(code, 1);
      }
    
    @Test
    public void promptYesNoCancel() throws Exception {
    	String code = "@Prompt([YESNOCancel]; {Titel}; {Bitte auf 'Abbrechen' klicken})";
    	testCode(code, -1);
    }
    
    @Test
    public void promptOKCancelEdit() throws Exception {
    	String code = "@Prompt([OKCancelEdit]; {Titel}; {Bitte geben Sie 'abc' ein})";
    	testCode(code, "abc");
    }
    
    @Test
    public void promptOKCancelList() throws Exception {
    	String code = "@Prompt([OKCancelList]; {Titel}; {Wählen Sie 'abc' aus}; {xyz}; {abc}:{def}:{xyz})";
    	testCode(code, "abc");
    }
    
    @Test
    public void promptOKCancelListDefaultWrong() throws Exception {
    	String code = "@Prompt([OKCancelList]; {Titel}; {Wählen Sie 'abc' aus}; {xyzxxx}; {abc}:{def}:{xyz})";
    	testCode(code, "abc");
    }
   
    @Test
    public void promptOKCancelListLong1() throws Exception {
    	String code = "@Prompt([OKCancelList]; {Titel}; {Wählen Sie 'abc' aus}; {xyz}; {abc}:{def}:{g}:{h}:{i}:{j}:{k}:{l}:{m}:{n}:{xyz})";
    	testCode(code, "abc");
    }

    @Test
    public void promptOKCancelListLong2() throws Exception {
    	String code = "@Prompt([OKCancelList]; {Titel}; {Wählen Sie 'abc' aus}; {xyz}; {abc}:{def}:{g}:{h}:{iiiiiii}:{j}:{k}:{l}:{m1234567890123456789012345678901234567890}:{n}:{xyz})";
    	testCode(code, "abc");
    }
    
    @Test
    public void promptPassword() throws Exception {
    	String code = "@Prompt([Password]; {Titel}; {Geben Sie 'abc' ein})";
    	testCode(code, "abc");
    }
    
    @Test(expected = CompilerException.class)
    public void sonderzeichen() throws Exception {
    	// das Space vor + ist ein Sondercode
    	String code = "@Right({abc}; {a})  + {d}";
    	testCode(code, "bd");
    }
    
    @Test
    public void kommaZahlen() throws Exception {
    	// das Space vor + ist ein Sondercode
    	String code = "@Text(1,23  + 3,21)";
    	testCode(code, "4,44");
    }
    
    @Test
    public void createJavaObject() throws Exception {
    	String code = "Object o := @createJavaObject({java.lang.String});\n"
    			+ "o.length";
    	testCode(code, 0);
    }
    
    @Test
    public void errorText() throws Exception {
    	testCode("@Text(1/0)", "@ERROR");
    	testCode("@Text({a} + 1)", "Incorrect data type for operator or @Function: Text expected");
    	testCode("@Text(@TextToNumber({x}))", "@ERROR");
    	testCode("@Text(@TextToTime({x}))", "");
    	testCode("a:=1;@Text(@Right(a; {x}))", "Incorrect data type for operator or @Function: Text expected");
    	testCode("@Text(@Month(x))", "Incorrect data type for operator or @Function: Time/Date expected");
    	testCode("@Text(@Round(x; 2))", "Incorrect data type for operator or @Function: Number expected");
    }
    
    @Test
    public void indexNumberList() throws Exception {
    	// das Space vor + ist ein Sondercode
    	String code = "_l := 1:2:3:4; _l[2]";
    	testCode(code, 2);
    }
    
	@Test
	public void text() throws Exception {
		testCode("@Text(@True)", "1");
		testCode("@Text(@False)", "0");
		testCode("_PersArt := {0};@Text( !_PersArt={P}:{N})", "1");
		testCode("_PersArt := {N};@Text( !_PersArt={P}:{N})", "0");
		testCode("_PersArt := {N};@If(!(_PersArt={P}:{N});{0};{1})", "1");
	}
	
	@Test
	public void dblookup() throws Exception {
		testCode("@Subset(@DbLookup({}:{}; {}:{names.nsf}; {($Users)}; {Bert Häßler}; {FullName}); 1)", "bh >> Bert Häßler <bhaessler@leonso.de>");
		// ohne doc-referenz
		testCode("@Subset(@DbLookup({}:{}; {}:{names.nsf}; {($Users)}; {Bert Häßler}; {FullName}); 1)", "bh >> Bert Häßler <bhaessler@leonso.de>", null);
	}
	
	@Test
	public void sessionAsSigner1() throws ScriptException {
				
		class SessionAsSignerEngine extends XflEngine implements Serializable {
			private static final long serialVersionUID = 1L;

			public SessionAsSignerEngine() {
				super(new LINotesFactoryProvider() {
					private static final long serialVersionUID = 1L;

					private LNativeNotesSessionProvider sessionProvider = new LSessionProviderNotesJsf();
					//private LNativeNotesSessionProvider sessionProvider = new LSessionProviderNotesExtern("server2", "log.nsf");

					// diese Methode gibt es zwar, die wird aber nicht von der XflEngine benutzt
					@Override
					public SessionWrapper getSessionAsSigner() {
						return getNotesFactory().getSessionAsSigner();
					}
					
					// das wird von Xfl benutzt. 
					@Override
					public SessionWrapper getSession() {
						// return getNotesFactory().getSession();
						// deshalb ändern wir das auf SessionAsSigner.
						return getNotesFactory().getSessionAsSigner();
					}

					@Override
					public boolean isWebSession() {
						return true;
					}

					@Override
					public void close() {
						if (!closed) {
							sessionProvider.close();
							sessionProvider = null;
							if (notesFactory != null) {
								notesFactory.close();
								notesFactory = null;
							}
							closed = true;
						}
					}

					private boolean closed = false;

					@Override
					public boolean isClosed() {
						return closed;
					}

					private NotesFactory notesFactory;

					@Override
					public NotesFactory getNotesFactory() {
						if (notesFactory == null) {
							notesFactory = new NotesFactory(sessionProvider);
						}
						return notesFactory;
					}
				});
			}	
		}
		
		XflEngine xfl = new SessionAsSignerEngine();
		String formula = "@Username";
		Object result = xfl.eval(formula);
		assertEquals("CN=Bert Häßler/O=Leonso", result);
	}

	@Test
	public void sessionAsSigner2() throws ScriptException {
				
		class SessionAsSignerEngine extends XflJsfEngine implements Serializable {
			private static final long serialVersionUID = 1L;

			public SessionAsSignerEngine() {
				super();
			}
				
			Session cache = null;
			@Override
			public Session getSession() throws Exception {
				// das läuft leider nicht. Intern hängt noch der JSF-Teil drin. currentDatabase geht schief
				if (cache == null) {
					cache =  new LSessionProviderNotesExtern("server2", "log.nsf").getSession();
				}
				return cache;
				//return UtilsJsf.getCurrentSessionAsSigner();
			}
		}
		
		XflEngine xfl = new SessionAsSignerEngine();
		String formula = "@Username";
		Object result = xfl.eval(formula);
		assertEquals("CN=Bert Häßler/O=Leonso", result);
	}

}