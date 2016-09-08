package br.skylight.cucs.plugins.communications;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.SamplesGraph;
import br.skylight.cucs.widgets.SamplesGraphModel;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class DataTerminalControlView extends VehicleView implements MessageListener {

	private JPanel contents1;
	private SamplesGraph gdtSamplesGraph = null;
	private JLabel jLabel = null;
	private JButton gdtSetup = null;
	private JButton gdtInfo = null;
	private JLabel gdtPercent = null;
	private JPanel contents2 = null;
	private JPanel contents = null;  //  @jve:decl-index=0:visual-constraint="46,-13"
	private SamplesGraph adtSamplesGraph = null;
	private JLabel jLabel1 = null;
	private JButton adtSetup = null;
	private JButton adtInfo = null;
	private JLabel adtPercent = null;
	private JSplitPane jSplitPane = null;
	private Vehicle lastSelectedVehicle = null;
	private JLabel adtMode = null;
	private JLabel gdtMode = null;
	
	private DataTerminalSetupDialog setupDialog;
	private DataTerminalControlDialog controlDialog;
	private JButton adtControl = null;
	private JButton gdtControl = null;
	private JPanel jPanel1 = null;
	private VehicleMessageRefreshButton refresh = null;
	
	@ServiceInjection
	public PluginManager pluginManager;

	@ServiceInjection
	public MessagingService messagingService;
	
	public DataTerminalControlView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	protected void updateGUI() {
		//don't hide setup controls when there is no status because maybe data terminal is only 
		//receiving messages and can't send a status report
		getAdtSetup().setEnabled(getCurrentVehicle()!=null);
		getGdtSetup().setEnabled(getCurrentVehicle()!=null);
		getAdtControl().setEnabled(getCurrentVehicle()!=null);
		getGdtControl().setEnabled(getCurrentVehicle()!=null);
		getAdtInfo().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getAdtDataLinkStatusReport()!=null);
		getGdtInfo().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getGdtDataLinkStatusReport()!=null);
		getRefresh().setVehicle(getCurrentVehicle());
		if(getCurrentVehicle()!=lastSelectedVehicle) {
			gdtPercent.setText("-");
			adtPercent.setText("-");
			getAdtSamplesGraph().getModel("adt").clear();
			getGdtSamplesGraph().getModel("gdt").clear();
			lastSelectedVehicle = getCurrentVehicle();
		}
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.gridx = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(231, 152));
			contents.add(getJSplitPane(), gridBagConstraints2);
		}
		return contents;
	}
	
	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M501, this);
	}
	
	protected JPanel getContents1() {
		if(contents1==null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 2;
			gridBagConstraints31.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 4;
			gridBagConstraints7.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints7.insets = new Insets(3, 4, 0, 5);
			gridBagConstraints7.gridy = 0;
			gdtPercent = new JLabel();
			gdtPercent.setText("0%");
			gdtPercent.setToolTipText("This is the indication of how strong the vehicle signal is being received by ground station");
			gdtPercent.setFont(new Font("Tahoma", Font.PLAIN, 20));
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 3;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(0, 2, 0, 0);
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.insets = new Insets(4, 4, 0, 0);
			gridBagConstraints4.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("GDT");
			jLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridwidth = 5;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridy = 1;
			contents1 = new JPanel();
			contents1.setLayout(new GridBagLayout());
			contents1.add(getGdtSamplesGraph(), gridBagConstraints);
			contents1.add(jLabel, gridBagConstraints4);
			contents1.add(getGdtSetup(), gridBagConstraints5);
			contents1.add(getGdtInfo(), gridBagConstraints6);
			contents1.add(gdtPercent, gridBagConstraints7);
			contents1.add(getGdtControl(), gridBagConstraints31);
		}
		return contents1;
	}

	@Override
	protected String getBaseTitle() {
		return "Data Terminal Control";
	}

	/**
	 * This method initializes gdtSamplesGraph	
	 * 	
	 * @return br.skylight.cucs.widgets.SamplesGraph	
	 */
	private SamplesGraph getGdtSamplesGraph() {
		if (gdtSamplesGraph == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.gridy = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints8.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints8.fill = GridBagConstraints.NONE;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.gridy = 0;
			gdtMode = new JLabel();
			gdtMode.setText(" ");
			gdtMode.setFont(new Font("Tahoma", Font.PLAIN, 10));
			gdtSamplesGraph = new SamplesGraph();
			gdtSamplesGraph.add(gdtMode, gridBagConstraints8);
			gdtSamplesGraph.add(getJPanel1(), gridBagConstraints10);
			SamplesGraphModel m = new SamplesGraphModel();
			m.setColor(new Color(0.5F,1,0.33F,0.8F));
			gdtSamplesGraph.addModel("gdt", m);
		}
		return gdtSamplesGraph;
	}

	/**
	 * This method initializes gdtSetup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGdtSetup() {
		if (gdtSetup == null) {
			gdtSetup = new JButton();
			gdtSetup.setText("Setup");
			gdtSetup.setMargin(ViewHelper.getDefaultButtonMargin());
			gdtSetup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getSetupDialog().showDialog(getCurrentVehicle().getGdtDataLinkStatusReport(), getCurrentVehicle().getVehicleID().getVehicleID(), DataTerminalType.GDT);
				}
			});
		}
		return gdtSetup;
	}

	/**
	 * This method initializes gdtInfo	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGdtInfo() {
		if (gdtInfo == null) {
			gdtInfo = new JButton();
			gdtInfo.setText("Info");
			gdtInfo.setMargin(ViewHelper.getDefaultButtonMargin());
			gdtInfo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DataTerminalInfoDialog d = new DataTerminalInfoDialog(null);
					d.showInfo(getCurrentVehicle().getGdtDataLinkStatusReport());
				}
			});
		}
		return gdtInfo;
	}

	/**
	 * This method initializes contents2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContents2() {
		if (contents2 == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints71.gridx = 4;
			gridBagConstraints71.gridy = 0;
			gridBagConstraints71.insets = new Insets(3, 4, 0, 5);
			adtPercent = new JLabel();
			adtPercent.setFont(new Font("Tahoma", Font.PLAIN, 20));
			adtPercent.setToolTipText("This is the indication of how strong the ground station signal is being received by vehicle");
			adtPercent.setText("0%");
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.anchor = GridBagConstraints.WEST;
			gridBagConstraints61.gridx = 3;
			gridBagConstraints61.gridy = 0;
			gridBagConstraints61.weightx = 1.0;
			gridBagConstraints61.insets = new Insets(0, 2, 0, 0);
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.anchor = GridBagConstraints.EAST;
			gridBagConstraints51.gridx = 1;
			gridBagConstraints51.gridy = 0;
			gridBagConstraints51.weightx = 1.0;
			gridBagConstraints51.insets = new Insets(0, 0, 0, 2);
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.gridy = 0;
			gridBagConstraints41.insets = new Insets(4, 4, 0, 0);
			jLabel1 = new JLabel();
			jLabel1.setFont(new Font("Tahoma", Font.BOLD, 14));
			jLabel1.setText("ADT");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridwidth = 5;
			contents2 = new JPanel();
			contents2.setLayout(new GridBagLayout());
			contents2.add(getAdtSamplesGraph(), gridBagConstraints1);
			contents2.add(jLabel1, gridBagConstraints41);
			contents2.add(getAdtSetup(), gridBagConstraints51);
			contents2.add(getAdtInfo(), gridBagConstraints61);
			contents2.add(adtPercent, gridBagConstraints71);
			contents2.add(getAdtControl(), gridBagConstraints9);
		}
		return contents2;
	}

	/**
	 * This method initializes adtSamplesGraph	
	 * 	
	 * @return br.skylight.cucs.widgets.SamplesGraph	
	 */
	private SamplesGraph getAdtSamplesGraph() {
		if (adtSamplesGraph == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridy = 0;
			adtMode = new JLabel();
			adtMode.setText(" ");
			adtMode.setFont(new Font("Tahoma", Font.PLAIN, 10));
			adtSamplesGraph = new SamplesGraph();
			adtSamplesGraph.add(adtMode, gridBagConstraints3);
			SamplesGraphModel m = new SamplesGraphModel();
			m.setColor(new Color(1,1,0.16F,0.8F));
			adtSamplesGraph.addModel("adt", m);
		}
		return adtSamplesGraph;
	}

	/**
	 * This method initializes adtSetup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdtSetup() {
		if (adtSetup == null) {
			adtSetup = new JButton();
			adtSetup.setMargin(ViewHelper.getDefaultButtonMargin());
			adtSetup.setText("Setup");
			adtSetup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getSetupDialog().showDialog(getCurrentVehicle().getAdtDataLinkStatusReport(), getCurrentVehicle().getVehicleID().getVehicleID(), DataTerminalType.ADT);
				}
			});
		}
		return adtSetup;
	}

	private DataTerminalSetupDialog getSetupDialog() {
		if(setupDialog==null) {
			setupDialog = new DataTerminalSetupDialog(null);
			pluginManager.manageObject(setupDialog);
		}
		return setupDialog;
	}
	
	private DataTerminalControlDialog getControlDialog() {
		if(controlDialog==null) {
			controlDialog = new DataTerminalControlDialog(null);
			pluginManager.manageObject(controlDialog);
		}
		return controlDialog;
	}
	
	/**
	 * This method initializes adtInfo	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdtInfo() {
		if (adtInfo == null) {
			adtInfo = new JButton();
			adtInfo.setMargin(ViewHelper.getDefaultButtonMargin());
			adtInfo.setText("Info");
			adtInfo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DataTerminalInfoDialog d = new DataTerminalInfoDialog(null);
					d.showInfo(getCurrentVehicle().getAdtDataLinkStatusReport());
				}
			});
		}
		return adtInfo;
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setDividerSize(3);
			jSplitPane.setTopComponent(getContents2());
			jSplitPane.setBottomComponent(getContents1());
			jSplitPane.setDividerLocation(60);
		}
		return jSplitPane;
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			getRefresh().notifyFeedback();
			//M501
			if(message instanceof DataLinkStatusReport) {
				DataLinkStatusReport m = (DataLinkStatusReport)message;
				if(m.getAddressedTerminal().equals(DataTerminalType.ADT)) {
					adtPercent.setText(m.getDownlinkStatus() +"%");
					adtMode.setText(m.getDataLinkState().getName());
					getAdtSamplesGraph().getModel("adt").addSample(m.getDownlinkStatus());
					updateGUI();
				} else if(m.getAddressedTerminal().equals(DataTerminalType.GDT)) {
					gdtPercent.setText(m.getDownlinkStatus() +"%");
					gdtMode.setText(m.getDataLinkState().getName());
					getGdtSamplesGraph().getModel("gdt").addSample(m.getDownlinkStatus());
					updateGUI();
				}
			}
		}
	}

	/**
	 * This method initializes adtControl	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdtControl() {
		if (adtControl == null) {
			adtControl = new JButton();
			adtControl.setMargin(ViewHelper.getDefaultButtonMargin());
			adtControl.setText("Control");
			adtControl.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getControlDialog().showDialog(getCurrentVehicle().getAdtDataLinkStatusReport(), getCurrentVehicle().getVehicleID().getVehicleID(), DataTerminalType.ADT);
				}
			});
		}
		return adtControl;
	}

	/**
	 * This method initializes gdtControl	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGdtControl() {
		if (gdtControl == null) {
			gdtControl = new JButton();
			gdtControl.setMargin(ViewHelper.getDefaultButtonMargin());
			gdtControl.setText("Control");
			gdtControl.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getControlDialog().showDialog(getCurrentVehicle().getGdtDataLinkStatusReport(), getCurrentVehicle().getVehicleID().getVehicleID(), DataTerminalType.GDT);
				}
			});
		}
		return gdtControl;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.anchor = GridBagConstraints.EAST;
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(3, 3, 3, 3);
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getRefresh(), gridBagConstraints11);
		}
		return jPanel1;
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
			refresh.setEnabled(false);
			refresh.setup(subscriberService, messagingService, MessageType.M501);
		}
		return refresh;
	}

}
