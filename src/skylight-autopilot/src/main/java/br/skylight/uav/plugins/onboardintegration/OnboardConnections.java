package br.skylight.uav.plugins.onboardintegration;

import br.skylight.commons.infra.Worker;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.storage.RepositoryService;

@ManagedMember
public class OnboardConnections extends Worker {

	public static final String PREFIX_MODEM = "modem";
	
//	private SerialConnectionParams modemConnectionParams;
	private SerialConnectionParams gpsConnectionParams;
	private SerialConnectionParams avionicsConnectionParams;
	private SerialConnectionParams cameraConnectionParams;
	
	@ServiceInjection
	public RepositoryService repositoryService;

	@Override
	public void onActivate() throws Exception {
//		modemConnectionParams = new SerialConnectionParams(repositoryService.getConfigProperties(), PREFIX_MODEM);
		gpsConnectionParams = new SerialConnectionParams(repositoryService.getConfigProperties(), "gps");
		avionicsConnectionParams = new SerialConnectionParams(repositoryService.getConfigProperties(), "autopilotHardware");
		cameraConnectionParams = new SerialConnectionParams(repositoryService.getConfigProperties(), "camera");
	}
	
	@Override
	public void onDeactivate() throws Exception {
		//close any remaining serial connections
//		System.out.println("Closing modem connection...");
//		modemConnectionParams.closeConnection();
		System.out.println("Closing gps connection...");
		gpsConnectionParams.closeConnection();
		System.out.println("Closing efis connection...");
		avionicsConnectionParams.closeConnection();
		System.out.println("Closing camera connection...");
		cameraConnectionParams.closeConnection();
	}
	
	public SerialConnectionParams getAvionicsConnectionParams() {
		return avionicsConnectionParams;
	}
	public SerialConnectionParams getCameraConnectionParams() {
		return cameraConnectionParams;
	}
	public SerialConnectionParams getGpsConnectionParams() {
		return gpsConnectionParams;
	}
//	public SerialConnectionParams getModemConnectionParams() {
//		return modemConnectionParams;
//	}
	
}
