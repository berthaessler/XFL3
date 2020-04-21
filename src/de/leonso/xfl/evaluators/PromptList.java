package de.leonso.xfl.evaluators;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.leonso.core.UtilsString;

// http://stackoverflow.com/questions/1341699/how-do-i-make-a-thread-wait-for-jframe-to-close-in-java#1342950
// http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/PasswordDemoProject/src/components/PasswordDemo.java

public class PromptList extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static String OK = "ok";
	private static String CANCEL = "cancel";

	private final JFrame controllingFrame;
	private final JScrollPane jScrollPane;

	public PromptList(JFrame frame, String prompt, List<String> list, boolean multi, String defaultValue) {
		this.multi = multi;
		// Use the default FlowLayout.
		controllingFrame = frame;

		// Create everything.
		final DefaultListModel listModel = new DefaultListModel();
		for (String s : list) {
			listModel.addElement(s);
		}

		setLayout(new BorderLayout(0, 0));

		JTextPane txtpnMessage = new JTextPane();
		add(txtpnMessage, BorderLayout.NORTH);
		txtpnMessage.setText(prompt);

		final JList jlist = new JList(listModel);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jlist.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				selection.clear();

				int selectedIndex = jlist.getSelectedIndex();
				if (selectedIndex < 0) {
					// nichts selektiert
					return;
				}
				Object sel = listModel.get(selectedIndex);
				selection.add(sel.toString());

			}
		});

		jScrollPane = new JScrollPane(jlist);
		add(jScrollPane, BorderLayout.WEST);

		// Defaultwert markieren
		if (!UtilsString.isNullOrEmpty(defaultValue)) {
			int ix = list.indexOf(defaultValue);
			// -1, wenn nicht enthalten
			if (ix >= 0) {
				jlist.setSelectedIndex(ix);
				selection.add(defaultValue);
			}
		}

		// Breite festlegen
		String max = "";
		for (String element : list) {
			if (max.length() < element.length()) {
				max = element;
			}
		}
		if (max.length() < 30) {
			// ein paar Stellen anhängen
			// http://stackoverflow.com/questions/1235179/simple-way-to-repeat-a-string-in-java?s=1|2.1629
			max += String.format("%0" + (30 - max.length()) + "d", 0).replace("0", "x");
		}
		jlist.setPrototypeCellValue(max);

		add(createButtonPanel(), BorderLayout.SOUTH);

	}

	private JComponent createButtonPanel() {
		JPanel buttons = new JPanel();

		JButton okButton = new JButton("OK");
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		okButton.setActionCommand(OK);
		okButton.addActionListener(this);
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttons.add(okButton);

		JButton cancelButton = new JButton("Abbrechen");
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelButton.setActionCommand(CANCEL);
		cancelButton.addActionListener(this);
		buttons.add(cancelButton);

		return buttons;
	}

	private boolean enterPressed = false;

	public boolean isEnterPressed() {
		return enterPressed;
	}

	private boolean cancelPressed = false;

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (OK.equals(cmd)) {

			// merken für EventListener
			enterPressed = true;

			// controllingFrame.setVisible(false);
			controllingFrame.dispose();
			// System.exit(0); beendet die Anwendung :(
			// Window window = SwingUtilities.windowForComponent((Component) e.getSource());
			// window.dispose();

		} else if (CANCEL.equals(cmd)) {

			// merken für EventListener
			cancelPressed = true;
			controllingFrame.dispose();

		}

	}

	// Must be called from the event dispatch thread.
	protected void resetFocus() {
		jScrollPane.requestFocusInWindow();
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event dispatch thread.
	 * 
	 * @return
	 */
	public static List<String> createAndShowGUI(String title, String prompt, List<String> list, boolean multi, String defaultValue) {
		// Create and set up the window.
		final JFrame frame = new JFrame(title);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		// frame.setAlwaysOnTop(true); // verhindern, dass modal "und" im Hintergrund - (Sch... Notes-Client)

		// in die Mitte positionieren
		// http://stackoverflow.com/questions/4627553/show-jframe-in-a-specific-screen-in-dual-monitor-configuration#4627773
		// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice[] gds = ge.getScreenDevices();
		// Rectangle bounds = gds[0].getDefaultConfiguration().getBounds();
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		int y = (int) Math.round((height - frame.getHeight()) / 2);
		int x = (int) Math.round((width - frame.getWidth()) / 2);
		frame.setLocation(x, y);

		// Create and set up the content pane.
		final PromptList newContentPane = new PromptList(frame, prompt, list, multi, defaultValue);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		final Object lock = new Object();

		frame.addWindowListener(new WindowAdapter() {

			// Make sure the focus goes to the right component
			// whenever the frame is initially given the focus.
			public void windowActivated(WindowEvent e) {
				newContentPane.resetFocus();
			}

			// Button OK löst das aus
			@Override
			public void windowDeactivated(WindowEvent e) {
				super.windowDeactivated(e);
				if (newContentPane.isEnterPressed() || newContentPane.isCancelPressed()) {
					synchronized (lock) {
						frame.setVisible(false);
						lock.notify();
					}
				}
			}

			// Fenster über X geschlossen
			@Override
			public void windowClosing(WindowEvent arg0) {
				synchronized (lock) {
					frame.setVisible(false);
					lock.notify();
				}
			}

		});

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		// warten auf Eingabe
		synchronized (lock) {
			while (frame.isVisible()) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (newContentPane.isEnterPressed()) {
			return newContentPane.getSelection();
		} else {
			return null;
		}
	}

	// Mehrfachauswahl erlaubt?
	private boolean multi;

	public boolean isMulti() {
		return multi;
	}

	private List<String> selection = new ArrayList<String>();

	public List<String> getSelection() {
		return selection;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	public void setCancelPressed(boolean cancelPressed) {
		this.cancelPressed = cancelPressed;
	}

}
