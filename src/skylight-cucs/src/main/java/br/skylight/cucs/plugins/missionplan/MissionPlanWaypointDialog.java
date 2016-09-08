package br.skylight.cucs.plugins.missionplan;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import br.skylight.commons.MeasureType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.LocationType;
import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.SensorOutput;
import br.skylight.commons.dli.enums.SensorPointingMode;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.widgets.JMeasureSpinner;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.LocationInput;

public class MissionPlanWaypointDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JTabbedPane tabbedPane = null;
	private JPanel positionPanel = null;
	private JPanel advancedPanel = null;
	private int waypointNumber;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel111 = null;
	private JLabel jLabel112 = null;
	private JLabel jLabel113 = null;
	private JLabel jLabel114 = null;
	private JLabel jLabel115 = null;
	private JLabel jLabel116 = null;
	private JLabel jLabel117 = null;
	private JLabel jLabel118 = null;
	private JLabel jLabel119 = null;
	private JLabel jLabel1110 = null;
	private JLabel number = null;
	private JComboBox locationType = null;
	private JComboBox altitudeType = null;
	private JComboBox speedType = null;
	private JComboBox turnType = null;
	private JMeasureSpinner<Integer> contingencyA = null;
	private JMeasureSpinner<Integer> contingencyB = null;
	private JMeasureSpinner<Integer> nextWaypoint = null;
	private JButton ok = null;
	private LocationInput latitude = null;
	private LocationInput longitude = null;
	private JButton add = null;
	private JButton remove = null;
	private JPopupMenu addMenu = null;  //  @jve:decl-index=0:visual-constraint="429,0"
	private JMenuItem addLoiter = null;
	private JMenuItem addPayloadAction = null;
	private JPanel panel = null;
	private JPanel loiterPanel = null;
	private JButton applyLoiter = null;
	private JLabel jLabel2 = null;
	private JMeasureSpinner<Integer> loiterTime = null;
	private JLabel jLabel3 = null;
	private JComboBox loiterType = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel41 = null;
	private JLabel jLabel42 = null;
	private JLabel jLabel43 = null;
	private JMeasureSpinner<Float> radius = null;
	private JMeasureSpinner<Float> length = null;
	private JMeasureSpinner<Double> bearing = null;
	private JComboBox direction = null;
	private AVLoiterWaypoint currentLoiterWaypoint;
	private Message selectedItem;
	private JMeasureSpinner<Float> speed = null;
	private JMeasureSpinner<Double> arrivalTime = null;
	private JMeasureSpinner<Float> altitude = null;
	private JComboBox waypointExtensions = null;
	private JPanel payloadActionPanel = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel51 = null;
	private JLabel jLabel511 = null;
	private JLabel jLabel512 = null;
	private JLabel jLabel513 = null;
	private JLabel jLabel514 = null;
	private JLabel jLabel5141 = null;
	private JLabel jLabel5142 = null;
	private JLabel jLabel5143 = null;
	private JLabel jLabel51421 = null;
	private JLabel jLabel514211 = null;
	private JLabel jLabel5142111 = null;
	private JComboBox station = null;
	private JComboBox sensor1Mode = null;
	private JComboBox sensor2Mode = null;
	private JComboBox sensorOutput = null;
	private JComboBox sensorPointingMode = null;
	private JMeasureSpinner<Double> starepointLatitude = null;
	private JMeasureSpinner<Double> starepointLongitude = null;
	private JMeasureSpinner<Float> starepointAltitude = null;
	private JComboBox starepointAltitudeType = null;
	private JMeasureSpinner<Float> payloadAzimuth = null;
	private JMeasureSpinner<Float> payloadElevation = null;
	private JMeasureSpinner<Float> payloadSensorRotation = null;
	private JButton applyPayload = null;
	private JScrollPane payloadScroll = null;

	private Vehicle vehicle;  //  @jve:decl-index=0:
	
	/**
	 * @param owner
	 */
	public MissionPlanWaypointDialog(Frame owner) {
		super(owner);
		initialize();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setValues();
			};
		});
	}

	protected void setValues() {
		//save ui data to waypoint def
		WaypointDef wd = vehicle.getMission().computeWaypointsMap().get(waypointNumber);
		AVPositionWaypoint pw = wd.getPositionWaypoint();
		pw.setLocationType((LocationType)getLocationType().getSelectedItem());
		pw.setWaypointToLatitudeOrRelativeY(getLatitude().getValue());
		pw.setWaypointToLongitudeOrRelativeX(getLongitude().getValue());
		pw.setWaypointAltitudeType((AltitudeType)getAltitudeType().getSelectedItem());
		pw.setWaypointToAltitude(getAltitude().getValue());
		pw.setWaypointSpeedType((WaypointSpeedType)getSpeedType().getSelectedItem());
		pw.setWaypointToSpeed(getSpeed().getValue());
		pw.setArrivalTime(getArrivalTime().getValue());
		pw.setTurnType((TurnType)getTurnType().getSelectedItem());
		pw.setContingencyWaypointA(getContingencyA().getValue());
		pw.setContingencyWaypointB(getContingencyB().getValue());
		pw.setNextWaypoint(getNextWaypoint().getValue());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(373, 418);
		this.setTitle("Waypoint properties");
		this.setContentPane(getJContentPane());
		this.setModal(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.fill = GridBagConstraints.BOTH;
			gridBagConstraints17.gridy = 0;
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.weighty = 1.0;
			gridBagConstraints17.gridwidth = 3;
			gridBagConstraints17.gridx = 0;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.anchor = GridBagConstraints.CENTER;
			gridBagConstraints15.insets = new Insets(3, 0, 4, 0);
			gridBagConstraints15.gridy = 3;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getOk(), gridBagConstraints15);
			jContentPane.add(getTabbedPane(), gridBagConstraints17);
		}
		return jContentPane;
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Position waypoint", null, getPositionPanel(), null);
			tabbedPane.addTab("Waypoint extensions", null, getAdvancedPanel(), null);
		}
		return tabbedPane;
	}

	/**
	 * This method initializes positionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPositionPanel() {
		if (positionPanel == null) {
			GridBagConstraints gridBagConstraints310 = new GridBagConstraints();
			gridBagConstraints310.gridx = 1;
			gridBagConstraints310.anchor = GridBagConstraints.WEST;
			gridBagConstraints310.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints310.gridy = 5;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints22.gridy = 8;
			GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
			gridBagConstraints110.gridx = 1;
			gridBagConstraints110.anchor = GridBagConstraints.WEST;
			gridBagConstraints110.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints110.gridy = 7;
			GridBagConstraints gridBagConstraints201 = new GridBagConstraints();
			gridBagConstraints201.gridx = 1;
			gridBagConstraints201.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints201.anchor = GridBagConstraints.WEST;
			gridBagConstraints201.insets = new Insets(0, 3, 3, 5);
			gridBagConstraints201.gridy = 3;
			GridBagConstraints gridBagConstraints191 = new GridBagConstraints();
			gridBagConstraints191.gridx = 1;
			gridBagConstraints191.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints191.anchor = GridBagConstraints.WEST;
			gridBagConstraints191.insets = new Insets(0, 3, 3, 5);
			gridBagConstraints191.gridy = 2;
			GridBagConstraints gridBagConstraints141 = new GridBagConstraints();
			gridBagConstraints141.gridx = 1;
			gridBagConstraints141.insets = new Insets(0, 3, 5, 0);
			gridBagConstraints141.anchor = GridBagConstraints.WEST;
			gridBagConstraints141.gridy = 13;
			GridBagConstraints gridBagConstraints131 = new GridBagConstraints();
			gridBagConstraints131.gridx = 1;
			gridBagConstraints131.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints131.anchor = GridBagConstraints.WEST;
			gridBagConstraints131.gridy = 11;
			GridBagConstraints gridBagConstraints121 = new GridBagConstraints();
			gridBagConstraints121.gridx = 1;
			gridBagConstraints121.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints121.anchor = GridBagConstraints.WEST;
			gridBagConstraints121.gridy = 10;
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints111.gridy = 9;
			gridBagConstraints111.weightx = 1.0;
			gridBagConstraints111.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints111.anchor = GridBagConstraints.WEST;
			gridBagConstraints111.gridx = 1;
			GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
			gridBagConstraints101.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints101.gridy = 10;
			gridBagConstraints101.weightx = 1.0;
			gridBagConstraints101.gridx = 1;
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints61.gridy = 6;
			gridBagConstraints61.weightx = 1.0;
			gridBagConstraints61.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints61.anchor = GridBagConstraints.WEST;
			gridBagConstraints61.gridx = 1;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints41.gridy = 4;
			gridBagConstraints41.weightx = 1.0;
			gridBagConstraints41.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.gridx = 1;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints14.gridy = 1;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridx = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.insets = new Insets(5, 3, 3, 0);
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 0;
			number = new JLabel();
			number.setText("#");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.EAST;
			gridBagConstraints12.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints12.gridy = 13;
			jLabel1110 = new JLabel();
			jLabel1110.setText("Next waypoint:");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.EAST;
			gridBagConstraints11.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints11.gridy = 9;
			jLabel119 = new JLabel();
			jLabel119.setText("Turn type:");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints10.gridy = 11;
			jLabel118 = new JLabel();
			jLabel118.setText("Contingency waypoint B:");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.anchor = GridBagConstraints.EAST;
			gridBagConstraints9.insets = new Insets(0, 15, 3, 0);
			gridBagConstraints9.gridy = 10;
			jLabel117 = new JLabel();
			jLabel117.setText("Contingency waypoint A:");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints8.gridy = 8;
			jLabel116 = new JLabel();
			jLabel116.setText("Arrival time:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints7.gridy = 7;
			jLabel115 = new JLabel();
			jLabel115.setText("Speed:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints6.gridy = 6;
			jLabel114 = new JLabel();
			jLabel114.setText("Speed type:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints5.gridy = 5;
			jLabel113 = new JLabel();
			jLabel113.setText("Altitude:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints4.gridy = 4;
			jLabel112 = new JLabel();
			jLabel112.setText("Altitude type:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints3.gridy = 3;
			jLabel111 = new JLabel();
			jLabel111.setText("Position longitude:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints2.gridy = 2;
			jLabel11 = new JLabel();
			jLabel11.setText("Position latitude:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints1.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("Location type:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(5, 0, 3, 0);
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Waypoint number:");
			positionPanel = new JPanel();
			positionPanel.setLayout(new GridBagLayout());
			positionPanel.add(jLabel, gridBagConstraints);
			positionPanel.add(jLabel1, gridBagConstraints1);
			positionPanel.add(jLabel11, gridBagConstraints2);
			positionPanel.add(jLabel111, gridBagConstraints3);
			positionPanel.add(jLabel112, gridBagConstraints4);
			positionPanel.add(jLabel113, gridBagConstraints5);
			positionPanel.add(jLabel114, gridBagConstraints6);
			positionPanel.add(jLabel115, gridBagConstraints7);
			positionPanel.add(jLabel116, gridBagConstraints8);
			positionPanel.add(jLabel117, gridBagConstraints9);
			positionPanel.add(jLabel118, gridBagConstraints10);
			positionPanel.add(jLabel119, gridBagConstraints11);
			positionPanel.add(jLabel1110, gridBagConstraints12);
			positionPanel.add(number, gridBagConstraints13);
			positionPanel.add(getLocationType(), gridBagConstraints14);
			positionPanel.add(getAltitudeType(), gridBagConstraints41);
			positionPanel.add(getSpeedType(), gridBagConstraints61);
			positionPanel.add(getTurnType(), gridBagConstraints111);
			positionPanel.add(getContingencyA(), gridBagConstraints121);
			positionPanel.add(getContingencyB(), gridBagConstraints131);
			positionPanel.add(getNextWaypoint(), gridBagConstraints141);
			positionPanel.add(getLatitude(), gridBagConstraints191);
			positionPanel.add(getLongitude(), gridBagConstraints201);
			positionPanel.add(getSpeed(), gridBagConstraints110);
			positionPanel.add(getArrivalTime(), gridBagConstraints22);
			positionPanel.add(getAltitude(), gridBagConstraints310);
		}
		return positionPanel;
	}

	/**
	 * This method initializes advancedPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAdvancedPanel() {
		if (advancedPanel == null) {
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.fill = GridBagConstraints.BOTH;
			gridBagConstraints18.gridy = 1;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.gridwidth = 2;
			gridBagConstraints18.insets = new Insets(2, 3, 0, 3);
			gridBagConstraints18.gridx = 0;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.fill = GridBagConstraints.BOTH;
			gridBagConstraints25.gridwidth = 2;
			gridBagConstraints25.weighty = 1.0;
			gridBagConstraints25.weightx = 1.0;
			gridBagConstraints25.insets = new Insets(5, 2, 2, 2);
			gridBagConstraints25.anchor = GridBagConstraints.NORTH;
			gridBagConstraints25.gridy = 2;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.insets = new Insets(5, 3, 0, 0);
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints16.gridy = 0;
			advancedPanel = new JPanel();
			advancedPanel.setLayout(new GridBagLayout());
			advancedPanel.add(getAdd(), gridBagConstraints16);
			advancedPanel.add(getRemove(), gridBagConstraints21);
			advancedPanel.add(getPanel(), gridBagConstraints25);
			advancedPanel.add(getWaypointExtensions(), gridBagConstraints18);
		}
		return advancedPanel;
	}
	
	public void showDialog(Vehicle vehicle, int waypointNumber) {
		this.vehicle = vehicle;
		this.waypointNumber = waypointNumber;
		setTitle("Waypoint #"+ waypointNumber +" properties");
		WaypointDef wd = vehicle.getMission().computeWaypointsMap().get(waypointNumber);
		AVPositionWaypoint pw = wd.getPositionWaypoint();
		number.setText(wd.getWaypointNumber()+"");
		getLocationType().setSelectedItem(pw.getLocationType());
		getLatitude().setValue(pw.getWaypointToLatitudeOrRelativeY());
		getLongitude().setValue(pw.getWaypointToLongitudeOrRelativeX());
		getAltitudeType().setSelectedItem(pw.getWaypointAltitudeType());
		getAltitude().setValue(pw.getWaypointToAltitude());
		getSpeedType().setSelectedItem(pw.getWaypointSpeedType());
		updateUI();//use this to update speed spinner unit type
		getSpeed().setValue(pw.getWaypointToSpeed());
		getArrivalTime().setValue(pw.getArrivalTime());
		getTurnType().setSelectedItem(pw.getTurnType());
		getContingencyA().setValue(pw.getContingencyWaypointA());
		getContingencyB().setValue(pw.getContingencyWaypointB());
		getNextWaypoint().setValue(pw.getNextWaypoint());

		//load payload stations combo box
		Vector<Payload> list = new Vector<Payload>();
		for (Payload p : vehicle.getPayloads().values()) {
			list.add(p);
		}
		station.setModel(new DefaultComboBoxModel(list));
		station.updateUI();
		
		updateComboBoxModel();
		
		//show first item in extensions combo box
		showFirstExtension();
		
		updateUI();
		ViewHelper.centerWindow(this);
		setVisible(true);
	}

	private void updateComboBoxModel() {
		Vector<Message> list = new Vector<Message>();
		WaypointDef wd = vehicle.getMission().computeWaypointsMap().get(waypointNumber);
		for (Message message : wd.getPayloadActionWaypoints()) {
			list.add(message);
		}
		if(wd.getLoiterWaypoint()!=null) {
			list.add(wd.getLoiterWaypoint());
		}
		getWaypointExtensions().setModel(new DefaultComboBoxModel(list));
		getWaypointExtensions().updateUI();
		if(list.size()>0) {
			if(selectedItem!=null) {
				getWaypointExtensions().setSelectedItem(selectedItem);
			} else {
				getWaypointExtensions().setSelectedIndex(0);
			}
		}
	}

	private void updateUI() {
		WaypointSpeedType st = (WaypointSpeedType)getSpeedType().getSelectedItem();
		//show units for POSITION PANEL
		if(st.equals(WaypointSpeedType.ARRIVAL_TIME)) {
			getSpeed().setEnabled(false);
			getArrivalTime().setEnabled(true);
		} else {
			getSpeed().setEnabled(true);
			getArrivalTime().setEnabled(false);
			if(st.equals(WaypointSpeedType.GROUND_SPEED)) {
				getSpeed().setMeasureType(MeasureType.GROUND_SPEED);
			} else {
				getSpeed().setMeasureType(MeasureType.AIR_SPEED);
			}
			getRadius().setMeasureType(MeasureType.DISTANCE);
		}
		
		//show units for LOITER PANEL
		getAltitude().setMeasureType(MeasureType.ALTITUDE);
		getLength().setMeasureType(MeasureType.DISTANCE);
		getBearing().setMeasureType(MeasureType.HEADING);
	}

	/**
	 * This method initializes locationType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getLocationType() {
		if (locationType == null) {
			locationType = new JComboBox();
			locationType.setModel(new EnumComboBoxModel(LocationType.class));
			locationType.setPreferredSize(new Dimension(87, 20));
		}
		return locationType;
	}

	/**
	 * This method initializes altitudeType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getAltitudeType() {
		if (altitudeType == null) {
			altitudeType = new JComboBox();
			altitudeType.setModel(new EnumComboBoxModel(AltitudeType.class));
			altitudeType.setPreferredSize(new Dimension(87, 20));
		}
		return altitudeType;
	}

	/**
	 * This method initializes speedType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSpeedType() {
		if (speedType == null) {
			speedType = new JComboBox();
			speedType.setModel(new EnumComboBoxModel(WaypointSpeedType.class));
			speedType.setPreferredSize(new Dimension(87, 20));
			speedType.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					updateUI();
				}
			});
		}
		return speedType;
	}

	/**
	 * This method initializes turnType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getTurnType() {
		if (turnType == null) {
			turnType = new JComboBox();
			turnType.setModel(new EnumComboBoxModel(TurnType.class));
			turnType.setPreferredSize(new Dimension(87, 20));
		}
		return turnType;
	}

	/**
	 * This method initializes contingencyA	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getContingencyA() {
		if (contingencyA == null) {
			contingencyA = new JMeasureSpinner<Integer>();
			contingencyA.setup(null, 0, 0, 9999999, 1, 0, 0);
			contingencyA.setPreferredSize(new Dimension(120, 20));
		}
		return contingencyA;
	}

	/**
	 * This method initializes contingencyB	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getContingencyB() {
		if (contingencyB == null) {
			contingencyB = new JMeasureSpinner<Integer>();
			contingencyB.setup(null, 0, 0, 9999999, 1, 0, 0);
			contingencyB.setPreferredSize(new Dimension(120, 20));
		}
		return contingencyB;
	}

	/**
	 * This method initializes nextWaypoint	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getNextWaypoint() {
		if (nextWaypoint == null) {
			nextWaypoint = new JMeasureSpinner<Integer>();
			nextWaypoint.setup(null, 0, 0, 9999999, 1, 0, 0);
			nextWaypoint.setPreferredSize(new Dimension(120, 20));
		}
		return nextWaypoint;
	}

	/**
	 * This method initializes ok	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOk() {
		if (ok == null) {
			ok = new JButton();
			ok.setText("Close");
			ok.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setValues();
					setVisible(false);
				}
			});
		}
		return ok;
	}

	/**
	 * This method initializes latitude	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private LocationInput getLatitude() {
		if (latitude == null) {
			latitude = new LocationInput();
			latitude.setup(br.skylight.cucs.widgets.LocationInput.LocationType.LATITUDE);
			latitude.setPreferredSize(new Dimension(120, 20));
		}
		return latitude;
	}

	/**
	 * This method initializes longitude	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private LocationInput getLongitude() {
		if (longitude == null) {
			longitude = new LocationInput();
			longitude.setup(br.skylight.cucs.widgets.LocationInput.LocationType.LONGITUDE);
			longitude.setPreferredSize(new Dimension(120, 20));
		}
		return longitude;
	}

	/**
	 * This method initializes add	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdd() {
		if (add == null) {
			add = new JButton();
			add.setEnabled(true);
			add.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/add.gif")));
			add.setMargin(ViewHelper.getDefaultButtonMargin());
			add.setToolTipText("Add an waypoint extension");
			add.addMouseListener(new JPopupMenuMouseListener(getAddMenu(), true));
		}
		return add;
	}

	/**
	 * This method initializes remove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemove() {
		if (remove == null) {
			remove = new JButton();
			remove.setEnabled(true);
			remove.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/remove.gif")));
			remove.setMargin(ViewHelper.getDefaultButtonMargin());
			remove.setToolTipText("Remove selected waypoint extension");
			remove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(selectedItem!=null) {
						if(selectedItem instanceof AVLoiterWaypoint) {
							vehicle.getMission().getLoiterWaypoints().remove(selectedItem);
						} else if(selectedItem instanceof PayloadActionWaypoint) {
							vehicle.getMission().getPayloadActionWaypoints().remove(selectedItem);
						}
						updateComboBoxModel();
						selectedItem = null;
						showFirstExtension();
					}
				}
			});
		}
		return remove;
	}

	protected void showFirstExtension() {
		if(getWaypointExtensions().getModel().getSize()>0) {
			getWaypointExtensions().setSelectedIndex(0);
			selectedItem = (Message)getWaypointExtensions().getSelectedItem();
		} else {
			getWaypointExtensions().setSelectedIndex(-1);
			selectedItem=null;
		}
		if(selectedItem==null) {
			((DefaultComboBoxModel)getWaypointExtensions().getModel()).addElement("-no extensions found-");
		}
		showPanelForSelectedItem();
	}

	/**
	 * This method initializes addMenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getAddMenu() {
		if (addMenu == null) {
			addMenu = new JPopupMenu();
			addMenu.add(getAddLoiter());
			addMenu.add(getAddPayloadAction());
		}
		return addMenu;
	}

	/**
	 * This method initializes addRoute	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAddLoiter() {
		if (addLoiter == null) {
			addLoiter = new JMenuItem();
			addLoiter.setText("Add Loiter Definition");
			addLoiter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AVLoiterWaypoint lw = new AVLoiterWaypoint();
					lw.setWaypointNumber(waypointNumber);
					lw.setVehicleID(vehicle.getVehicleID().getVehicleID());
					vehicle.getMission().getLoiterWaypoints().add(lw);
					selectedItem = lw;
					updateComboBoxModel();
					showPanelForSelectedItem();
				}
			});
		}
		return addLoiter;
	}

	/**
	 * This method initializes addWaypoint	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAddPayloadAction() {
		if (addPayloadAction == null) {
			addPayloadAction = new JMenuItem();
			addPayloadAction.setText("Add Payload Action");
			addPayloadAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!vehicle.getPayloads().isEmpty()) {
						PayloadActionWaypoint pa = new PayloadActionWaypoint();
						pa.setWaypointNumber(waypointNumber);
						pa.setVehicleID(vehicle.getVehicleID().getVehicleID());
						vehicle.getMission().getPayloadActionWaypoints().add(pa);
						selectedItem = pa;
						updateComboBoxModel();
						showPanelForSelectedItem();
					} else {
						JOptionPane.showMessageDialog(getThis(), "Cannot add Payload Action Waypoint because current vehicle has no payloads");
					}
				}
			});
		}
		return addPayloadAction;
	}

	private MissionPlanWaypointDialog getThis() {
		return this;
	}
	
	private void showPanelForSelectedItem() {
		if(selectedItem instanceof AVLoiterWaypoint) {
			AVLoiterWaypoint m = (AVLoiterWaypoint)selectedItem;
			getLoiterTime().setValue(m.getWaypointLoiterTime());
			getLoiterType().setSelectedItem(m.getWaypointLoiterType());
			getRadius().setValue(m.getLoiterRadius());
			getLength().setValue(m.getLoiterLength());
			getBearing().setValue(m.getLoiterBearing());
			getDirection().setSelectedItem(m.getLoiterDirection());
			((CardLayout)getPanel().getLayout()).show(getPanel(), getLoiterPanel().getName());
			
		} else if(selectedItem instanceof PayloadActionWaypoint) {
			PayloadActionWaypoint m = (PayloadActionWaypoint)selectedItem;
			
			Payload p = null;
			for (Entry<Integer,Payload> pl : vehicle.getPayloads().entrySet()) {
				if(m.getStationNumber().isStation(pl.getKey())) {
					p = pl.getValue();
				}
			}
			getStation().setSelectedItem(p);
			
			getPayloadAzimuth().setValue(m.getPayloadAz());
			getPayloadElevation().setValue(m.getPayloadEl());
			getPayloadSensorRotation().setValue(m.getPayloadSensorRotationAngle());
			getSensorOutput().setSelectedItem(m.getSensorOutput());
			getSensor1Mode().setSelectedItem(m.getSetSensor1Mode());
			getSensor2Mode().setSelectedItem(m.getSetSensor2Mode());
			getSensorPointingMode().setSelectedItem(m.getSetSensorPointingMode());
			getStarepointAltitude().setValue(m.getStarepointAltitude());
			getStarepointAltitudeType().setSelectedItem(m.getStarepointAltitudeType());
			getStarepointLatitude().setValue(m.getStarepointLatitude());
			getStarepointLongitude().setValue(m.getStarepointLongitude());
			
			((CardLayout)getPanel().getLayout()).show(getPanel(), getPayloadScroll().getName());
		} else {
			((CardLayout)getPanel().getLayout()).show(getPanel(), "EMPTY");
		}
	}

	/**
	 * This method initializes panel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new CardLayout());
			panel.add(getLoiterPanel(), getLoiterPanel().getName());
			panel.add(getPayloadScroll(), getPayloadScroll().getName());
			panel.add(new JPanel(), "EMPTY");
			((CardLayout)panel.getLayout()).show(panel, "EMPTY");
		}
		return panel;
	}

	/**
	 * This method initializes loiterPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLoiterPanel() {
		if (loiterPanel == null) {
			GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
			gridBagConstraints39.fill = GridBagConstraints.NONE;
			gridBagConstraints39.gridy = 5;
			gridBagConstraints39.weightx = 1.0;
			gridBagConstraints39.anchor = GridBagConstraints.WEST;
			gridBagConstraints39.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints39.gridx = 1;
			GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
			gridBagConstraints38.gridx = 1;
			gridBagConstraints38.anchor = GridBagConstraints.WEST;
			gridBagConstraints38.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints38.weightx = 1.0;
			gridBagConstraints38.fill = GridBagConstraints.NONE;
			gridBagConstraints38.gridy = 4;
			GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
			gridBagConstraints37.gridx = 1;
			gridBagConstraints37.anchor = GridBagConstraints.WEST;
			gridBagConstraints37.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints37.weightx = 1.0;
			gridBagConstraints37.fill = GridBagConstraints.NONE;
			gridBagConstraints37.gridy = 3;
			GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
			gridBagConstraints36.gridx = 1;
			gridBagConstraints36.anchor = GridBagConstraints.WEST;
			gridBagConstraints36.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints36.weightx = 1.0;
			gridBagConstraints36.fill = GridBagConstraints.NONE;
			gridBagConstraints36.gridy = 2;
			GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
			gridBagConstraints35.gridx = 0;
			gridBagConstraints35.anchor = GridBagConstraints.EAST;
			gridBagConstraints35.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints35.weightx = 0.0;
			gridBagConstraints35.gridy = 5;
			jLabel43 = new JLabel();
			jLabel43.setText("Direction:");
			GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
			gridBagConstraints34.gridx = 0;
			gridBagConstraints34.anchor = GridBagConstraints.EAST;
			gridBagConstraints34.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints34.weightx = 0.0;
			gridBagConstraints34.gridy = 4;
			jLabel42 = new JLabel();
			jLabel42.setText("Bearing:");
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 0;
			gridBagConstraints33.anchor = GridBagConstraints.EAST;
			gridBagConstraints33.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints33.weightx = 0.0;
			gridBagConstraints33.gridy = 3;
			jLabel41 = new JLabel();
			jLabel41.setText("Length:");
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 0;
			gridBagConstraints32.anchor = GridBagConstraints.EAST;
			gridBagConstraints32.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints32.weightx = 0.0;
			gridBagConstraints32.gridy = 2;
			jLabel4 = new JLabel();
			jLabel4.setText("Radius:");
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.NONE;
			gridBagConstraints31.gridy = 1;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.anchor = GridBagConstraints.WEST;
			gridBagConstraints31.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints31.gridx = 1;
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = 0;
			gridBagConstraints30.anchor = GridBagConstraints.EAST;
			gridBagConstraints30.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints30.weightx = 0.0;
			gridBagConstraints30.gridy = 1;
			jLabel3 = new JLabel();
			jLabel3.setText("Loiter type:");
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 1;
			gridBagConstraints29.anchor = GridBagConstraints.WEST;
			gridBagConstraints29.insets = new Insets(0, 3, 3, 10);
			gridBagConstraints29.fill = GridBagConstraints.NONE;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.gridy = 0;
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.gridx = 0;
			gridBagConstraints28.anchor = GridBagConstraints.EAST;
			gridBagConstraints28.insets = new Insets(0, 20, 3, 0);
			gridBagConstraints28.weightx = 0.0;
			gridBagConstraints28.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText("Loiter time:");
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 0;
			gridBagConstraints27.gridwidth = 2;
			gridBagConstraints27.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints27.weighty = 1.0;
			gridBagConstraints27.anchor = GridBagConstraints.NORTH;
			gridBagConstraints27.gridy = 6;
			loiterPanel = new JPanel();
			loiterPanel.setLayout(new GridBagLayout());
			loiterPanel.setBorder(BorderFactory.createTitledBorder(null, "Loiter Waypoint", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			loiterPanel.setName("loiterPanel");
			loiterPanel.add(getApplyLoiter(), gridBagConstraints27);
			loiterPanel.add(jLabel2, gridBagConstraints28);
			loiterPanel.add(getLoiterTime(), gridBagConstraints29);
			loiterPanel.add(jLabel3, gridBagConstraints30);
			loiterPanel.add(getLoiterType(), gridBagConstraints31);
			loiterPanel.add(jLabel4, gridBagConstraints32);
			loiterPanel.add(jLabel41, gridBagConstraints33);
			loiterPanel.add(jLabel42, gridBagConstraints34);
			loiterPanel.add(jLabel43, gridBagConstraints35);
			loiterPanel.add(getRadius(), gridBagConstraints36);
			loiterPanel.add(getLength(), gridBagConstraints37);
			loiterPanel.add(getBearing(), gridBagConstraints38);
			loiterPanel.add(getDirection(), gridBagConstraints39);
		}
		return loiterPanel;
	}

	/**
	 * This method initializes applyLoiter	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getApplyLoiter() {
		if (applyLoiter == null) {
			applyLoiter = new JButton();
			applyLoiter.setText("Apply");
			applyLoiter.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					AVLoiterWaypoint lw = (AVLoiterWaypoint)selectedItem;
					lw.setLoiterBearing(getBearing().getValue());
					lw.setLoiterDirection((LoiterDirection)getDirection().getSelectedItem());
					lw.setLoiterLength(getLength().getValue());
					lw.setLoiterRadius(getRadius().getValue());
					lw.setWaypointLoiterTime((Integer)getLoiterTime().getValue());
					lw.setWaypointLoiterType((LoiterType)getLoiterType().getSelectedItem());
					lw.setWaypointNumber(waypointNumber);
				}
			});
		}
		return applyLoiter;
	}

	/**
	 * This method initializes loiterTime	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getLoiterTime() {
		if (loiterTime == null) {
			loiterTime = new JMeasureSpinner<Integer>();
			loiterTime.setup(null, 0, 0, Integer.MAX_VALUE, 1, 0, 0);
			loiterTime.setUnitName("s");
			loiterTime.setPreferredSize(new Dimension(150, 20));
		}
		return loiterTime;
	}

	/**
	 * This method initializes loiterType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getLoiterType() {
		if (loiterType == null) {
			loiterType = new JComboBox();
			loiterType.setPreferredSize(new Dimension(150, 20));
			loiterType.setModel(new EnumComboBoxModel(LoiterType.class));
		}
		return loiterType;
	}

	/**
	 * This method initializes radius	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getRadius() {
		if (radius == null) {
			radius = new JMeasureSpinner<Float>();
			radius.setup(MeasureType.DISTANCE, 0F, 0, Integer.MAX_VALUE, 1, 0, 2);
			radius.setPreferredSize(new Dimension(150, 20));
		}
		return radius;
	}

	/**
	 * This method initializes length	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getLength() {
		if (length == null) {
			length = new JMeasureSpinner<Float>();
			length.setup(MeasureType.DISTANCE, 0F, 0, Integer.MAX_VALUE, 1, 0, 2);
			length.setPreferredSize(new Dimension(150, 20));
		}
		return length;
	}

	/**
	 * This method initializes bearing	
	 * 	
	 * @return javax.swing.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getBearing() {
		if (bearing == null) {
			bearing = new JMeasureSpinner<Double>();
			bearing.setup(MeasureType.HEADING, 0.0, 0, MathHelper.TWO_PI, Math.toRadians(5), 0, 2);
			bearing.setPreferredSize(new Dimension(150, 20));
		}
		return bearing;
	}

	/**
	 * This method initializes direction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDirection() {
		if (direction == null) {
			direction = new JComboBox();
			direction.setPreferredSize(new Dimension(150, 20));
			direction.setModel(new EnumComboBoxModel(LoiterDirection.class));
		}
		return direction;
	}

	/**
	 * This method initializes speed	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getSpeed() {
		if (speed == null) {
			speed = new JMeasureSpinner<Float>();
			speed.setup(MeasureType.GROUND_SPEED, 0F, 0, 99999, 1, 0, 3);
			speed.setPreferredSize(new Dimension(120, 20));
		}
		return speed;
	}

	/**
	 * This method initializes arrivalTime	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getArrivalTime() {
		if (arrivalTime == null) {
			arrivalTime = new JMeasureSpinner<Double>();
			arrivalTime.setup(null, 0.0, 0, 99999999, 1, 0, 0);
			arrivalTime.setPreferredSize(new Dimension(120, 20));
			arrivalTime.setUnitName("s");
		}
		return arrivalTime;
	}

	/**
	 * This method initializes altitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getAltitude() {
		if (altitude == null) {
			altitude = new JMeasureSpinner<Float>();
			altitude.setup(MeasureType.ALTITUDE, 0F, 0, 9999999, 1, 0, 5);
			altitude.setPreferredSize(new Dimension(120, 20));
		}
		return altitude;
	}

	/**
	 * This method initializes waypointExtensions	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getWaypointExtensions() {
		if (waypointExtensions == null) {
			waypointExtensions = new JComboBox();
			waypointExtensions.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					Object si = waypointExtensions.getSelectedItem();
					if(si instanceof Message) {
						selectedItem = (Message)waypointExtensions.getSelectedItem();
					} else {
						selectedItem = null;
					}
					showPanelForSelectedItem();
				}
			});
		}
		return waypointExtensions;
	}

	/**
	 * This method initializes payloadActionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPayloadActionPanel() {
		if (payloadActionPanel == null) {
			GridBagConstraints gridBagConstraints60 = new GridBagConstraints();
			gridBagConstraints60.gridx = 0;
			gridBagConstraints60.gridwidth = 2;
			gridBagConstraints60.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints60.weighty = 1.0;
			gridBagConstraints60.gridy = 12;
			GridBagConstraints gridBagConstraints59 = new GridBagConstraints();
			gridBagConstraints59.gridx = 1;
			gridBagConstraints59.anchor = GridBagConstraints.WEST;
			gridBagConstraints59.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints59.gridy = 11;
			GridBagConstraints gridBagConstraints58 = new GridBagConstraints();
			gridBagConstraints58.gridx = 1;
			gridBagConstraints58.anchor = GridBagConstraints.WEST;
			gridBagConstraints58.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints58.gridy = 10;
			GridBagConstraints gridBagConstraints57 = new GridBagConstraints();
			gridBagConstraints57.gridx = 1;
			gridBagConstraints57.anchor = GridBagConstraints.WEST;
			gridBagConstraints57.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints57.gridy = 9;
			GridBagConstraints gridBagConstraints56 = new GridBagConstraints();
			gridBagConstraints56.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints56.gridy = 7;
			gridBagConstraints56.weightx = 1.0;
			gridBagConstraints56.anchor = GridBagConstraints.WEST;
			gridBagConstraints56.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints56.gridx = 1;
			GridBagConstraints gridBagConstraints55 = new GridBagConstraints();
			gridBagConstraints55.gridx = 1;
			gridBagConstraints55.anchor = GridBagConstraints.WEST;
			gridBagConstraints55.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints55.gridy = 8;
			GridBagConstraints gridBagConstraints54 = new GridBagConstraints();
			gridBagConstraints54.gridx = 1;
			gridBagConstraints54.anchor = GridBagConstraints.WEST;
			gridBagConstraints54.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints54.gridy = 6;
			GridBagConstraints gridBagConstraints53 = new GridBagConstraints();
			gridBagConstraints53.gridx = 1;
			gridBagConstraints53.anchor = GridBagConstraints.WEST;
			gridBagConstraints53.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints53.gridy = 5;
			GridBagConstraints gridBagConstraints52 = new GridBagConstraints();
			gridBagConstraints52.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints52.gridy = 4;
			gridBagConstraints52.weightx = 1.0;
			gridBagConstraints52.anchor = GridBagConstraints.WEST;
			gridBagConstraints52.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints52.gridx = 1;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints51.gridy = 3;
			gridBagConstraints51.weightx = 1.0;
			gridBagConstraints51.anchor = GridBagConstraints.WEST;
			gridBagConstraints51.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints51.gridx = 1;
			GridBagConstraints gridBagConstraints50 = new GridBagConstraints();
			gridBagConstraints50.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints50.gridy = 2;
			gridBagConstraints50.weightx = 1.0;
			gridBagConstraints50.anchor = GridBagConstraints.WEST;
			gridBagConstraints50.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints50.gridx = 1;
			GridBagConstraints gridBagConstraints49 = new GridBagConstraints();
			gridBagConstraints49.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints49.gridy = 1;
			gridBagConstraints49.weightx = 1.0;
			gridBagConstraints49.anchor = GridBagConstraints.WEST;
			gridBagConstraints49.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints49.gridx = 1;
			GridBagConstraints gridBagConstraints48 = new GridBagConstraints();
			gridBagConstraints48.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints48.gridy = 0;
			gridBagConstraints48.weightx = 1.0;
			gridBagConstraints48.anchor = GridBagConstraints.WEST;
			gridBagConstraints48.insets = new Insets(1, 3, 3, 0);
			gridBagConstraints48.gridx = 1;
			GridBagConstraints gridBagConstraints47 = new GridBagConstraints();
			gridBagConstraints47.gridx = 0;
			gridBagConstraints47.anchor = GridBagConstraints.EAST;
			gridBagConstraints47.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints47.gridy = 11;
			jLabel5142111 = new JLabel();
			jLabel5142111.setText("Payload sensor rotation:");
			GridBagConstraints gridBagConstraints46 = new GridBagConstraints();
			gridBagConstraints46.gridx = 0;
			gridBagConstraints46.anchor = GridBagConstraints.EAST;
			gridBagConstraints46.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints46.gridy = 10;
			jLabel514211 = new JLabel();
			jLabel514211.setText("Payload elevation (AV):");
			GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
			gridBagConstraints45.gridx = 0;
			gridBagConstraints45.anchor = GridBagConstraints.EAST;
			gridBagConstraints45.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints45.gridy = 9;
			jLabel51421 = new JLabel();
			jLabel51421.setText("Payload azimuth (AV):");
			GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
			gridBagConstraints44.gridx = 0;
			gridBagConstraints44.anchor = GridBagConstraints.EAST;
			gridBagConstraints44.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints44.gridy = 7;
			jLabel5143 = new JLabel();
			jLabel5143.setText("Starepoint altitude type:");
			GridBagConstraints gridBagConstraints43 = new GridBagConstraints();
			gridBagConstraints43.gridx = 0;
			gridBagConstraints43.anchor = GridBagConstraints.EAST;
			gridBagConstraints43.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints43.gridy = 8;
			jLabel5142 = new JLabel();
			jLabel5142.setText("Starepoint altitude:");
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.gridx = 0;
			gridBagConstraints42.anchor = GridBagConstraints.EAST;
			gridBagConstraints42.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints42.gridy = 6;
			jLabel5141 = new JLabel();
			jLabel5141.setText("Starepoint longitude:");
			GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
			gridBagConstraints40.gridx = 0;
			gridBagConstraints40.anchor = GridBagConstraints.EAST;
			gridBagConstraints40.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints40.gridy = 5;
			jLabel514 = new JLabel();
			jLabel514.setText("Starepoint latitude:");
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 0;
			gridBagConstraints26.anchor = GridBagConstraints.EAST;
			gridBagConstraints26.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints26.gridy = 4;
			jLabel513 = new JLabel();
			jLabel513.setText("Sensor pointing mode:");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints24.gridy = 3;
			jLabel512 = new JLabel();
			jLabel512.setText("Sensor output:");
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 0;
			gridBagConstraints23.anchor = GridBagConstraints.EAST;
			gridBagConstraints23.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints23.gridy = 2;
			jLabel511 = new JLabel();
			jLabel511.setText("Sensor 2 mode:");
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.anchor = GridBagConstraints.EAST;
			gridBagConstraints20.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints20.gridy = 1;
			jLabel51 = new JLabel();
			jLabel51.setText("Sensor 1 mode:");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.anchor = GridBagConstraints.EAST;
			gridBagConstraints19.insets = new Insets(1, 10, 0, 0);
			gridBagConstraints19.gridy = 0;
			jLabel5 = new JLabel();
			jLabel5.setText("Station:");
			payloadActionPanel = new JPanel();
			payloadActionPanel.setLayout(new GridBagLayout());
			payloadActionPanel.setBorder(BorderFactory.createTitledBorder(null, "Payload Action", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			payloadActionPanel.setName("payloadActionPanel");
			payloadActionPanel.add(jLabel5, gridBagConstraints19);
			payloadActionPanel.add(jLabel51, gridBagConstraints20);
			payloadActionPanel.add(jLabel511, gridBagConstraints23);
			payloadActionPanel.add(jLabel512, gridBagConstraints24);
			payloadActionPanel.add(jLabel513, gridBagConstraints26);
			payloadActionPanel.add(jLabel514, gridBagConstraints40);
			payloadActionPanel.add(jLabel5141, gridBagConstraints42);
			payloadActionPanel.add(jLabel5142, gridBagConstraints43);
			payloadActionPanel.add(jLabel5143, gridBagConstraints44);
			payloadActionPanel.add(jLabel51421, gridBagConstraints45);
			payloadActionPanel.add(jLabel514211, gridBagConstraints46);
			payloadActionPanel.add(jLabel5142111, gridBagConstraints47);
			payloadActionPanel.add(getStation(), gridBagConstraints48);
			payloadActionPanel.add(getSensor1Mode(), gridBagConstraints49);
			payloadActionPanel.add(getSensor2Mode(), gridBagConstraints50);
			payloadActionPanel.add(getSensorOutput(), gridBagConstraints51);
			payloadActionPanel.add(getSensorPointingMode(), gridBagConstraints52);
			payloadActionPanel.add(getStarepointLatitude(), gridBagConstraints53);
			payloadActionPanel.add(getStarepointLongitude(), gridBagConstraints54);
			payloadActionPanel.add(getStarepointAltitude(), gridBagConstraints55);
			payloadActionPanel.add(getStarepointAltitudeType(), gridBagConstraints56);
			payloadActionPanel.add(getPayloadAzimuth(), gridBagConstraints57);
			payloadActionPanel.add(getPayloadElevation(), gridBagConstraints58);
			payloadActionPanel.add(getPayloadSensorRotation(), gridBagConstraints59);
			payloadActionPanel.add(getApplyPayload(), gridBagConstraints60);
		}
		return payloadActionPanel;
	}

	/**
	 * This method initializes station	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getStation() {
		if (station == null) {
			station = new JComboBox();
			station.setPreferredSize(new Dimension(120, 20));
		}
		return station;
	}

	/**
	 * This method initializes sensor1Mode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSensor1Mode() {
		if (sensor1Mode == null) {
			sensor1Mode = new JComboBox();
			sensor1Mode.setPreferredSize(new Dimension(120, 20));
			sensor1Mode.setModel(new EnumComboBoxModel(SensorMode.class));
		}
		return sensor1Mode;
	}

	/**
	 * This method initializes sensor2Mode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSensor2Mode() {
		if (sensor2Mode == null) {
			sensor2Mode = new JComboBox();
			sensor2Mode.setPreferredSize(new Dimension(120, 20));
			sensor2Mode.setModel(new EnumComboBoxModel(SensorMode.class));
		}
		return sensor2Mode;
	}

	/**
	 * This method initializes sensorOutput	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSensorOutput() {
		if (sensorOutput == null) {
			sensorOutput = new JComboBox();
			sensorOutput.setPreferredSize(new Dimension(120, 20));
			sensorOutput.setModel(new EnumComboBoxModel(SensorOutput.class));
		}
		return sensorOutput;
	}

	/**
	 * This method initializes sensorPointingMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSensorPointingMode() {
		if (sensorPointingMode == null) {
			sensorPointingMode = new JComboBox();
			sensorPointingMode.setPreferredSize(new Dimension(120, 20));
			sensorPointingMode.setModel(new EnumComboBoxModel(SensorPointingMode.class));
		}
		return sensorPointingMode;
	}

	/**
	 * This method initializes starepointLatitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getStarepointLatitude() {
		if (starepointLatitude == null) {
			starepointLatitude = new JMeasureSpinner<Double>();
			starepointLatitude.setPreferredSize(new Dimension(120, 20));
			starepointLatitude.setup(MeasureType.GEO_POSITION, 0.0, -Math.PI/2.0, Math.PI/2.0, Math.toRadians(1), 0, 13);
			starepointLatitude.setShowUnit(false);
		}
		return starepointLatitude;
	}

	/**
	 * This method initializes starepointLongitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getStarepointLongitude() {
		if (starepointLongitude == null) {
			starepointLongitude = new JMeasureSpinner<Double>();
			starepointLongitude.setPreferredSize(new Dimension(120, 20));
			starepointLongitude.setup(MeasureType.GEO_POSITION, 0.0, -Math.PI, Math.PI, Math.toRadians(1), 0, 13);
			starepointLongitude.setShowUnit(false);
		}
		return starepointLongitude;
	}

	/**
	 * This method initializes starepointAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getStarepointAltitude() {
		if (starepointAltitude == null) {
			starepointAltitude = new JMeasureSpinner<Float>();
			starepointAltitude.setPreferredSize(new Dimension(120, 20));
			starepointAltitude.setup(MeasureType.ALTITUDE, 0F, 0, 999999999, 1, 0, 4);
		}
		return starepointAltitude;
	}

	/**
	 * This method initializes starepointAltitudeType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getStarepointAltitudeType() {
		if (starepointAltitudeType == null) {
			starepointAltitudeType = new JComboBox();
			starepointAltitudeType.setPreferredSize(new Dimension(120, 20));
			starepointAltitudeType.setModel(new EnumComboBoxModel(AltitudeType.class));
		}
		return starepointAltitudeType;
	}

	/**
	 * This method initializes payloadAzimuth	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getPayloadAzimuth() {
		if (payloadAzimuth == null) {
			payloadAzimuth = new JMeasureSpinner<Float>();
			payloadAzimuth.setPreferredSize(new Dimension(120, 20));
			payloadAzimuth.setup(MeasureType.ATTITUDE_ANGLES, 0F, -MathHelper.TWO_PI, MathHelper.TWO_PI, Math.toRadians(1), 0, 4);
		}
		return payloadAzimuth;
	}

	/**
	 * This method initializes payloadElevation	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getPayloadElevation() {
		if (payloadElevation == null) {
			payloadElevation = new JMeasureSpinner<Float>();
			payloadElevation.setPreferredSize(new Dimension(120, 20));
			payloadElevation.setup(MeasureType.ATTITUDE_ANGLES, 0F, -MathHelper.TWO_PI, MathHelper.TWO_PI, Math.toRadians(1), 0, 4);
		}
		return payloadElevation;
	}

	/**
	 * This method initializes payloadSensorRotation	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getPayloadSensorRotation() {
		if (payloadSensorRotation == null) {
			payloadSensorRotation = new JMeasureSpinner<Float>();
			payloadSensorRotation.setPreferredSize(new Dimension(120, 20));
			payloadSensorRotation.setup(MeasureType.ATTITUDE_ANGLES, 0F, -MathHelper.TWO_PI, MathHelper.TWO_PI, Math.toRadians(1), 0, 4);
		}
		return payloadSensorRotation;
	}

	/**
	 * This method initializes applyLoiter1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getApplyPayload() {
		if (applyPayload == null) {
			applyPayload = new JButton();
			applyPayload.setText("Apply");
			applyPayload.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PayloadActionWaypoint aw = (PayloadActionWaypoint)selectedItem;
					aw.getStationNumber().addStation(((Payload)getStation().getSelectedItem()).getUniqueStationNumber());
					aw.setPayloadAz(getPayloadAzimuth().getValue());
					aw.setPayloadEl(getPayloadElevation().getValue());
					aw.setPayloadSensorRotationAngle(getPayloadSensorRotation().getValue());
					aw.setSensorOutput((SensorOutput)getSensorOutput().getSelectedItem());
					aw.setSetSensor1Mode((SensorMode)getSensor1Mode().getSelectedItem());
					aw.setSetSensor2Mode((SensorMode)getSensor2Mode().getSelectedItem());
					aw.setSetSensorPointingMode((SensorPointingMode)getSensorPointingMode().getSelectedItem());
					aw.setStarepointAltitude(getStarepointAltitude().getValue());
					aw.setStarepointAltitudeType((AltitudeType)getStarepointAltitudeType().getSelectedItem());
					aw.setStarepointLatitude(getStarepointLatitude().getValue());
					aw.setStarepointLongitude(getStarepointLongitude().getValue());
				}
			});
		}
		return applyPayload;
	}

	/**
	 * This method initializes payloadScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getPayloadScroll() {
		if (payloadScroll == null) {
			payloadScroll = new JScrollPane();
			payloadScroll.setName("payloadScroll");
			payloadScroll.setViewportView(getPayloadActionPanel());
		}
		return payloadScroll;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
