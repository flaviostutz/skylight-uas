package br.skylight.uav.infra;

import java.util.logging.Logger;

import br.skylight.uav.infra.GPSUpdate.FixQuality;

/**
 * NMEA-0183 Parser. Parses data sent by GPS receiver
 */
public class NMEAParser {

	private GPSUpdate lastPositionData = new GPSUpdate();
	private static final Logger logger = Logger.getLogger(NMEAParser.class.getName());

	/**
	 * Type not supported.
	 */
	public static final int TYPE_NA = -1;

	/**
	 * Start marker, see class GPS.
	 */
	public static final int TYPE_START = -2;

	/**
	 * Type GPRMC.
	 */
	public static final int TYPE_GPRMC = 0;

	/**
	 * Type GPGGA.
	 */
	public static final int TYPE_GPGGA = 1;

	/**
	 * Parses a string sent by GPS receiver.
	 * @param gpsUpdate 
	 * 
	 * @param s
	 *            String to be parsed
	 * @param record
	 *            Record to store data
	 * @return Type of record
	 */
	public boolean parse(GPSUpdate result, String s) {

		// remove garbage in the beginning
		int trimPosition = s.indexOf('$');
		if(trimPosition==-1) return false;
		s = s.substring(trimPosition);
		int termIndex = s.lastIndexOf('*');
		if(termIndex==-1) return false;
		String payload = s.substring(1, termIndex);
		byte rxChecksum = Byte.parseByte(s.substring(termIndex + 1), 16);
		if (rxChecksum != NMEAParser.computeChecksum(payload.getBytes())) {
			logger.info("NMEA checksum didn't match");
			return false;
		}

		// Tokenizer to separate tokens
		String[] tokens = s.split("\\,");
//		StringTokenizer tokenizer = new StringTokenizer(s, ",");
		// Type of record

//		String token = tokenizer.nextToken();
		boolean positionUpdated = false;

		if (tokens[0].equals("$GPRMC")) {
			result.sentenceType = TYPE_GPRMC;
//			record.dateTimeOfFix = tokens[1].substring(0, 2) + ":" + tokens[1].substring(2, 4) + ":" + tokens[1].substring(4, 6);
			result.status = tokens[2];
			try {
				if(tokens[3].length()>0 && Float.parseFloat(tokens[3])!=0F && result.getFixQuality().ordinal()!=FixQuality.INVALID.ordinal()) {
					result.latitude = tokens[3];
					result.latitudeDirection = tokens[4];
					result.longitude = tokens[5];
					result.longitudeDirection = tokens[6];
					result.groundSpeed = tokens[7];
					result.courseMadeGood = tokens[8];
	//				record.dateTimeOfFix += "/" + tokens[9].substring(0, 2) + "." + tokens[9].substring(2, 4) + "." + tokens[9].substring(4, 6);
					result.magneticVariation = tokens[10];
					result.magneticVariationDirection = tokens[11];
					positionUpdated = true;
				}
			} catch (Exception e) {
				//could not parse number
				e.printStackTrace();
			}
			
		} else if (tokens[0].equals("$GPGGA")) {
			result.sentenceType = TYPE_GPGGA;
			//tokens[1] Time of fix
			//ignore those position data because GPRMC seems to have higher precision
//			record.latitude = tokens[2];
//			record.latitudeDirection = tokens[3];
//			record.longitude= tokens[4];
//			record.longitudeDirection = tokens[5];
			result.quality = tokens[6];
			result.satelliteCount = tokens[7];
			// tokens[8] horizontal dillution
			result.altitudeMSL = tokens[9];
			//tokens[10] M
			result.heighOfGeoidAboveEllipsoid = tokens[11];
			// Ignore rest
		}

		return positionUpdated;
	}

	/**
	 * Compute an NMEA checksum for the given array of bytes.
	 */
	public static byte computeChecksum(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < bytes.length; i++)
			result = result ^ bytes[i];
		return (byte) result;
	}

}