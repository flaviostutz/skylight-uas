package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.PayloadView;

public class PayloadInfoView extends PayloadView {

	private static final long serialVersionUID = 1L;
	private JPanel contents = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JLabel vehicleId = null;
	private JLabel vsmId = null;
	private JLabel payloadType = null;
	private JLabel recordingDevices = null;
	private JScrollPane jScrollPane1 = null;
	private JList controlledBy = null;
	private JLabel jLabel11 = null;
	private JLabel stationNumber = null;
	private JLabel jLabel111 = null;
	private JLabel door = null;
	
	/**
	 * @param owner
	 */
	public PayloadInfoView(ViewExtensionPoint vep) {
		super(vep);
	}

	@Override
	protected void updateGUI() {
		if(getCurrentPayload()!=null) {
			Payload payload = getCurrentPayload();
			vehicleId.setText(StringHelper.formatId(payload.getVehicleID().getVehicleID()));
			vsmId.setText(StringHelper.formatId(payload.getVehicleID().getVsmID()));
			payloadType.setText(payload.getPayloadType().getName());
			stationNumber.setText(payload.getUniqueStationNumber()+"");
			recordingDevices.setText(payload.getNumberOfPayloadRecordingDevices()+"");
			door.setText(payload.getStationDoor().name());
			
			DefaultListModel m2 = new DefaultListModel();
			getControlledBy().setModel(m2);
			for (Entry<Integer,CUCSControl> ce : payload.getCucsControls().entrySet()) {
				String cucsName = StringHelper.formatId(ce.getKey());
				CUCS c = vehicleControlService.getKnownCUCS().get(ce.getKey());
				if(c!=null) {
					cucsName = c.getLabel();
				}
				m2.addElement("> " + cucsName + " " + Arrays.deepToString(ce.getValue().getGrantedLOIs().getLOIs().toArray()));
			}
		}
	}
	
	/**
	 * This method initializes contents
	 * 
	 * @return javax.swing.JPanel
	 */
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
			gridBagConstraints81.gridx = 3;
			gridBagConstraints81.anchor = GridBagConstraints.WEST;
			gridBagConstraints81.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints81.gridy = 3;
			door = new JLabel();
			door.setText("-");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 2;
			gridBagConstraints7.insets = new Insets(0, 5, 4, 0);
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.gridy = 3;
			jLabel111 = new JLabel();
			jLabel111.setText("Door:");
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 3;
			gridBagConstraints61.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints61.anchor = GridBagConstraints.WEST;
			gridBagConstraints61.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints61.gridy = 2;
			stationNumber = new JLabel();
			stationNumber.setText("-");
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 2;
			gridBagConstraints51.insets = new Insets(0, 5, 4, 0);
			gridBagConstraints51.anchor = GridBagConstraints.EAST;
			gridBagConstraints51.gridy = 2;
			jLabel11 = new JLabel();
			jLabel11.setText("Station #:");
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.gridy = 7;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.gridwidth = 4;
			gridBagConstraints31.insets = new Insets(0, 15, 5, 15);
			gridBagConstraints31.gridx = 0;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints13.gridy = 3;
			recordingDevices = new JLabel();
			recordingDevices.setText("-");
			recordingDevices.setToolTipText("Number of recording devices");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints12.gridy = 2;
			payloadType = new JLabel();
			payloadType.setText("-");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 3;
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.insets = new Insets(10, 3, 3, 10);
			gridBagConstraints9.gridy = 0;
			vsmId = new JLabel();
			vsmId.setText("-");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.insets = new Insets(10, 3, 3, 0);
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.gridy = 0;
			vehicleId = new JLabel();
			vehicleId.setText("-");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.insets = new Insets(0, 15, 4, 0);
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 6;
			jLabel6 = new JLabel();
			jLabel6.setText("Controlled by:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 10, 4, 0);
			gridBagConstraints5.gridy = 3;
			jLabel5 = new JLabel();
			jLabel5.setText("Nr. rec. dev.:");
			jLabel5.setToolTipText("Number of recording devices");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(0, 10, 4, 0);
			gridBagConstraints4.gridy = 2;
			jLabel4 = new JLabel();
			jLabel4.setText("Payload type:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 2;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(10, 5, 4, 0);
			gridBagConstraints1.gridy = 0;
			jLabel1 = new JLabel();
			jLabel1.setText("VSM ID:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(10, 10, 4, 0);
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Vehicle ID:");
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.add(jLabel, gridBagConstraints);
			contents.add(jLabel1, gridBagConstraints1);
			contents.add(jLabel4, gridBagConstraints4);
			contents.add(jLabel5, gridBagConstraints5);
			contents.add(jLabel6, gridBagConstraints6);
			contents.add(vehicleId, gridBagConstraints8);
			contents.add(vsmId, gridBagConstraints9);
			contents.add(payloadType, gridBagConstraints12);
			contents.add(recordingDevices, gridBagConstraints13);
			contents.add(getJScrollPane1(), gridBagConstraints31);
			contents.add(jLabel11, gridBagConstraints51);
			contents.add(stationNumber, gridBagConstraints61);
			contents.add(jLabel111, gridBagConstraints7);
			contents.add(door, gridBagConstraints81);
		}
		return contents;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setPreferredSize(new Dimension(259, 60));
			jScrollPane1.setViewportView(getControlledBy());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes controlledBy	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getControlledBy() {
		if (controlledBy == null) {
			controlledBy = new JList();
			controlledBy.setBackground(SystemColor.control);
		}
		return controlledBy;
	}

	@Override
	protected String getBaseTitle() {
		return "Payload Info";
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
	}

}
