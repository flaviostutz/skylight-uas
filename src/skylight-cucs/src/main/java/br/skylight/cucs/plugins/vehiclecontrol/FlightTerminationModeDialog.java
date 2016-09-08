package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import br.skylight.commons.ViewHelper;
import br.skylight.cucs.widgets.JMeasureSpinner;

public class FlightTerminationModeDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JMeasureSpinner<Integer> modeCode = null;
	private JButton ok = null;
	private JButton cancel = null;
	private JComboBox modeCodeCombo = null;
	GridBagConstraints gridBagConstraints1 = new GridBagConstraints();  //  @jve:decl-index=0:
	private boolean cancelled = true;
	
	public FlightTerminationModeDialog() {
		this(null);
	}
	
	/**
	 * @param owner
	 */
	public FlightTerminationModeDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(246, 127);
		this.setTitle("Flight Termination Mode Select");
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
		this.setContentPane(getJContentPane());
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
			jContentPane.add(getJPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.insets = new Insets(4, 10, 0, 10);
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(10, 10, 0, 0);
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Flight Termination mode:");
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(jLabel, gridBagConstraints);
			jPanel.add(getOk(), gridBagConstraints2);
			jPanel.add(getCancel(), gridBagConstraints3);
//			jPanel.add(getModeCodeCombo(), gridBagConstraints1);
		}
		return jPanel;
	}

	/**
	 * This method initializes modeCode	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getModeCode() {
		if (modeCode == null) {
			modeCode = new JMeasureSpinner<Integer>();
		}
		return modeCode;
	}

	/**
	 * This method initializes ok	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOk() {
		if (ok == null) {
			ok = new JButton();
			ok.setText("ARM");
			ok.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancelled = false;
					setVisible(false);
				}
			});
		}
		return ok;
	}

	/**
	 * This method initializes cancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancel() {
		if (cancel == null) {
			cancel = new JButton();
			cancel.setText("Cancel");
			cancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancelled = true;
					setVisible(false);
				}
			});
		}
		return cancel;
	}

	public static int showFlightTerminationModeInput(Map<Integer,String> modes) {
		FlightTerminationModeDialog f = new FlightTerminationModeDialog();
		if(modes!=null && modes.size()>0) {
			DefaultComboBoxModel cm = new DefaultComboBoxModel();
			for (Entry<Integer,String> e : modes.entrySet()) {
				cm.addElement(new Item(e.getValue(), e.getKey()));
			}
			f.getModeCodeCombo().setModel(cm);
			f.getJPanel().add(f.getModeCodeCombo(), f.gridBagConstraints1);
		} else {
			f.getModeCode().setValue(0);
			f.getJPanel().add(f.getModeCode(), f.gridBagConstraints1);
		}
		f.getOk().requestFocus();
		ViewHelper.centerWindow(f);
		f.setVisible(true);
		if(f.isCancelled()) {
			return -1;
		} else {
			return f.getInputValue();
		}
	}
	
	public int getInputValue() {
		if(getModeCodeCombo().getItemCount()>0) {
			return ((Item)getModeCodeCombo().getSelectedItem()).getMode();
		} else {
			return getModeCode().getValue();
		}
	}
	
	/**
	 * This method initializes modeCodeCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getModeCodeCombo() {
		if (modeCodeCombo == null) {
			modeCodeCombo = new JComboBox();
		}
		return modeCodeCombo;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	private static class Item {
		private String label;
		private int mode;
		
		public Item(String label, int mode) {
			this.label = label;
			this.mode = mode;
		}
		
		public int getMode() {
			return mode;
		}
		
		@Override
		public String toString() {
			return label;
		}
		
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
