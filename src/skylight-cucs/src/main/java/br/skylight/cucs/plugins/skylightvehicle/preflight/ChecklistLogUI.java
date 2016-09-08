package br.skylight.cucs.plugins.skylightvehicle.preflight;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import br.skylight.commons.ViewHelper;

public class ChecklistLogUI extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JScrollPane jScrollPane = null;
	private JTextArea logArea = null;

	/**
	 * @param owner
	 */
	public ChecklistLogUI(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(450, 500);
		this.setTitle("Checklist logs");
		this.setContentPane(getJContentPane());
	}

	public void showContents(String contents) {
		this.setVisible(true);
		getLogArea().setText(contents);
		ViewHelper.centerWindow(this);
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getLogArea());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes logArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getLogArea() {
		if (logArea == null) {
			logArea = new JTextArea();
			logArea.setEditable(false);
		}
		return logArea;
	}
	
	public void updateContents(String contents) {
		getLogArea().setText(contents);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
