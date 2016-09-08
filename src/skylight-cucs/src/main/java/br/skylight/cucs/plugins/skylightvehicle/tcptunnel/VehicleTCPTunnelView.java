package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.streamchannel.StreamListener;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleTCPTunnelView extends VehicleView {

	private JPanel contents; // @jve:decl-index=0:visual-constraint="17,-1"

	@ServiceInjection
	public PluginManager pluginManager;
	
	@MemberInjection
	public TelnetClientOperator telnetClientOperator;
	
	@MemberInjection
	public JMXClientOperator jmxClientOperator;
	
	@ServiceInjection
	public UserService userService;

	private JToggleButton telnetTunnelButton = null;

	private JLabel telnetStatus = null;

	private JToggleButton jmxTunnelButton = null;

	private JLabel jmxStatus = null;
	
	public VehicleTCPTunnelView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle TCP Tunnel";
	}

	@Override
	protected void updateGUI() {
		if (getCurrentVehicle() != null) {
			try {
				//drop session with other vehicle
				if(telnetClientOperator.isChannelOpen() && telnetClientOperator.getVehicleId()!=getCurrentVehicle().getVehicleID().getVehicleID()) {
					telnetClientOperator.closeChannel();
				}
			} catch (IOException e) {
				ViewHelper.showException(e);
			}
		}
	}

	@Override
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(2, 3, 0, 5);
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			jmxStatus = new JLabel();
			jmxStatus.setText("Tunnel not open");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.weightx = 0.0;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.insets = new Insets(2, 5, 0, 0);
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.insets = new Insets(0, 3, 0, 5);
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridy = 0;
			telnetStatus = new JLabel();
			telnetStatus.setText("Tunnel not open");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints.gridy = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(268, 126));
			contents.add(getTelnetTunnelButton(), gridBagConstraints);
			contents.add(telnetStatus, gridBagConstraints1);
			contents.add(getJmxTunnelButton(), gridBagConstraints11);
			contents.add(jmxStatus, gridBagConstraints2);
		}
		return contents;
	}

	/**
	 * This method initializes telnetTunnelButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getTelnetTunnelButton() {
		if (telnetTunnelButton == null) {
			telnetTunnelButton = new JToggleButton();
			telnetTunnelButton.setText("Start telnet tunnel");
			telnetTunnelButton.setMargin(ViewHelper.getDefaultButtonMargin());
			telnetClientOperator.addStreamListener(new StreamListener() {
				@Override
				public void onChannelOpened() {
					telnetStatus.setText("Client connected to port " + telnetClientOperator.getListenPort());
				}
				@Override
				public void onChannelClosed() {
					telnetStatus.setText("Tunnel not open");
				}
			});
			telnetTunnelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(telnetTunnelButton.isSelected()) {
						telnetTunnelButton.setText("Stop telnet tunnel");
						telnetStatus.setText("Port " + telnetClientOperator.getListenPort() + " open");
						Thread t = new Thread() {
							public void run() {
								try {
									telnetClientOperator.openChannel(userService.getCurrentCucsId(), getCurrentVehicle().getVehicleID().getVehicleID());
								} catch (Exception e) {
									ViewHelper.showException(e);
								}
							};
						};
						t.start();
					} else {
						telnetStatus.setText("Tunnel not open");
						try {
							telnetClientOperator.closeChannel();
						} catch (IOException e1) {
							ViewHelper.showException(e1);
						}
						telnetTunnelButton.setText("Start telnet tunnel");
					}
				}
			});
		}
		return telnetTunnelButton;
	}

	/**
	 * This method initializes jmxTunnelButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJmxTunnelButton() {
		if (jmxTunnelButton == null) {
			jmxTunnelButton = new JToggleButton();
			jmxTunnelButton.setMargin(ViewHelper.getDefaultButtonMargin());
			jmxTunnelButton.setText("Start JMX tunnel");
			jmxTunnelButton.setMargin(ViewHelper.getDefaultButtonMargin());
			jmxClientOperator.addStreamListener(new StreamListener() {
				@Override
				public void onChannelOpened() {
					jmxStatus.setText("Client connected to port " + jmxClientOperator.getListenPort());
				}
				@Override
				public void onChannelClosed() {
					jmxStatus.setText("Tunnel not open");
				}
			});
			jmxTunnelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(jmxTunnelButton.isSelected()) {
						jmxTunnelButton.setText("Stop JMX tunnel");
						jmxStatus.setText("Port " + jmxClientOperator.getListenPort() + " open");
						Thread t = new Thread(){
							public void run() {
								try {
									jmxClientOperator.openChannel(userService.getCurrentCucsId(), getCurrentVehicle().getVehicleID().getVehicleID());
								} catch (IOException e1) {
									ViewHelper.showException(e1);
								}
							};
						};
						t.start();
					} else {
						jmxStatus.setText("Tunnel not open");
						try {
							jmxClientOperator.closeChannel();
						} catch (IOException e1) {
							ViewHelper.showException(e1);
						}
						jmxTunnelButton.setText("Start JMX tunnel");
					}
				}
			});
		}
		return jmxTunnelButton;
	}

}
