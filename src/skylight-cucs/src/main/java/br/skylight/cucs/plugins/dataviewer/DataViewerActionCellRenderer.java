package br.skylight.cucs.plugins.dataviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableCellRenderer;

import br.skylight.commons.MessageFieldDef;

public class DataViewerActionCellRenderer extends JPanel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	private JToggleButton actionButton;
	private MessageFieldDef messageFieldDef;
	private JPanel graphsPanel;
	private JLabel label;
	
	public DataViewerActionCellRenderer() {
		initialize();
	}
	
	public void setGraphsPanel(JPanel graphsPanel) {
		this.graphsPanel = graphsPanel;
	}
	
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		setLayout(new GridBagLayout());
		setSize(new Dimension(37, 20));
		add(getActionButton(), gridBagConstraints1);
	}

	private Component getActionButton() {
		if(actionButton==null) {
			actionButton = new JToggleButton();
			actionButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/graph.gif")));
			actionButton.setMargin(new Insets(0,0,0,0));
			actionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(actionButton.isSelected()) {
						label = new JLabel(messageFieldDef.getFormattedValue());
						graphsPanel.add(label);
					} else {
						graphsPanel.remove(label);
					}
				}
			});
		}
		return actionButton;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof MessageFieldDef) {
			this.messageFieldDef = (MessageFieldDef)value;
		}
		return this;
	}

}
