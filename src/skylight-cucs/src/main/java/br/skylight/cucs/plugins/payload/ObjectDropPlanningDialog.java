package br.skylight.cucs.plugins.payload;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.MeasureType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.RouteType;
import br.skylight.commons.dli.enums.RunwayDirection;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.SensorOutput;
import br.skylight.commons.dli.enums.SensorPointingMode;
import br.skylight.commons.dli.enums.Side;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.LandingHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.JMeasureSpinner;

public class ObjectDropPlanningDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JButton createButton = null;
	private JMeasureSpinner<Float> dropAltitude = null;
	private JMeasureSpinner<Float> verticalSpeed = null;
	private JMeasureSpinner<Float> windDirection = null;
	private JMeasureSpinner<Float> windSpeed = null;
	private JMeasureSpinner<Double> hitLatitude = null;
	private JMeasureSpinner<Double> hitLongitude = null;
	private JComboBox stare = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel111 = null;
	private JComboBox dispenser = null;
	private Vehicle vehicle;  //  @jve:decl-index=0:
	private SubscriberService subscriberService;  //  @jve:decl-index=0:
	private JButton jButton = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel12 = null;
	private JMeasureSpinner<Float> approach = null;
	
	/**
	 * @param owner
	 */
	public ObjectDropPlanningDialog(Frame owner) {
		super(owner);
		ViewHelper.centerWindow(this);
	}

	public void showDialog(Vehicle vehicle, SubscriberService subscriberService, Coordinates clickLocation) {
		this.vehicle = vehicle;
		this.subscriberService = subscriberService;
		initialize();
		getHitLatitude().setValue(clickLocation.getLatitudeRadians());
		getHitLongitude().setValue(clickLocation.getLongitudeRadians());
		getDropAltitude().setValue(vehicle.getCurrentAltitude(AltitudeType.AGL));
		getVerticalSpeed().setValue(1F);
		AirAndGroundRelativeStates rs = vehicle.getLastReceivedMessage(MessageType.M102);
		if(rs!=null) {
			getWindDirection().setValue(CoordinatesHelper.calculateHeading(rs.getUWind(), rs.getVWind()));
			getWindSpeed().setValue(MeasureHelper.calculateMagnitude(rs.getUWind(), rs.getVWind()));
		}
		setVisible(true);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(283, 293);
		this.setTitle("Object drop planning");
		this.add(getJPanel(), BorderLayout.CENTER);
		CUCSViewHelper.setDefaultIcon(this);
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints4.gridy = 7;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints3.gridy = 7;
			jLabel12 = new JLabel();
			jLabel12.setText("Approach scale:");
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.gridwidth = 2;
			gridBagConstraints25.gridy = 9;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints31.gridx = 1;
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = 0;
			gridBagConstraints30.anchor = GridBagConstraints.EAST;
			gridBagConstraints30.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints30.gridy = 0;
			jLabel111 = new JLabel();
			jLabel111.setText("Dispensable payload:");
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 0;
			gridBagConstraints29.anchor = GridBagConstraints.EAST;
			gridBagConstraints29.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints29.gridy = 8;
			jLabel11 = new JLabel();
			jLabel11.setText("Stare target with:");
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.fill = GridBagConstraints.BOTH;
			gridBagConstraints28.gridy = 8;
			gridBagConstraints28.weightx = 1.0;
			gridBagConstraints28.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints28.gridx = 1;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 1;
			gridBagConstraints24.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.gridy = 6;
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 1;
			gridBagConstraints23.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints23.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints23.gridy = 5;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.gridy = 4;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridy = 3;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 1;
			gridBagConstraints20.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints20.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints20.gridy = 2;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 1;
			gridBagConstraints19.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints19.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints19.gridy = 1;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints17.gridy = 1;
			jLabel5 = new JLabel();
			jLabel5.setText("Drop altitude (AGL):");
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.anchor = GridBagConstraints.EAST;
			gridBagConstraints16.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints16.gridy = 4;
			jLabel4 = new JLabel();
			jLabel4.setText("Wind speed:");
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints15.gridy = 3;
			jLabel3 = new JLabel();
			jLabel3.setText("Wind direction:");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.EAST;
			gridBagConstraints14.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints14.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Object vertical speed:");
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints13.gridy = 6;
			jLabel1 = new JLabel();
			jLabel1.setText("Ground hit longitude:");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.EAST;
			gridBagConstraints12.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints12.gridy = 5;
			jLabel = new JLabel();
			jLabel.setText("Ground hit latitude:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.NORTH;
			gridBagConstraints.gridx = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(jLabel, gridBagConstraints12);
			jPanel.add(jLabel1, gridBagConstraints13);
			jPanel.add(jLabel2, gridBagConstraints14);
			jPanel.add(jLabel3, gridBagConstraints15);
			jPanel.add(jLabel4, gridBagConstraints16);
			jPanel.add(jLabel5, gridBagConstraints17);
			jPanel.add(getDropAltitude(), gridBagConstraints19);
			jPanel.add(getVerticalSpeed(), gridBagConstraints20);
			jPanel.add(getWindDirection(), gridBagConstraints21);
			jPanel.add(getWindSpeed(), gridBagConstraints22);
			jPanel.add(getHitLatitude(), gridBagConstraints23);
			jPanel.add(getHitLongitude(), gridBagConstraints24);
			jPanel.add(getStare(), gridBagConstraints28);
			jPanel.add(jLabel11, gridBagConstraints29);
			jPanel.add(jLabel111, gridBagConstraints30);
			jPanel.add(getDispenser(), gridBagConstraints31);
			jPanel.add(getJPanel1(), gridBagConstraints25);
			jPanel.add(jLabel12, gridBagConstraints3);
			jPanel.add(getApproach(), gridBagConstraints4);
		}
		return jPanel;
	}

	/**
	 * This method initializes createButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCreateButton() {
		if (createButton == null) {
			createButton = new JButton();
			createButton.setText("Create drop route");
			createButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//CALCULATE PATH WAYPOINTS
					//ground distance travelled by object while descending because of wind effect
					float d = (getDropAltitude().getValue()*getWindSpeed().getValue())/getVerticalSpeed().getValue();
					
					//best point of object release in order for it to hit ground at desired location
					Coordinates hitPosition = new Coordinates(Math.toDegrees(getHitLatitude().getValue()), Math.toDegrees(getHitLongitude().getValue()), 0);
					Coordinates dropPosition = CoordinatesHelper.calculateCoordinates(hitPosition, d, 0, CoordinatesHelper.headingToMathReference(getWindDirection().getValue() + Math.PI));//PI for going in oposite direction of wind
					Coordinates actionPosition = CoordinatesHelper.calculateCoordinates(dropPosition, 60, 0, CoordinatesHelper.headingToMathReference(getWindDirection().getValue() + Math.PI));
					
					//the drop route will be alike landing downwind/base/final legs
					Runway r = new Runway();//define touch down positioning
					r.setManeuversSide(Side.LEFT);
					r.setRunwayWidth(20);
					r.setDirection(RunwayDirection.RUNWAY12);
					r.setPoint1(dropPosition);
					r.setPoint2(actionPosition);
					Coordinates[] approachWaypoints = LandingHelper.calculateLandingPoints(r, 0.5F * getApproach().getValue());
					int vid = vehicle.getVehicleID().getVehicleID();
					
					//CREATE MISSION ROUTE/WAYPOINTS
					//create route
					int nextWaypointNumber = vehicle.getMission().getHighestWaypointNumber() + 1;
					AVRoute route = new AVRoute();
					route.setRouteType(RouteType.APPROACH);
					route.setVehicleID(vehicle.getVehicleID().getVehicleID());
					route.setRouteID("Object drop");
					route.setInitialWaypointNumber(nextWaypointNumber);
					route.setVehicleID(vid);
					vehicle.getMission().getRoutes().add(route);
					
					//DOWNWIND LEG start waypoint
					AVPositionWaypoint dw = new AVPositionWaypoint();
					setup(dw, approachWaypoints[0]);
					dw.setWaypointNumber(nextWaypointNumber);
					dw.setNextWaypoint(nextWaypointNumber+1);
					dw.setVehicleID(vid);
					vehicle.getMission().getPositionWaypoints().add(dw);
					
					//stare target with camera during entire procedure
					if(getStare().getSelectedItem()!=null && getStare().getSelectedIndex()!=0) {
						PayloadActionWaypoint pb = new PayloadActionWaypoint();
						pb.setSensorOutput(SensorOutput.SENSOR_1);
						pb.setSetSensor1Mode(SensorMode.TURN_ON);
						pb.setSetSensorPointingMode(SensorPointingMode.LAT_LONG_SLAVED);
						pb.setStarepointAltitude(0);
						pb.setStarepointAltitudeType(AltitudeType.AGL);
						pb.setStarepointLatitude(getHitLatitude().getValue());
						pb.setStarepointLongitude(getHitLongitude().getValue());
						pb.getStationNumber().setUniqueStationNumber(((Payload)getStare().getSelectedItem()).getUniqueStationNumber());
						pb.setWaypointNumber(nextWaypointNumber);
						pb.setVehicleID(vid);
						vehicle.getMission().getPayloadActionWaypoints().add(pb);
					}
					nextWaypointNumber++;
					
					//BASE LEG start waypoint
					AVPositionWaypoint bw = new AVPositionWaypoint();
					setup(bw, approachWaypoints[1]);
					bw.setWaypointNumber(nextWaypointNumber);
					bw.setNextWaypoint(nextWaypointNumber+1);
					bw.setVehicleID(vid);
					vehicle.getMission().getPositionWaypoints().add(bw);
					nextWaypointNumber++;
					
					//FINAL LEG start waypoint
					AVPositionWaypoint fw = new AVPositionWaypoint();
					setup(fw, approachWaypoints[2]);
					fw.setWaypointNumber(nextWaypointNumber);
					fw.setNextWaypoint(nextWaypointNumber+1);
					fw.setVehicleID(vid);
					vehicle.getMission().getPositionWaypoints().add(fw);
					nextWaypointNumber++;
					
					//DROP LOCATION waypoint
					AVPositionWaypoint rw = new AVPositionWaypoint();
					setup(rw, dropPosition);
					rw.setWaypointNumber(nextWaypointNumber);
					rw.setNextWaypoint(nextWaypointNumber+1);
					rw.setVehicleID(vid);
					rw.setTurnType(TurnType.FLYOVER);
					vehicle.getMission().getPositionWaypoints().add(rw);
					nextWaypointNumber++;
					
					//PAYLOAD ACTION WAYPOINT that will trigger drop object when activated
					//position
					AVPositionWaypoint aw = new AVPositionWaypoint();
					setup(aw, actionPosition);
					aw.setWaypointNumber(nextWaypointNumber);
					aw.setNextWaypoint(nextWaypointNumber+1);
					aw.setVehicleID(vid);
					vehicle.getMission().getPositionWaypoints().add(aw);
					//activate sensor for drop
					PayloadActionWaypoint pp = new PayloadActionWaypoint();
					pp.setSensorOutput(SensorOutput.SENSOR_1);
					pp.setSetSensor1Mode(SensorMode.TURN_ON);
					pp.getStationNumber().setUniqueStationNumber(((Payload)getDispenser().getSelectedItem()).getUniqueStationNumber());
					pp.setWaypointNumber(nextWaypointNumber);
					pp.setVehicleID(vid);
					vehicle.getMission().getPayloadActionWaypoints().add(pp);
					nextWaypointNumber++;
					
					//LOITER ABOVE TARGET after releasing object
					AVPositionWaypoint lw = new AVPositionWaypoint();
					setup(lw, hitPosition);
					lw.setWaypointNumber(nextWaypointNumber);
					lw.setVehicleID(vid);
					vehicle.getMission().getPositionWaypoints().add(lw);
					//loiter definition
					AVLoiterWaypoint alw = new AVLoiterWaypoint();
					alw.setLoiterRadius(140);
					alw.setWaypointLoiterTime(1800);
					alw.setWaypointLoiterType(LoiterType.CIRCULAR);
					alw.setWaypointNumber(nextWaypointNumber);
					alw.setVehicleID(vid);
					vehicle.getMission().getLoiterWaypoints().add(alw);
					
					subscriberService.notifyMissionEvent(vehicle.getMission(), EventType.UPDATED, null);
					subscriberService.notifyMissionWaypointEvent(vehicle.getMission(), dw, EventType.SELECTED, null);
					
					setVisible(false);
				}
			});
		}
		return createButton;
	}

	protected void setup(AVPositionWaypoint pw, Coordinates pos) {
		pw.setWaypointToSpeed(vehicle.getVehicleConfiguration().getOptimumEnduranceIndicatedAirspeed());
		pw.setWaypointSpeedType(WaypointSpeedType.INDICATED_AIRSPEED);
		pw.setWaypointToAltitude(getDropAltitude().getValue());
		pw.setWaypointAltitudeType(AltitudeType.AGL);
		pw.setWaypointToLatitudeOrRelativeY(pos.getLatitudeRadians());
		pw.setWaypointToLongitudeOrRelativeX(pos.getLongitudeRadians());
	}

	/**
	 * This method initializes dropAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getDropAltitude() {
		if (dropAltitude == null) {
			dropAltitude = new JMeasureSpinner<Float>();
			dropAltitude.setup(MeasureType.ALTITUDE, 150F, 0, 9999999, 1, 0, 2);
		}
		return dropAltitude;
	}

	/**
	 * This method initializes verticalSpeed	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getVerticalSpeed() {
		if (verticalSpeed == null) {
			verticalSpeed = new JMeasureSpinner<Float>();
			verticalSpeed.setup(MeasureType.GROUND_SPEED, 1F, 0.01, 9999999, 1, 0, 2);
		}
		return verticalSpeed;
	}

	/**
	 * This method initializes windDirection	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getWindDirection() {
		if (windDirection == null) {
			windDirection = new JMeasureSpinner<Float>();
			windDirection.setup(MeasureType.HEADING, 0F, 0, Math.PI*2, Math.toRadians(1), 0, 2);
		}
		return windDirection;
	}

	/**
	 * This method initializes windSpeed	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getWindSpeed() {
		if (windSpeed == null) {
			windSpeed = new JMeasureSpinner<Float>();
			windSpeed.setup(MeasureType.WIND_SPEED, 0F, 0, 9999999, 1, 0, 2);
		}
		return windSpeed;
	}

	/**
	 * This method initializes hitLatitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getHitLatitude() {
		if (hitLatitude == null) {
			hitLatitude = new JMeasureSpinner<Double>();
			hitLatitude.setup(MeasureType.GEO_POSITION, 0.0, -Math.PI, Math.PI, Math.toRadians(1), 0, 8);
		}
		return hitLatitude;
	}

	/**
	 * This method initializes hitLongitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getHitLongitude() {
		if (hitLongitude == null) {
			hitLongitude = new JMeasureSpinner<Double>();
			hitLongitude.setup(MeasureType.GEO_POSITION, 0.0, -Math.PI*2, Math.PI*2, Math.toRadians(1), 0, 8);
		}
		return hitLongitude;
	}

	/**
	 * This method initializes stare	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getStare() {
		if (stare == null) {
			stare = new JComboBox();
			DefaultComboBoxModel m = new DefaultComboBoxModel();
			m.addElement("-No EO/IR payload-");
			for (Payload p : vehicle.getPayloads().values()) {
				if(p.getPayloadType().equals(PayloadType.EO) 
					|| p.getPayloadType().equals(PayloadType.EOIR) 
					|| p.getPayloadType().equals(PayloadType.IR)) {
					m.addElement(p);
				}
			}
			stare.setModel(m);
		}
		return stare;
	}

	/**
	 * This method initializes dispenser	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDispenser() {
		if (dispenser == null) {
			dispenser = new JComboBox();
			DefaultComboBoxModel m = new DefaultComboBoxModel();
			for (Payload p : vehicle.getPayloads().values()) {
				if(p.getPayloadType().equals(PayloadType.DISPENSABLE_PAYLOAD)) {
					m.addElement(p);
				}
			}
			dispenser.setModel(m);
		}
		return dispenser;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Cancel");
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.gridwidth = 1;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getCreateButton(), gridBagConstraints2);
			jPanel1.add(getJButton(), gridBagConstraints1);
		}
		return jPanel1;
	}

	/**
	 * This method initializes approach	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getApproach() {
		if (approach == null) {
			approach = new JMeasureSpinner<Float>();
			approach.setup(null, 1F, 0, 99, 0.1F, 1, 2);
		}
		return approach;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
