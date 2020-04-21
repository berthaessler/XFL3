package de.leonso.xfl.evaluators;

import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

// http://stackoverflow.com/questions/1341699/how-do-i-make-a-thread-wait-for-jframe-to-close-in-java#1342950
// http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/PasswordDemoProject/src/components/PasswordDemo.java

public class PromptPassword extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static String OK = "ok";

	private JPasswordField passwordField;

	private final JFrame controllingFrame;

	public PromptPassword(JFrame f, String prompt) {
		// Use the default FlowLayout.
		controllingFrame = f;

		// Create everything.
		passwordField = new JPasswordField(10);
		passwordField.setActionCommand(OK);
		passwordField.addActionListener(this);

		JLabel label = new JLabel(prompt);
		label.setLabelFor(passwordField);

		JComponent buttonPane = createButtonPanel();

		// Lay out everything.
		JPanel textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		textPane.add(label);
		textPane.add(passwordField);

		add(textPane);
		add(buttonPane);
	}

	protected JComponent createButtonPanel() {
		JPanel p = new JPanel(new GridLayout(0, 1));
		JButton okButton = new JButton("OK");

		okButton.setActionCommand(OK);
		okButton.addActionListener(this);

		p.add(okButton);

		return p;
	}

	private String password;
	private boolean enterPressed = false;

	public String getPassword() {
		return password;
	}

	public boolean isEnterPressed() {
		return enterPressed;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (OK.equals(cmd)) { // Process the password.
			char[] input = passwordField.getPassword();

			// wurde was eingegeben?
			if (input.length == 0) {
				resetFocus();
				return;
			}

			// merken für EventListener
			enterPressed = true;

			password = new String(input);
			// controllingFrame.setVisible(false);
			controllingFrame.dispose();
			// System.exit(0); beendet die Anwendung :(
			// Window window = SwingUtilities.windowForComponent((Component) e.getSource());
			// window.dispose();
		}
	}

	// Must be called from the event dispatch thread.
	protected void resetFocus() {
		passwordField.requestFocusInWindow();
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the event dispatch thread.
	 * 
	 * @return
	 */
	public static String createAndShowGUI(String title, String prompt) {
		// Create and set up the window.
		final JFrame frame = new JFrame(title);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		// Create and set up the content pane.
		final PromptPassword newContentPane = new PromptPassword(frame, prompt);
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
				if (newContentPane.isEnterPressed()) {
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
			return newContentPane.getPassword();
		} else {
			return null;
		}
	}

}
