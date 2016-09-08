package br.skylight.cucs.plugins.vehicleconfiguration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.Bitmapped;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.AirVehicleLights;
import br.skylight.commons.dli.vehicle.VehicleLightsState;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.RoundButton;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleLightsView extends VehicleView implements MessageListener {

	@ServiceInjection
	public MessagingService messagingService;
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="18,12"
	private JPanel lightsPanel = null;

	private RoundButton nav = null;
	private RoundButton landing = null;
	private RoundButton navIR = null;
	private RoundButton strobe = null;
	private RoundButton strobeIR = null;
	private RoundButton landingIR = null;
	private RoundButton nvd = null;

	private VehicleMessageRefreshButton refresh = null;
	
	public VehicleLightsView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M107, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M107
		if(message instanceof VehicleLightsState) {
			getRefresh().notifyFeedback();
			updateGUI();
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridy = 2;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(262, 190));
			contents.add(getLightsPanel(), gridBagConstraints1);
			updateGUI();
		}
		return contents;
	}

	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		if(!cucsHasGrantedLOI(MessageType.M44.getLOIs())) {
			enableControls(false);
		} else {
			enableControls(true);
			
			//LIGHTS
			VehicleLightsState s = (VehicleLightsState)getLastReceivedMessageForCurrentVehicle(MessageType.M107);
			if(s!=null) {
				Bitmapped l = s.getNavigationLightsState();
				nav.setSelected(l.isBit(1));
				navIR.setSelected(l.isBit(2));
				strobe.setSelected(l.isBit(3));
				strobeIR.setSelected(l.isBit(4));
				nvd.setSelected(l.isBit(5));
				landing.setSelected(l.isBit(7));
				landingIR.setSelected(l.isBit(8));
			}
		}
	}
	
	private void enableControls(boolean enable) {
		getRefresh().setEnabled(enable);
		getNav().setEnabled(enable);
		getNavIR().setEnabled(enable);
		getStrobe().setEnabled(enable);
		getStrobeIR().setEnabled(enable);
		getLanding().setEnabled(enable);
		getLandingIR().setEnabled(enable);
		getNvd().setEnabled(enable);
	}

	/**
	 * This method initializes lightsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLightsPanel() {
		if (lightsPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 3;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new Insets(2, 2, 5, 2);
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.gridy = 2;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 2;
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.insets = new Insets(2, 2, 2, 5);
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.gridy = 1;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(5, 2, 2, 2);
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.insets = new Insets(2, 5, 2, 2);
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.insets = new Insets(5, 2, 2, 5);
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(5, 5, 2, 2);
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.gridy = 0;
			lightsPanel = new JPanel();
			lightsPanel.setLayout(new GridBagLayout());
			lightsPanel.add(getNav(), gridBagConstraints2);
			lightsPanel.add(getLanding(), gridBagConstraints5);
			lightsPanel.add(getNavIR(), gridBagConstraints6);
			lightsPanel.add(getStrobe(), gridBagConstraints3);
			lightsPanel.add(getStrobeIR(), gridBagConstraints7);
			lightsPanel.add(getLandingIR(), gridBagConstraints8);
			lightsPanel.add(getNvd(), gridBagConstraints9);
			lightsPanel.add(getRefresh(), gridBagConstraints);
		}
		return lightsPanel;
	}

	/**
	 * This method initializes nav	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getNav() {
		if (nav == null) {
			nav = new RoundButton();
			nav.setMargin(ViewHelper.getMinimalButtonMargin());
			nav.setText("Nav");
			nav.setColorSelected(new Color(255, 255, 51));
			nav.setColorUnselected(new Color(184, 183, 183));
			nav.setRoundness(15);
			nav.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return nav;
	}

	protected void sendLightsCommand() {
		AirVehicleLights m = messagingService.resolveMessageForSending(AirVehicleLights.class);
		m.getSetLights().setBit(1, nav.getRequestedSelection());
		m.getSetLights().setBit(2, navIR.getRequestedSelection());
		m.getSetLights().setBit(3, strobe.getRequestedSelection());
		m.getSetLights().setBit(4, strobeIR.getRequestedSelection());
		m.getSetLights().setBit(5, nvd.getRequestedSelection());
		m.getSetLights().setBit(7, landing.getRequestedSelection());
		m.getSetLights().setBit(8, landingIR.getRequestedSelection());
		m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
		messagingService.sendMessage(m);
	}

	/**
	 * This method initializes landing	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getLanding() {
		if (landing == null) {
			landing = new RoundButton();
			landing.setMargin(ViewHelper.getMinimalButtonMargin());
			landing.setRoundness(15);
			landing.setColorSelected(new Color(255, 255, 51));
			landing.setColorUnselected(new Color(184, 183, 183));
			landing.setText("Landing");
			landing.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return landing;
	}

	/**
	 * This method initializes navIR	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private RoundButton getNavIR() {
		if (navIR == null) {
			navIR = new RoundButton();
			navIR.setMargin(ViewHelper.getMinimalButtonMargin());
			navIR.setRoundness(15);
			navIR.setColorSelected(new Color(255, 255, 51));
			navIR.setColorUnselected(new Color(184, 183, 183));
			navIR.setText("Nav IR");
			navIR.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return navIR;
	}

	/**
	 * This method initializes strobe	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getStrobe() {
		if (strobe == null) {
			strobe = new RoundButton();
			strobe.setMargin(ViewHelper.getMinimalButtonMargin());
			strobe.setRoundness(15);
			strobe.setColorSelected(new Color(255, 255, 51));
			strobe.setColorUnselected(new Color(184, 183, 183));
			strobe.setText("Strobe");
			strobe.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return strobe;
	}

	/**
	 * This method initializes strobeIR	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getStrobeIR() {
		if (strobeIR == null) {
			strobeIR = new RoundButton();
			strobeIR.setMargin(ViewHelper.getMinimalButtonMargin());
			strobeIR.setRoundness(15);
			strobeIR.setColorSelected(new Color(255, 255, 51));
			strobeIR.setColorUnselected(new Color(184, 183, 183));
			strobeIR.setText("Strobe IR");
			strobeIR.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return strobeIR;
	}

	/**
	 * This method initializes landingIR	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getLandingIR() {
		if (landingIR == null) {
			landingIR = new RoundButton();
			landingIR.setMargin(ViewHelper.getMinimalButtonMargin());
			landingIR.setRoundness(15);
			landingIR.setColorSelected(new Color(255, 255, 51));
			landingIR.setColorUnselected(new Color(184, 183, 183));
			landingIR.setText("Landing IR");
			landingIR.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return landingIR;
	}

	/**
	 * This method initializes nvd	
	 * 	
	 * @return javax.swing.RoundButton	
	 */
	private RoundButton getNvd() {
		if (nvd == null) {
			nvd = new RoundButton();
			nvd.setMargin(ViewHelper.getMinimalButtonMargin());
			nvd.setRoundness(15);
			nvd.setColorSelected(new Color(255, 255, 51));
			nvd.setColorUnselected(new Color(184, 183, 183));
			nvd.setText("NVD");
			nvd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendLightsCommand();
				}
			});
		}
		return nvd;
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle Lights";
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
			refresh.setup(subscriberService, messagingService, MessageType.M107);
		}
		return refresh;
	}

}
