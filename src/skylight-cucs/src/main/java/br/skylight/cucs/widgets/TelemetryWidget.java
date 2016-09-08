package br.skylight.cucs.widgets;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;


public class TelemetryWidget extends JPanel {

	private static final long serialVersionUID = 1534378925088841531L;
	
	private JLabel label = null;
	private JLabel outputValue = null;
	private JButton actionButton = null;
	private String sufix;  //  @jve:decl-index=0:
	private String prefix;  //  @jve:decl-index=0:
	private String value;
	private TelemetryWidgetListener listener;
	
	public TelemetryWidget() {
		initialize();
	}
	
	private void initialize() {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.insets = new Insets(0, 4, 0, 0);
        gridBagConstraints2.gridy = 0;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new Insets(0, 3, 0, 0);
        gridBagConstraints1.gridy = 0;
        outputValue = new JLabel();
        outputValue.setText(" ");
        outputValue.setFont(new Font("Dialog", Font.PLAIN, 12));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.gridy = 0;
        label = new JLabel();
        label.setText("Attribute:");
        label.setPreferredSize(new Dimension(77, 14));
        label.setFont(new Font("Dialog", Font.PLAIN, 10));
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(193, 22));
        this.add(label, gridBagConstraints);
        this.add(outputValue, gridBagConstraints1);
        this.add(getActionButton(), gridBagConstraints2);
		
	}

	/**
	 * This method initializes actionButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getActionButton() {
		if (actionButton == null) {
			actionButton = new JButton();
			actionButton.setFont(new Font("Dialog", Font.PLAIN, 10));
			actionButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/groundstation/images/graph.gif")));
			actionButton.setMargin(ViewHelper.getMinimalButtonMargin());
			actionButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(listener!=null) listener.onButtonClicked();
				}
			});
		}
		return actionButton;
	}

	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSufix() {
		return sufix;
	}
	public void setSufix(String sufix) {
		this.sufix = sufix;
	}
	
	public void setLabel(String labelstr) {
		label.setText(labelstr);
	}
	public String getLabel() {
		return label.getText();
	}
	
	public void setButtonVisible(boolean visible) {
		getActionButton().setVisible(visible);
	}
	public boolean isButtonVisible() {
		return getActionButton().isVisible();
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
		outputValue.setText((prefix!=null?prefix:"") + value + (sufix!=null?sufix:""));
	}
	
	public void setTelemetryWidgetListener(TelemetryWidgetListener listener) {
		this.listener = listener;
	}
	public TelemetryWidgetListener getTelemetryWidgetListener() {
		return listener;
	}
	
	public void setButtonToolTip(String tip) {
		getActionButton().setToolTipText(tip);
	}
	public String getButtonToolTip() {
		return getActionButton().getToolTipText();
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
