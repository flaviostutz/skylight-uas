package br.skylight.uav.plugins.onboardintegration;

import java.io.DataInputStream;
import java.util.logging.Logger;
import java.util.zip.CheckedInputStream;

import br.skylight.commons.StringHelper;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.CRC8;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.instruments.GeoMagJ;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsFailures;
import br.skylight.uav.services.InstrumentsInfos;
import br.skylight.uav.services.InstrumentsListener;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.InstrumentsWarnings;

@ServiceImplementation(serviceDefinition=InstrumentsService.class)
public class OnboardInstrumentsService extends ThreadWorker implements InstrumentsService {

	private static final Logger logger = Logger.getLogger(OnboardInstrumentsService.class.getName());
	
	private static final int PACKET_SIZE = 37;
	private DataInputStream is = null;
	private CRC8 checkSum;
	
	private InstrumentsData data = new InstrumentsData();
	private InstrumentsData tempData = new InstrumentsData();
	private InstrumentsListener listener;

	//reading corrections
	private float pitchTrimming = 0;
	private float rollTrimming = 0;	
	private float yawTrimming = 0;
	
	private TimedBoolean printTimer = new TimedBoolean(1000);
	private int discardedMessagesCounter = 0;
	
	//just for optimization (could be inside method)
	private int crcValue;
	private int packetsAvailable;
	
	@ServiceInjection
	public GPSService gpsService;

	@ServiceInjection
	public RepositoryService repositoryService;

	@MemberInjection
	public OnboardConnections onboardConnections;
	
	public OnboardInstrumentsService() {
		super(60, 200, 3000);
	}
	
	@Override
	public void onActivate() throws Exception {
		checkSum = new CRC8();
		is = new DataInputStream(new CheckedInputStream(onboardConnections.getAvionicsConnectionParams().resolveConnection().getInputStream(), checkSum));
	}
	
	@Override
	public void onDeactivate() throws Exception {
		//don't close avionics connection because actuators gateway may be using it 
	}
	
	@Override
	public void step() throws Exception {
		try {
//			long t = System.currentTimeMillis();
			// drain stream until header
//			while (is.read() != '$') {}
			
			// if buffer has more than a whole package, go for the next package
//			if(is.available() > PACKET_SIZE) {
				packetsAvailable = (int)Math.floor(is.available()/PACKET_SIZE);
				int discardedPackets = (Math.max(0, packetsAvailable-1));
				is.skip(discardedPackets*PACKET_SIZE);
				while (is.read() != '$') {}
				discardedMessagesCounter += discardedPackets;
//			}

			//reset CRC calculator
			checkSum.reset();
//			long time = System.currentTimeMillis()-t;
	
			//parse message
			tempData.readState(is);
	
			//verify checksum
			//if checksum is not OK, the parsed contents will be discarded
			crcValue = (int)(checkSum.getValue() & 0xFF);//truncate to 8 bits
			if(is.read()==crcValue) {
				data.copyState(tempData);
				
				//record all data to a csv file
				if(listener!=null) {
					listener.onInstrumentsDataUpdated();
				}
				
				//FIXME: REMOVE. THIS IS FOR TESTS
				if(printTimer.checkTrue()) {
					System.out.println(
						"> " +
						StringHelper.formatFixedString(data.getRoll()) + ";" + 
						StringHelper.formatFixedString(data.getPitch()) + ";" + 
						StringHelper.formatFixedString(data.getYaw()) + "; P" + 
						StringHelper.formatFixedString(data.getStaticPressure()) + ";" + 
						StringHelper.formatFixedString(data.getDynamicPressure()) + "; E" + 
						StringHelper.formatFixedString(data.getEngineCilinderHeadTemperature()) + ";" + 
						StringHelper.formatFixedString(data.getEngineRPM()) + "; B" + 
						StringHelper.formatFixedString(data.getMainBattVolts()) + ";" + 
						StringHelper.formatFixedString(data.getAuxBattVolts()) + "; R" + 
						StringHelper.formatFixedString(data.getRollRate()) + ";" + 
						StringHelper.formatFixedString(data.getPitchRate()) + ";" + 
						StringHelper.formatFixedString(data.getYawRate()) + "; A" + 
						StringHelper.formatFixedString(data.getAccelX()) + ";" + 
						StringHelper.formatFixedString(data.getAccelY()) + ";" + 
						StringHelper.formatFixedString(data.getAccelZ()) + "; H" + 
						StringHelper.formatFixedString(data.getAutopilotTemperature()) + ";" +
						data.getInstrumentsFailures().toString() + ";" + 
						data.getInstrumentsWarnings().toString() + ";" + 
						data.getInstrumentsInfos().toString() + ";" + 
						StringHelper.formatFixedString(data.getIncomingHz()) + "Hz; " +
						discardedMessagesCounter);
				}
				
			} else {
				logger.finer("EFIS CRC error. Discarding message.");
				discardedMessagesCounter++;
				
			}
			
			if(!isReady()) {
				setReady(true);
			}
		} catch (Exception e) {
			logger.warning("Exception parsing hardware data. e="+e.toString());
		}
	}

	@Override
	public float getPitch() {
		return data.getPitch() + pitchTrimming;
	}

	@Override
	public float getRoll() {
		return data.getRoll() + rollTrimming;
	}

	@Override
	public float getYaw() {
		//will perform geo magnetic declination correction because IMU uses 
		//magnetometers readings for yaw reference
		//FIXME validate this in Australia because during Outback Challenge we faced problems with this correction 
		return data.getYaw() + yawTrimming + (float)GeoMagJ.getCurrentDeclination(gpsService.getPosition().getLatitude(), gpsService.getPosition().getLongitude(), gpsService.getPosition().getAltitude());
//		return data.getYaw() + yawTrimming;
	}
	
	@Override
	public void reloadVehicleConfiguration() {
		SkylightVehicleConfigurationMessage ac = repositoryService.getSkylightVehicleConfiguration();
		rollTrimming = ac.getSensorRollTrim();
		pitchTrimming = ac.getSensorPitchTrim();
		yawTrimming = ac.getSensorYawTrim();
	}
	
	@Override
	public float getPitchRate() {
		return data.getPitchRate();
	}

	@Override
	public float getRollRate() {
		return data.getRollRate();
	}

	@Override
	public float getYawRate() {
		return data.getYawRate();
	}
	
	@Override
	public float getStaticPressure() {
		return data.getStaticPressure();
	}

	@Override
	public float getPitotPressure() {
		return data.getDynamicPressure();
	}

	@Override
	public float getAccelerationX() {
		return data.getAccelX();
	}

	@Override
	public float getAccelerationY() {
		return data.getAccelY();
	}

	@Override
	public float getAccelerationZ() {
		return data.getAccelZ();
	}

	@Override
	public float getAutoPilotTemperature() {
		return data.getAutopilotTemperature();
	}

	@Override
	public float getAuxiliaryBatteryLevel() {
		return data.getAuxBattVolts();
	}

	@Override
	public float getMainBatteryLevel() {
		return data.getMainBattVolts();
	}

	@Override
	public int getEngineRPM() {
		return data.getEngineRPM();
	}

	@Override
	public float getEngineCilinderTemperature() {
		return data.getEngineCilinderHeadTemperature();
	}
	
	@Override
	public int getEffectiveActuatorsMessageFrequency() {
		return data.getIncomingHz();
	}

	@Override
	public InstrumentsFailures getInstrumentsFailures() {
		return data.getInstrumentsFailures();
	}

	@Override
	public InstrumentsInfos getInstrumentsInfos() {
		return data.getInstrumentsInfos();
	}

	@Override
	public InstrumentsWarnings getInstrumentsWarnings() {
		return data.getInstrumentsWarnings();
	}
	
	public OnboardConnections getOnboardConnections() {
		return onboardConnections;
	}

	@Override
	public void setInstrumentsListener(InstrumentsListener listener) {
		this.listener = listener;
	}
	
	public int getDiscardedMessagesCounter() {
		return discardedMessagesCounter;
	}
	
}