package br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import br.skylight.commons.SkylightMission;
import br.skylight.commons.Vehicle;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.core.VehicleControlExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;

@ExtensionPointImplementation(extensionPointDefinition=VehicleControlExtensionPoint.class)
public class SkylightVehicleControlServiceExtensionPointImpl implements VehicleControlExtensionPoint {

	private static final Logger logger = Logger.getLogger(SkylightVehicleControlServiceExtensionPointImpl.class.getName());
	
	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@Override
	public void createNewMission(int vehicleId) {
		SkylightVehicle sv = skylightVehicleControlService.resolveSkylightVehicle(vehicleId);
		sv.setSkylightMission(new SkylightMission());
	}

	@Override
	public void loadMission(int vehicleId, DataInputStream dis) {
		try {
			skylightVehicleControlService.resolveSkylightMission(vehicleId).readState(dis);
		} catch (IOException e) {
			logger.warning("Couldn't read skylight mission state. e=" + e.toString());
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
	}

	@Override
	public void saveMission(int vehicleId, DataOutputStream dos) {
		try {
			skylightVehicleControlService.resolveSkylightMission(vehicleId).writeState(dos);
		} catch (IOException e) {
			logger.warning("Couldn't save skylight mission state. e=" + e.toString());
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
	}

	@Override
	public void sendMissionToVehicle(int vehicleId) {
		SkylightVehicle sv = skylightVehicleControlService.resolveSkylightVehicle(vehicleId);
		if(sv.getSkylightMission()!=null) {
			List<Message> mm = sv.getSkylightMission().getAllMissionMessages();
			for (Message sm : mm) {
				sm.setVehicleID(vehicleId);
				sm.setTimeStamp(System.currentTimeMillis()/1000.0);
				messagingService.sendMessage(sm);
			}
		}
	}

	@Override
	public void validateMission(int vehicleId, VerificationResult vr) {
		Vehicle v = vehicleControlService.resolveVehicle(vehicleId);
		SkylightVehicle sv = skylightVehicleControlService.resolveSkylightVehicle(vehicleId);
		skylightVehicleControlService.resolveSkylightMission(vehicleId).validate(vr, v.getMission(), v.getVehicleConfiguration(), sv.getSkylightVehicleConfiguration());
	}

	@Override
	public String getExtensionIdentification() {
		return "skylight";
	}

	@Override
	public boolean isCompatibleWith(VehicleType vehicleType) {
		return vehicleType.equals(VehicleType.TYPE_60);
	}

}
