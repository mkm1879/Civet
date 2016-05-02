package edu.clemson.lph.civet.prefs;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class TestPanel extends JPanel {
	/**
	 * @wbp.nonvisual location=23,11
	 */
	private final JLabel label = new JLabel("New label");
	/**
	 * @wbp.nonvisual location=124,11
	 */
	private final JTextField textField = new JTextField();

	/**
	 * Create the panel.
	 */
	public TestPanel() {
		textField.setColumns(10);
		setLayout(null);

	}
}
