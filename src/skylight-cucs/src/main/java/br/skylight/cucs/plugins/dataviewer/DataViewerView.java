package br.skylight.cucs.plugins.dataviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.MessageFieldSelectionDialog;

public class DataViewerView extends View<DataViewerState> implements MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private JXTable dataTable = null;
	private JPanel graphsPanel = null;  //  @jve:decl-index=0:visual-constraint="348,32"
	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="284,79"
	private JButton jButton = null;

	@ServiceInjection
	public SubscriberService subscriberService;  //  @jve:decl-index=0:
	
	public DataViewerView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		subscriberService.addMessageListener(MessageType.M101, this);
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints.gridx = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(252, 125));
			contents.add(getDataTable(), gridBagConstraints);
			contents.add(getGraphsPanel(), gridBagConstraints11);
		}
		return contents;
	}


	@Override
	protected DataViewerState instantiateState() {
		return new DataViewerState();
	}

	@Override
	protected void prepareState() {
	}

	@Override
	protected void onStateUpdated() {
	}

	/**
	 * This method initializes dataTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JXTable getDataTable() {
		if (dataTable == null) {
			dataTable = new JXTable();
			dataTable.setModel(new MessageFieldTableModel());
			DataViewerActionCellRenderer renderer = new DataViewerActionCellRenderer();
			renderer.setGraphsPanel(getGraphsPanel());
			dataTable.getColumn(2).setCellRenderer(renderer);
		}
		return dataTable;
	}

	@Override
	public void onMessageReceived(Message message) {
		MessageFieldTableModel tm = (MessageFieldTableModel)getDataTable().getModel();
		tm.updateMessage(message);
	}

	/**
	 * This method initializes graphsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGraphsPanel() {
		if (graphsPanel == null) {
			graphsPanel = new JPanel();
			graphsPanel.setLayout(new BoxLayout(getGraphsPanel(), BoxLayout.Y_AXIS));
			graphsPanel.setSize(new Dimension(161, 100));
		}
		return graphsPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints1.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setSize(new Dimension(163, 22));
			jPanel.add(getJButton(), gridBagConstraints1);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Select...");
			jButton.setMargin(new Insets(0,0,0,0));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					MessageFieldSelectionDialog d = new MessageFieldSelectionDialog(null);
					d.setVisible(true);
				}
			});
		}
		return jButton;
	}

	protected Component getThis() {
		return getContents();
	}

}
