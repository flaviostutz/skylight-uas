package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTable;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.SoftwarePartReport;
import br.skylight.commons.dli.skylight.SoftwareStatus;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.ScrollTextViewDialog;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTableModel;

public class VehicleSoftwareStatusView extends VehicleView implements MessageListener {

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public SubscriberService subscriberService;

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"

	private JScrollPane jScrollPane = null;

	private JXTable jTable = null;

	private VehicleMessageRefreshButton refresh = null;

	private JLabel version = null;
	private Map<String,SoftwarePartReport> softwareParts = new HashMap<String,SoftwarePartReport>();  //  @jve:decl-index=0:

	private VehicleMessageRefreshButton showDetails = null;

	public VehicleSoftwareStatusView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M2013, this);
		subscriberService.addMessageListener(MessageType.M2018, this);
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.anchor = GridBagConstraints.EAST;
			gridBagConstraints21.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(2, 2, 0, 0);
			gridBagConstraints.gridy = 1;
			version = new JLabel();
			version.setText("Version: -");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 2;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridx = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(210, 149));
			contents.add(getRefresh(), gridBagConstraints1);
			contents.add(getJScrollPane(), gridBagConstraints2);
			contents.add(version, gridBagConstraints);
			contents.add(getShowDetails(), gridBagConstraints21);
		}
		return contents;
	}

	@Override
	public void onMessageReceived(Message message) {
		//M2013
		if(message instanceof SoftwareStatus) {
			SoftwareStatus ss = (SoftwareStatus)message;
			if(isMessageFromCurrentVehicle(ss)) {
				updateGUI();
				getRefresh().notifyFeedback();
			}

		//M2018
		} else if(message instanceof SoftwarePartReport) {
			SoftwarePartReport m = (SoftwarePartReport)message;
			softwareParts.put(m.getName(), (SoftwarePartReport)m.createCopy());
			((TypedTableModel<SoftwarePartReport>)getJXTable().getModel()).setUserObjects(new ArrayList<SoftwarePartReport>(softwareParts.values()));
			getJXTable().updateUI();
		}
	}
	
	@Override
	protected String getBaseTitle() {
		return "Vehicle Software Status";
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		TypedTableModel<SoftwarePartReport> parts = (TypedTableModel<SoftwarePartReport>)getJXTable().getModel();
		if(getCurrentVehicle()!=null && getCurrentVehicle().getLastReceivedMessage(MessageType.M2013)!=null) {
			SoftwareStatus ss = getCurrentVehicle().getLastReceivedMessage(MessageType.M2013);
			version.setText("Autopilot version: "+ss.getSoftwareVersion());
		} else {
			parts.setUserObjects(new ArrayList<SoftwarePartReport>());
			getJXTable().updateUI();
		}
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJXTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JXTable	
	 */
	private JXTable getJXTable() {
		if (jTable == null) {
			jTable = new JXTable();
			TypedTableModel<SoftwarePartReport> model = new TypedTableModel<SoftwarePartReport>(new ObjectToColumnAdapter<SoftwarePartReport>() {
				public Object getValueAt(SoftwarePartReport s, int columnIndex) {
					if(columnIndex==0) {
						return s.getName();
					} else if(columnIndex==1) {
						return s.getAverageFrequency();
					} else if(columnIndex==2) {
						return s.getTimeSinceLastStepMillis();
					} else if(columnIndex==3) {
						return s.isActive();
					} else if(columnIndex==4) {
						return s.isAlert();
					} else if(columnIndex==5) {
						return s.isTimeout();
					} else if(columnIndex==6) {
						return s.getExceptionCount();
					} else if(columnIndex==7) {
						return s.getStackFragment();
					}
					return null;
				}
				public void setValueAt(SoftwarePartReport s, Object value, int columnIndex) {
				}
			}, "Name", "Avg freq (Hz)", "Step time (ms)", "Active", "Time alert", "Timeout", "Exceptions", "Stack");
			
			jTable.setModel(model);
		}
		return jTable;
	}

	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.VehicleMessageRefreshButton	
	 */
	private VehicleMessageRefreshButton getRefresh() {
		if (refresh == null) {
			refresh = new VehicleMessageRefreshButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.setup(subscriberService, messagingService, MessageType.M2013, MessageType.M2018);
		}
		return refresh;
	}

	/**
	 * This method initializes showDetails	
	 * 	
	 * @return br.skylight.cucs.widgets.VehicleMessageRefreshButton	
	 */
	private VehicleMessageRefreshButton getShowDetails() {
		if (showDetails == null) {
			showDetails = new VehicleMessageRefreshButton();
			showDetails.setToolTipText("Show stack traces");
			showDetails.setMargin(ViewHelper.getMinimalButtonMargin());
			showDetails.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/attributes.gif")));
			showDetails.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ScrollTextViewDialog t = new ScrollTextViewDialog(null);
					String st = "";
					for (SoftwarePartReport sp : ((TypedTableModel<SoftwarePartReport>)getJXTable().getModel()).getUserObjects()) {
						st += sp.getName() + " " + sp.getAverageFrequency() + " Hz\n";
						st += sp.getStackFragment() + "\n";
					}
					t.showDialog(st, "Thread stack traces");
				}
			});
		}
		return showDetails;
	}

}
