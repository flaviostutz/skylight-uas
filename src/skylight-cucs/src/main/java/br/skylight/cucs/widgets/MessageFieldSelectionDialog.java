package br.skylight.cucs.widgets;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class MessageFieldSelectionDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JComboBox jComboBox = null;

	/**
	 * @param owner
	 */
	public MessageFieldSelectionDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setTitle("Message Field Selection");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJComboBox(), gridBagConstraints);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setModel(new DefaultComboBoxModel());
			for (MessageType mt : MessageType.values()) {
				try {
					Message m = mt.getImplementation().newInstance();
					for(int i=0; i<m.getFieldCount(); i++) {
						((DefaultComboBoxModel)jComboBox.getModel()).addElement(m.getMessageField(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return jComboBox;
	}

}
