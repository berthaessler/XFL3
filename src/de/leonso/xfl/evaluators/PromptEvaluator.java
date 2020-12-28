package de.leonso.xfl.evaluators;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.leonso.xfl.Context;
import de.leonso.xfl.Data;
import de.leonso.xfl.Expression;
import de.leonso.xfl.XflEngine;

/* @formatter:off */
/**
 * \@Prompt( [ style ] : [NoSort] ;  title  ;  prompt  ;  defaultChoice  ;  choiceList  ;  filetype  ) 
 *
 * @param stype<br>
 * PROMPT_CHOOSEDATABASE (13)<br>
 * Note: The constant name PROMPT_CHOOSEDATABASE is not implemented but the literal value 13 can be used instead.<br>
 * 
 * PROMPT_OK (1)<br>
 * PROMPT_OKCANCELCOMBO (5)<br>
 * PROMPT_OKCANCELEDIT (3)<br>
 * PROMPT_OKCANCELEDITCOMBO (6)<br>
 * PROMPT_OKCANCELLIST (4)<br>
 * PROMPT_OKCANCELLISTMULT (7)<br>
 * PROMPT_PASSWORD (10)<br>
 * PROMPT_YESNO (2)<br>
 * PROMPT_YESNOCANCEL (11)<br>
 * 
 * @return value<br>
 * If the user enters a value, returns the value as text or a text list.<br>
 * If the user selects Yes, returns 1 (True).<br>
 * If the user selects No, returns 0 (False).<br>
 * If the user selects Cancel, formula evaluation stops. The exception is [YesNoCancel], which returns -1 if the user selects Cancel.<br>
 * @Prompt([OkCancelEdit]) returns only the first 254 characters of the text entered.<br>
 * 
 */
public class PromptEvaluator extends Evaluator {
	private static final long serialVersionUID = 1L;
	/* @formatter:on */

	private final String PROMPT_OK = "[OK]";
	private final String PROMPT_OKCANCELCOMBO = "[OKCANCELCOMBO]";
	private final String PROMPT_OKCANCELEDIT = "[OKCANCELEDIT]";
	private final String PROMPT_OKCANCELEDITCOMBO = "[OKCANCELEDITCOMBO]";
	private final String PROMPT_OKCANCELLIST = "[OKCANCELLIST]";
	private final String PROMPT_OKCANCELLISTMULT = "[OKCANCELLISTMULT]";
	private final String PROMPT_PASSWORD = "[PASSWORD]";
	private final String PROMPT_YESNO = "[YESNO]";
	private final String PROMPT_YESNOCANCEL = "[YESNOCANCEL]";

	public PromptEvaluator(XflEngine engine) {
		super(engine);
	}

	@Override
	public Data evaluate(Expression expression, Context context) throws Exception {
		Data res = new Data(expression, context);
		ArrayList<Expression> elements = expression.getElements();
		Data data = elements.get(0).evaluate(context);
		String text = data.getText();
		Object value;

		if (PROMPT_OK.equalsIgnoreCase(text)) {
			value = promptOK(elements, context);

		} else if (PROMPT_YESNO.equalsIgnoreCase(text)) {
			value = promptYesNo(elements, context);

		} else if (PROMPT_YESNOCANCEL.equalsIgnoreCase(text)) {
			value = promptYesNoCancel(elements, context);
			// int i = (Integer) value;
			// if ((-1) == i) {
			// in LotusScript gibt es ein Flag DoNotQuitOnCancel.
			// Das könnten wir hier auch einführen
			// throw new CancelException(expression, context);
			// }

		} else if (PROMPT_OKCANCELEDIT.equalsIgnoreCase(text)) {
			value = promptOKCancelEdit(elements, context);

		} else if (PROMPT_OKCANCELLIST.equalsIgnoreCase(text)) {
			value = promptOKCancelList(elements, context);
			// if (!(value instanceof String)) {
			// in LotusScript gibt es ein Flag DoNotQuitOnCancel.
			// throw new CancelException(expression, context);
			// }

		} else if (PROMPT_OKCANCELCOMBO.equalsIgnoreCase(text)) {
			value = promptOKCancelCombo(elements, context);
			// if (!(value instanceof String)) {
			// in LotusScript gibt es ein Flag DoNotQuitOnCancel.
			// throw new CancelException(expression, context);
			// }

		} else if (PROMPT_OKCANCELEDITCOMBO.equalsIgnoreCase(text)) {
			throw new Exception("Option '" + text + "' wird von @Prompt nicht unterstützt");

		} else if (PROMPT_OKCANCELLISTMULT.equalsIgnoreCase(text)) {
			throw new Exception("Option '" + text + "' wird von @Prompt nicht unterstützt");

		} else if (PROMPT_PASSWORD.equalsIgnoreCase(text)) {
			value = promptPassword(elements, context);
			// if (!(value instanceof String)) {
			// in LotusScript gibt es ein Flag DoNotQuitOnCancel.
			// throw new CancelException(expression, context);
			// }

		} else {
			// ungültig? oder Zahlenwert?
			throw new Exception("Option '" + text + "' wird von @Prompt nicht unterstützt");
		}

		res.assignValue(value);
		return res;
	}

	private Object promptOK(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		final String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		final String prompt = (String) data.getValue();

		// final JFrame frame = new JFrame();
		// frame.setAlwaysOnTop(true); // verhindern, dass modal "und" im Hintergrund - (Sch... Notes-Client)
		// JOptionPane.showMessageDialog(frame, prompt, title, JOptionPane.INFORMATION_MESSAGE);
		openDialog(title, prompt, JOptionPane.DEFAULT_OPTION);

		return 1; // so ist es in Notes
	}

	private Object promptYesNo(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();

		int n = openDialog(title, prompt, JOptionPane.YES_NO_OPTION);
		// int n = JOptionPane.showConfirmDialog(frame, prompt, title, JOptionPane.YES_NO_OPTION);
		// hier wird die Position des Buttons geliefert, nicht die "Bedeutung"
		if (n == 0) { // 1. Button = JA
			return 1; // so ist es in Notes
		} else { // 2. Button = NEIN
			return 0; // so ist es in Notes
		}
	}

	private Object promptYesNoCancel(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();

		// int n = JOptionPane.showConfirmDialog(frame, prompt, title, JOptionPane.YES_NO_CANCEL_OPTION);
		int n = openDialog(title, prompt, JOptionPane.YES_NO_CANCEL_OPTION);

		// hier wird die Position des Buttons geliefert, nicht die "Bedeutung"
		if (n == 0) { // 1. Button = JA
			return 1; // so ist es in Notes
		} else if (n == 1) { // 2. Button = NEIN
			return 0; // so ist es in Notes
		} else { // 3. Button = ABBRUCH
			return -1;
		}

	}

	private boolean activated;

	private int openDialog(String title, String prompt, int paneOption) {
		final JFrame frame = new JFrame();

		JOptionPane pane = new JOptionPane(prompt);
		pane.setOptionType(paneOption);
		final JDialog dialog = pane.createDialog(frame, title);
		dialog.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// System.out.println("windowDeactivated");
				activated = false;
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// System.out.println("windowActivated");
				activated = true;
			}
		});

		// der Client legt sich manchmal VOR die Dialogbox
		activated = true;

		boolean dialogClosed = false;

		do {
			// Dialog anzeigen
			Thread show = new Thread() {
				public void run() {
					// dialog.setVisible(true);

					// http://stackoverflow.com/questions/309023/how-to-bring-a-window-to-the-front
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							dialog.toFront();
							dialog.repaint();
						}
					});
					dialog.setVisible(true);
				}
			};
			show.start();

			// schauen, ob noch im Vordergrund
			int count = 0;
			do {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) {
				}
				count++;
				// nach 5 sec refresh, falls das Fendter unbemerkt nach hinten gerutscht ist
			} while (dialog.isFocused() && activated && (count < 50));

			if (dialog.isVisible()) {
				// das Fenster ist nicht mehr vorn
				// neu öffnen...
				dialog.setVisible(false);
			} else {
				// Dialog geschlossen
				dialogClosed = true;
			}

			// wieder von vorn (anzeigen)
		} while (!dialogClosed);

		int n = (Integer) pane.getValue();
		dialog.dispose();
		return n;
	}

	private Object promptOKCancelEdit(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();
		String defaultValue;
		if (elements.size() > 3) {
			data = elements.get(3).evaluate(context);
			defaultValue = (String) data.getValue();
		} else {
			defaultValue = "";
		}

		JFrame frame = new JFrame();
		// frame.setAlwaysOnTop(true); // verhindern, dass modal "und" im Hintergrund - (Sch... Notes-Client)
		String s = (String) JOptionPane.showInputDialog(frame, prompt, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);

		return s;
	}

	private Object promptOKCancelCombo(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();
		String defaultValue;
		if (elements.size() > 3) {
			data = elements.get(3).evaluate(context);
			defaultValue = (String) data.getValue();
		} else {
			defaultValue = "";
		}
		Object[] options = null;
		List<String> list = null;
		if (elements.size() > 4) {
			data = elements.get(4).evaluate(context);
			Object values = data.getValue();
			if (values instanceof Object[]) {
				options = (Object[]) values;
			} else {
				list = new ArrayList<String>();
				String v = (String) values;
				list.add(v);
			}
		} else {
			list = new ArrayList<String>();
		}
		if (options == null) {
			options = new Object[list.size()];
			for (int i = 0; i < options.length; i++) {
				options[i] = list.get(i);
			}
		}

		JFrame frame = new JFrame();
		// frame.setAlwaysOnTop(true); // verhindern, dass modal "und" im Hintergrund - (Sch... Notes-Client)
		Object sel = JOptionPane.showInputDialog(frame, prompt, title, JOptionPane.PLAIN_MESSAGE, null, options, defaultValue);

		if (sel == null) { // Cancel
			return -1;
		}
		return sel;
	}

	private Object promptPassword(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();

		String pw = PromptPassword.createAndShowGUI(title, prompt);
		// bei Abbruch -1
		return pw == null ? -1 : pw;
	}

	private Object promptOKCancelList(ArrayList<Expression> elements, Context context) throws ScriptException {
		Data data = elements.get(1).evaluate(context);
		String title = (String) data.getValue();
		data = elements.get(2).evaluate(context);
		String prompt = (String) data.getValue();
		String defaultValue;
		if (elements.size() > 3) {
			data = elements.get(3).evaluate(context);
			defaultValue = (String) data.getValue();
		} else {
			defaultValue = "";
		}

		final List<String> list = new ArrayList<String>();
		if (elements.size() > 4) {
			data = elements.get(4).evaluate(context);
			Object values = data.getValue();
			if (values instanceof Object[]) {
				Object[] options = (Object[]) values;
				for (Object object : options) {
					list.add((String) object);
				}
			} else {
				list.add((String) values);
			}
		}

		List<String> sel = PromptList.createAndShowGUI(title, prompt, list, false, defaultValue);
		// bei Abbruch -1
		if (sel == null) {
			return -1;
		}
		if (sel.size() == 0) {
			return "";
		}
		return sel.get(0);
	}

}
