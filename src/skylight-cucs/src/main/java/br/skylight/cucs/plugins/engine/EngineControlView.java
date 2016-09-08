package br.skylight.cucs.plugins.engine;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.EngineOperatingStates;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class EngineControlView extends VehicleView implements MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;

	@ServiceInjection
	public MessagingService messagingService;
	private JTabbedPane tabbedPane = null;
	private Vehicle lastVehicle = null;  //  @jve:decl-index=0:
	private Map<Integer,EngineControlPanel> engines = new HashMap<Integer,EngineControlPanel>();  //  @jve:decl-index=0:
	
	@ServiceInjection
	public GameControllerService gameControllerService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	public EngineControlView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M105, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			//M105
			if(message instanceof EngineOperatingStates) {
				EngineOperatingStates m = (EngineOperatingStates)message;
				resolveEngineControlPanel(m.getEngineNumber()).setEngineOperatingStates(m);
				getTabbedPane().repaint();
				getRefresh().notifyFeedback();
			}
			
			updateGUI();
		}
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		if(getCurrentVehicle()!=null) {
			//refresh current engines tabbed pane
			if(lastVehicle!=getCurrentVehicle()) {
				engines.clear();
				getTabbedPane().removeAll();
				lastVehicle = getCurrentVehicle();
			}
		}
	}
	
	private EngineControlPanel resolveEngineControlPanel(int engineId) {
		EngineControlPanel r = engines.get(engineId);
		if(r==null) {
			r = new EngineControlPanel();
			r.setMessagingService(messagingService);
			r.setSize(new Dimension(163, 124));
			getTabbedPane().addTab("Engine " + engineId, r);
			engines.put(engineId, r);
		}
		return r;
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.fill = GridBagConstraints.BOTH;
			gridBagConstraints17.gridy = 0;
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.weighty = 1.0;
			gridBagConstraints17.gridx = 7;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 7;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 2;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(218, 186));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getTabbedPane(), gridBagConstraints17);
		}
		return contents;
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
			refresh.setup(subscriberService, messagingService, MessageType.M105);
		}
		return refresh;
	}

	@Override
	protected String getBaseTitle() {
		return "Engine Control";
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
		}
		return tabbedPane;
	}

}
