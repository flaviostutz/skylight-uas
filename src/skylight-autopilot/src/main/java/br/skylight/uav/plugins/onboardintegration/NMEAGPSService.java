package br.skylight.uav.plugins.onboardintegration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.MeasureHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.infra.GPSUpdate;
import br.skylight.uav.infra.NMEAParser;
import br.skylight.uav.infra.GPSUpdate.FixQuality;
import br.skylight.uav.services.GPSService;

@ServiceImplementation(serviceDefinition=GPSService.class)
public class NMEAGPSService extends ThreadWorker implements GPSService {

	private static final Logger logger = Logger.getLogger(NMEAGPSService.class.getName());
	
	private NMEAParser nmeaParser = new NMEAParser();
	private long timeOfFirstFix = 0;
	private double lastUpdateTime;
	private static Coordinates firstFixPosition = new Coordinates(0,0,0);
	public BufferedReader reader;
	private GPSUpdate gpsUpdate = new GPSUpdate();
	private TimedBoolean exceptionLog = new TimedBoolean(10000);
	private String line;

	@MemberInjection
	public OnboardConnections onboardConnections;
	
	public NMEAGPSService() {
		super(40, 250, -1);
	}

	public void onActivate() throws IOException {
		InputStream is = onboardConnections.getGpsConnectionParams().resolveConnection().getInputStream();
		reader = new BufferedReader(new InputStreamReader(is, Charset.forName("US-ASCII")));
	}
	
	@Override
	public void onDeactivate() throws Exception {
		reader.close();
	}

	public void step() throws IOException {
		
		try {
			line = reader.readLine();
			// parse nmea messages
			if(nmeaParser.parse(gpsUpdate, line)) {
				lastUpdateTime = System.currentTimeMillis()/1000.0;
				
				//verify time of first fix
				if (timeOfFirstFix == 0 && gpsUpdate.getFixQuality().ordinal() > 0) {
					timeOfFirstFix = System.currentTimeMillis();
					logger.fine(toString() + ": Got first fix");
				}
				
				//keep first fix position
				if (firstFixPosition.getLatitude()==0 && timeOfFirstFix!=0 && (System.currentTimeMillis() - timeOfFirstFix) > 15000) {
					firstFixPosition.setLatitude(getPosition().getLatitude());
					firstFixPosition.setLongitude(getPosition().getLongitude());
					firstFixPosition.setAltitude(getPosition().getAltitude());
					logger.fine("GPSService: Altitude WGS84: " + firstFixPosition.getAltitude() + "m");
				}
			}
		} catch (Exception e) {
			if(exceptionLog.checkTrue()) {
				e.printStackTrace();
				System.out.println("NMEA '"+ line +"'");
			}
		}
			
		if(!isReady()) {
			setReady(true);
		}
			
	}

	@Override
	public float getCourseHeading() {
		return (float)MathHelper.normalizeAngle2(Math.toRadians(gpsUpdate.getCourseMadeGood()));
	}

	@Override
	public float getGroundSpeed() {
		return MeasureHelper.knotsToMetersPerSecond(gpsUpdate.getGroundSpeed());
	}

	@Override
	public FixQuality getFixQuality() {
		return gpsUpdate.getFixQuality();
	}

	@Override
	public int getSatCount() {
		return gpsUpdate.getSatCount();
	}
	
	@Override
	public Coordinates getPosition() {
		return gpsUpdate.getCoordinates();
	}

	@Override
	public Coordinates getPositionOnFirstFix() {
		return firstFixPosition;
	}

	@Override
	public float getAltitudeMSL() {
		return gpsUpdate.getAltitudeMSL();
	}

	@Override
	public double getLastGPSUpdateTime() {
		return lastUpdateTime;
	}
	
}
