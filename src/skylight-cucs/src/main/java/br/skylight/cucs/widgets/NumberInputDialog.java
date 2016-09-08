package br.skylight.cucs.widgets;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import br.skylight.commons.ViewHelper;

public class NumberInputDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JSpinner jSpinner = null;
	private boolean ok = false;

	/**
	 * @param owner
	 */
	public NumberInputDialog(Frame owner) {
		super(owner);
		initialize();
	}

	public NumberInputDialog(Dialog owner) {
		super(owner);
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(366, 138);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Value input");
		this.setContentPane(getJContentPane());
		this.setResizable(true);
		this.setModal(true);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				((JSpinner.NumberEditor) jSpinner.getEditor()).getTextField().requestFocus();
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.weightx = 0.0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.insets = new Insets(0, 15, 0, 15);
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(6, 6, 10, 6);
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.insets = new Insets(6, 6, 10, 6);
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.gridy = 2;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(15, 15, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Message:");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabel, gridBagConstraints);
			jContentPane.add(getJButton(), gridBagConstraints1);
			jContentPane.add(getJButton1(), gridBagConstraints2);
			jContentPane.add(getJSpinner(), gridBagConstraints3);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ok = true;
					setVisible(false);
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Cancel");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ok = false;
					setVisible(false);
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jSpinner	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getJSpinner() {
		if (jSpinner == null) {
			jSpinner = new JSpinner();//new SpinnerNumberModel(0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1));
		}
		return jSpinner;
	}
	
	public boolean isOk() {
		return ok;
	}
	
	public double getValue() {
		return (Double)getJSpinner().getValue();
	}
	
	public static Double showInputDialog(String message) {
		return showInputDialog(null, message, 0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1, 0, 9);
	}

	public static Double showInputDialog(Component parent, final String message, final double initialValue, final double minValue, final double maxValue, final double stepSize, int minFractionDigits, int maxFractionDigits) {
        Window window = JOptionPane.getFrameForComponent(parent);
        NumberInputDialog d = null;
        if (window instanceof Frame) {
    		d = new NumberInputDialog((Frame)window);
        } else {
    		d = new NumberInputDialog((Dialog)window);
        }
        ViewHelper.centerWindow(d);
        d.setTitle("Enter a number");
        d.jLabel.setText(message);
		d.setupSpinnerModel(initialValue, minValue, maxValue, stepSize, minFractionDigits, maxFractionDigits);
		d.setVisible(true);
		if(d.isOk()) {
			return d.getValue();
		} else {
			return null;
		}
	}

	private void setupSpinnerModel(double initialValue, double minValue, double maxValue, double stepSize, int minFractionDigits, int maxFractionDigits) {
		ViewHelper.setupSpinnerNumber(jSpinner, initialValue, minValue, maxValue, stepSize, minFractionDigits, maxFractionDigits);
		((JSpinner.NumberEditor) jSpinner.getEditor()).getTextField().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent ke) {
				if (ke.getKeyChar() == '\n') {
					getJButton().doClick();
				}
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent ke) {
			}
		});
		jSpinner.setValue(initialValue);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
