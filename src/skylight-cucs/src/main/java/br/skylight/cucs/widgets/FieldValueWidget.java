package br.skylight.cucs.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.jar.Attributes.Name;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import br.skylight.commons.MessageFieldDef;

public class FieldValueWidget extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FieldValueWidget.class.getName());  //  @jve:decl-index=0:
	
	private JLabel label = null;
	private JLabel value = null;
	private JToggleButton showGraph = null;
	
	private MessageFieldDef messageFieldData;
	
	/**
	 * @param owner
	 */
	public FieldValueWidget() {
		initialize();
	}

	public void setMessageFieldData(MessageFieldDef messageFieldData) {
		this.messageFieldData = messageFieldData;
		label.setText(messageFieldData.getLabel() + ":");
		value.setText(messageFieldData.getFormattedValue());
	}
	
	public MessageFieldDef getMessageFieldData() {
		return messageFieldData;
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.insets = new Insets(0, 3, 0, 3);
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 0;
		value = new JLabel();
		value.setText("value");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		label = new JLabel();
		label.setText("name:");
		this.setLayout(new GridBagLayout());
		this.setSize(188, 24);
		this.add(label, gridBagConstraints);
		this.add(value, gridBagConstraints1);
		this.add(getShowGraph(), gridBagConstraints2);
	}

	/**
	 * This method initializes showGraph	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getShowGraph() {
		if (showGraph == null) {
			showGraph = new JToggleButton();
			showGraph.setMargin(new Insets(0, 0, 0, 0));
			showGraph.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/graph.gif")));
		}
		return showGraph;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
