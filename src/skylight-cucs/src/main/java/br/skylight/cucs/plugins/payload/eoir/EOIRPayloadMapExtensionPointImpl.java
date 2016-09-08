package br.skylight.cucs.plugins.payload.eoir;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.SetEOIRPointingMode;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.PayloadSteeringListener;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class EOIRPayloadMapExtensionPointImpl extends ControlMapExtensionPoint implements MessageListener, PayloadSteeringListener {

	private static final String PREFIX_PAYLOAD_FOV = "payload-fov-";
	private static final String PREFIX_PAYLOAD_STARE = "payload-stare-";

	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public MessagingService messagingService;

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M302, this);
		subscriberService.addPayloadSteeringListener(this);
	}
	
	@Override
	public List<JMenuItem> prepareContextMenuItems(final GeoPosition clickPosition) {
		List<JMenuItem> items = new ArrayList<JMenuItem>();
		if(getCurrentVehicle()!=null) {
			if(getMapKit().getLastSelectedButton3()==null) {
				//STARE PAYLOAD
				JMenu mi1 = new JMenu("Stare with");
				for (final Payload p : getCurrentVehicle().getPayloads().values()) {
					if(p.getPayloadType().equals(PayloadType.EO) || p.getPayloadType().equals(PayloadType.EOIR) || p.getPayloadType().equals(PayloadType.IR) || p.getPayloadType().equals(PayloadType.FIXED_CAMERA)) {
						JMenuItem mi11 = new JMenuItem(p.getLabel());
						mi11.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//KEEP PAYLOAD LOOKING AT CLICKED POSITION
								starePosition(clickPosition, p);
							}
						});
						mi1.add(mi11);
					}
				}
				items.add(mi1);
				
				//LOITER + STARE PAYLOAD
				JMenu mi2 = new JMenu("Loiter + Stare with");
				for (final Payload p : getCurrentVehicle().getPayloads().values()) {
					if(p.getPayloadType().equals(PayloadType.EO) || p.getPayloadType().equals(PayloadType.EOIR) || p.getPayloadType().equals(PayloadType.IR) || p.getPayloadType().equals(PayloadType.FIXED_CAMERA)) {
						JMenuItem mi21 = new JMenuItem(p.getLabel());
						mi21.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//PERFORM LOITER
								LoiterConfiguration lc = getCurrentVehicle().resolveLoiterConfiguration();
								lc.setLatitude(Math.toRadians(clickPosition.getLatitude()));
								lc.setLongitude(Math.toRadians(clickPosition.getLongitude()));
								
								//send steering for defining new loiter coordinates
								VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
								vs.setLoiterPositionLatitude(lc.getLatitude());
								vs.setLoiterPositionLongitude(lc.getLongitude());
								vs.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
								vehicleControlService.sendVehicleSteeringCommand(vs);
								
								//send loiter configuration
								LoiterConfiguration slc = messagingService.resolveMessageForSending(LoiterConfiguration.class);
								slc.copyParametersFrom(lc);
								slc.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
								messagingService.sendMessage(slc);

								if(!getCurrentVehicle().isCurrentMode(FlightPathControlMode.LOITER)) {
									VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
									m.setSelectFlightPathControlMode(FlightPathControlMode.LOITER);
									m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
									messagingService.sendMessage(m);
								}
								subscriberService.notifyLoiterEvent(lc, EventType.UPDATED, null);
								
								//KEEP PAYLOAD LOOKING AT CLICKED POSITION
								starePosition(clickPosition, p);
							}

						});
						mi2.add(mi21);
					}
				}
				items.add(mi2);
			}
		}
		
		return items;
	}

	private void starePosition(GeoPosition clickPosition, Payload p) {
		//send payload command
		EOIRLaserPayloadCommand ps = vehicleControlService.resolveEOIRLaserPayloadCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), p.getUniqueStationNumber());
		ps.setSetEOIRPointingMode(SetEOIRPointingMode.LAT_LONG_SLAVED);
		vehicleControlService.sendEOIRLaserPayloadCommand(ps);
		//send lat/long stare point
		PayloadSteeringCommand pc = vehicleControlService.resolvePayloadSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), p.getUniqueStationNumber());
		pc.setAltitude(0);
		pc.setAltitudeType(AltitudeType.AGL);
		pc.setLatitude(Math.toRadians(clickPosition.getLatitude()));
		pc.setLongitude(Math.toRadians(clickPosition.getLongitude()));
		vehicleControlService.sendPayloadSteeringCommand(pc);
	}
	
	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}

	private MapElementGroup<EOIRPayloadFOVMapElement> resolveCameraFOVGroup(final Vehicle vehicle, final Payload payload) {
		String groupId = PREFIX_PAYLOAD_FOV+vehicle.getVehicleID().getVehicleID()+"-"+payload.getUniqueStationNumber();
		MapElementGroup<EOIRPayloadFOVMapElement> g = getMapKit().getMapElementGroup(groupId, EOIRPayloadFOVMapElement.class);
		if(g==null) {
			g = new MapElementGroup<EOIRPayloadFOVMapElement>(getMapKit(),
					"Camera FOV view for " + payload.getLabel() + "@" + vehicle.getLabel(), 2,
					new MapElementBridge<EOIRPayloadFOVMapElement>() {
						@Override
						public EOIRPayloadFOVMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<EOIRPayloadFOVMapElement> group) {
							EOIRPayloadFOVMapElement pm = new EOIRPayloadFOVMapElement();
							pm.setup(payload, vehicle);
							return pm;
						};
					}, new EOIRPayloadFOVPainter<EOIRPayloadFOVMapElement>()
				);
			g.setMaxAllowedElements(1);
			getMapKit().addMapElementGroup(g, groupId);
		}
		if(g.getElements().size()==0) {
			g.createElement(new GeoPosition(0,0), 0, EOIRPayloadFOVMapElement.class);
		}
		return g;
	}

	private MapElementGroup<EOIRPayloadStareMapElement> resolveCameraStareGroup(final Vehicle vehicle, final Payload payload) {
		String groupId = PREFIX_PAYLOAD_STARE+vehicle.getVehicleID().getVehicleID()+"-"+payload.getUniqueStationNumber();
		MapElementGroup<EOIRPayloadStareMapElement> g = getMapKit().getMapElementGroup(groupId, EOIRPayloadStareMapElement.class);
		if(g==null) {
			g = new MapElementGroup<EOIRPayloadStareMapElement>(getMapKit(),
					"Camera stare point for " + payload.getLabel() + "@" + vehicle.getLabel(), 0,
					new MapElementBridge<EOIRPayloadStareMapElement>() {
						@Override
						public EOIRPayloadStareMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<EOIRPayloadStareMapElement> group) {
							EOIRPayloadStareMapElement pm = new EOIRPayloadStareMapElement();
							pm.setup(payload, vehicle);
							return pm;
						};
					}, new EOIRPayloadStarePainter<EOIRPayloadStareMapElement>()
				);
			g.setMaxAllowedElements(1);
			getMapKit().addMapElementGroup(g, groupId);
		}
		if(g.getElements().size()==0) {
			g.createElement(new GeoPosition(0,0), 0, EOIRPayloadStareMapElement.class);
		}
		return g;
	}
	
	@Override
	public void onMessageReceived(Message message) {
		//M302
		if(message instanceof EOIRLaserOperatingState) {
			EOIRLaserOperatingState m = (EOIRLaserOperatingState)message;
			Vehicle v = vehicleControlService.resolveVehicle(message.getVehicleID());
			Payload p = vehicleControlService.resolvePayload(m.getVehicleID(), m.getStationNumber().getStations().get(0));
			resolveCameraFOVGroup(v, p).getElement(0).setup(p, v);
			updateGUI();
		}
	}

	@Override
	public void onPayloadSteeringEvent(Payload payload) {
		Vehicle v = vehicleControlService.resolveVehicle(payload.getVehicleID().getVehicleID());
		resolveCameraStareGroup(v, payload).getElement(0).setup(payload, v);
		updateGUI();
	}
	
	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
		getMapKit().addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				if(mapElement instanceof EOIRPayloadStareMapElement) {
					EOIRPayloadStareMapElement me = (EOIRPayloadStareMapElement)mapElement;
					if(eventType.equals(EventType.UPDATED)) {
						//send steering for defining new stare point coordinates
						PayloadSteeringCommand ps = vehicleControlService.resolvePayloadSteeringCommandForSending(me.getVehicle().getVehicleID().getVehicleID(), me.getPayload().getUniqueStationNumber());
						//no need to set position because map element already did this when map updated its position
						vehicleControlService.sendPayloadSteeringCommand(ps);
						
						//change mode if needed
						if(me.getPayload().resolveEoIrPayload().getEoIrLaserPayloadCommand()!=null) {
							if(!me.getPayload().getEoIrPayload().getEoIrLaserPayloadCommand().getSetEOIRPointingMode().equals(SetEOIRPointingMode.LAT_LONG_SLAVED)) {
								EOIRLaserPayloadCommand pc = vehicleControlService.resolveEOIRLaserPayloadCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), me.getPayload().getUniqueStationNumber());
								pc.setSetEOIRPointingMode(SetEOIRPointingMode.LAT_LONG_SLAVED);
								vehicleControlService.sendEOIRLaserPayloadCommand(pc);
							}
						}
					}
					subscriberService.notifyPayloadSteeringEvent(me.getPayload());
					getMapKit().updateUI();
				}
			}
        });
	}
	
}
