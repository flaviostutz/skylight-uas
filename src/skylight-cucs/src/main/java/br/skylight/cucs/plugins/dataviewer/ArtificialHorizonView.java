package br.skylight.cucs.plugins.dataviewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.VehicleOperatingStates;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.subscriber.PreferencesListener;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon;

public class ArtificialHorizonView extends VehicleView implements MessageListener, PreferencesListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	private ArtificialHorizon artificialHorizon = null;
	
	@ServiceInjection
	public MessagingService messagingService;

	public ArtificialHorizonView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M104, this);
		subscriberService.addPreferencesListener(this);
		subscriberService.addPreferencesListener(this);
		onPreferencesUpdated();
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			//M101
			if(message instanceof InertialStates) {
				InertialStates m = (InertialStates)message;
				CUCSViewHelper.updateArtificialHorizonValues(m, getArtificialHorizon());
//				CUCSViewHelper.updateArtificialHorizonTargets(m, getArtificialHorizon());
				
			//M102
			} else if(message instanceof AirAndGroundRelativeStates) {
				AirAndGroundRelativeStates m = (AirAndGroundRelativeStates)message;
				CUCSViewHelper.updateArtificialHorizonValues(m, getArtificialHorizon());
//				CUCSViewHelper.updateArtificialHorizonTargets(m, getArtificialHorizon());
	
			//M104
			} else if(message instanceof VehicleOperatingStates) {
				VehicleOperatingStates m = (VehicleOperatingStates)message;
				CUCSViewHelper.updateArtificialHorizonTargets(m, artificialHorizon);
			}
			getArtificialHorizon().repaint();
			
			getRefresh().notifyFeedback();
		}
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 3;
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(218, 186));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getArtificialHorizon(), gridBagConstraints12);
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
			refresh.setup(subscriberService, messagingService, MessageType.M101, MessageType.M102, MessageType.M104);
		}
		return refresh;
	}

	@Override
	protected String getBaseTitle() {
		return "Artificial Horizon";
	}

	/**
	 * This method initializes artificialHorizon	
	 * 	
	 * @return br.skylight.cucs.widgets.ArtificialHorizon	
	 */
	private ArtificialHorizon getArtificialHorizon() {
		if (artificialHorizon == null) {
			artificialHorizon = new ArtificialHorizon();
		}
		return artificialHorizon;
	}

	@Override
	public void onPreferencesUpdated() {
		CUCSViewHelper.updateDisplayUnits(getArtificialHorizon());
		updateGUI();
	}

}
