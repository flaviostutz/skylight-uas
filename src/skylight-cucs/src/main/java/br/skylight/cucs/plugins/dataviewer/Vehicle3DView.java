package br.skylight.cucs.plugins.dataviewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.j3d.Airplane3dViewer;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class Vehicle3DView extends VehicleView implements MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"

	private VehicleMessageRefreshButton refresh = null;

	private Airplane3dViewer airplane3dViewer = null;

	private JToggleButton updateRoll = null;

	private JToggleButton updatePitch = null;

	private JToggleButton updateYaw = null;
	
	@ServiceInjection
	public MessagingService messagingService;

	private JLabel jLabel = null;  //  @jve:decl-index=0:visual-constraint="307,42"

	public Vehicle3DView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M101, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			//M101
			if(message instanceof InertialStates) {
				InertialStates m = (InertialStates)message;
				getAirplane3dViewer().setOrientation(getUpdateRoll().isSelected()?m.getPhi():0, getUpdatePitch().isSelected()?m.getTheta():0, getUpdateYaw().isSelected()?-m.getPsi():0);
				getRefresh().notifyFeedback();
			}
		}
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.insets = new Insets(3, 0, 3, 3);
			gridBagConstraints4.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(3, 0, 3, 3);
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridwidth = 4;
			gridBagConstraints1.gridy = 0;
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
			contents.setSize(new Dimension(239, 152));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getAirplane3dViewer(), gridBagConstraints1);
			contents.add(getUpdateRoll(), gridBagConstraints2);
			contents.add(getUpdatePitch(), gridBagConstraints3);
			contents.add(getUpdateYaw(), gridBagConstraints4);
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
			refresh.setup(subscriberService, messagingService, MessageType.M101);
		}
		return refresh;
	}

	/**
	 * This method initializes airplane3dViewer	
	 * 	
	 * @return br.skylight.commons.j3d.Airplane3dViewer	
	 */
	private Airplane3dViewer getAirplane3dViewer() {
		if (airplane3dViewer == null) {
			try {
				airplane3dViewer = new Airplane3dViewer();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return airplane3dViewer;
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle 3D View";
	}

	/**
	 * This method initializes updateRoll	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getUpdateRoll() {
		if (updateRoll == null) {
			updateRoll = new JToggleButton();
			updateRoll.setMargin(ViewHelper.getDefaultButtonMargin());
			updateRoll.setSelected(true);
			updateRoll.setText("Roll");
		}
		return updateRoll;
	}

	/**
	 * This method initializes updatePitch	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getUpdatePitch() {
		if (updatePitch == null) {
			updatePitch = new JToggleButton();
			updatePitch.setMargin(ViewHelper.getDefaultButtonMargin());
			updatePitch.setSelected(true);
			updatePitch.setText("Pitch");
		}
		return updatePitch;
	}

	/**
	 * This method initializes updateYaw	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getUpdateYaw() {
		if (updateYaw == null) {
			updateYaw = new JToggleButton();
			updateYaw.setMargin(ViewHelper.getDefaultButtonMargin());
			updateYaw.setSelected(true);
			updateYaw.setText("Yaw");
		}
		return updateYaw;
	}

	/**
	 * This method initializes jLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new JLabel();
			jLabel.setText("JLabel");
		}
		return jLabel;
	}

}
