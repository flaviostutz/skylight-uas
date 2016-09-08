package br.skylight.uav.infra;

import br.skylight.commons.Coordinates;

/**
 * Representa uma atualizacao de dados do GPS.
 * @author Edu
 */
public class GPSUpdate {
	
    public enum FixQuality {
    	INVALID,
    	GPS_FIX_SPS,
    	DGPS_FIX,
    	PPS_FIX,
    	RTK,
    	RTK_FLOAT,
    	ESTIMATED_DEAD_RECKONING,
    	MANUAL_INPUT_MODE,
    	SIMUATION_MODE
    }
    
    public int sentenceType;
    private Coordinates ZERO_COORDINATES = new Coordinates(0,0,0);
    
    public String courseMadeGood = "0";
//    public String dateTimeOfFix = "";
    public String groundSpeed = "0";
    public String latitude = "0";
    public String latitudeDirection = "N";
    public String longitude = "0";
    public String longitudeDirection = "W";
    public String magneticVariation = "0";
    public String magneticVariationDirection = "0";
    public String quality = "1";
    public String satelliteCount = "0";
    public String altitudeMSL = "0";
    public String heighOfGeoidAboveEllipsoid = "0";
    public String status = "V";
    
    private Coordinates position = new Coordinates();
    
	public void copy(GPSUpdate originalRecord) {
		this.courseMadeGood = originalRecord.courseMadeGood;
//		this.dateTimeOfFix = originalRecord.dateTimeOfFix;
		this.groundSpeed = originalRecord.groundSpeed;
		this.latitude = originalRecord.latitude;
		this.latitudeDirection = originalRecord.latitudeDirection;
		this.longitude = originalRecord.longitude;
		this.longitudeDirection = originalRecord.longitudeDirection;
		this.altitudeMSL = originalRecord.altitudeMSL;
		this.magneticVariation = originalRecord.magneticVariation;
		this.quality = originalRecord.quality;
		this.satelliteCount = originalRecord.satelliteCount;
		this.sentenceType = originalRecord.sentenceType;
		this.status = originalRecord.status;
		this.heighOfGeoidAboveEllipsoid = originalRecord.heighOfGeoidAboveEllipsoid;
		
	}

	public Coordinates getCoordinates() {
		try {
			//return last known position if coordinates/altitude is empty
			if (latitude.trim().length() <= 3 || longitude.trim().length() <= 4 || altitudeMSL.length() == 0) {
				return position;
			}
			
			double lat = Coordinates.convert(latitude.substring(0,2) + ":" + latitude.substring(2));
			double lon = Coordinates.convert(longitude.substring(0,3) + ":" + longitude.substring(3));

			//return last known position if coordinates is invalid
			if (lat==0 || lon==0) {
				return position;
			}
			
			if (latitudeDirection.equals("S")) {
				lat = -lat;
			}
	
			if (longitudeDirection.equals("W")) {
				lon = -lon;
			}
			
			position.setLatitude(lat);
			position.setLongitude(lon);
			position.setAltitude(Float.parseFloat(altitudeMSL) + Float.parseFloat(heighOfGeoidAboveEllipsoid));
			return position;
			
		} catch (Exception e) {
			e.printStackTrace();
			return ZERO_COORDINATES;
		}
	}
	
	public int getSatCount() {
		try {
			return Integer.parseInt(satelliteCount);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public FixQuality getFixQuality() {
		try {
			return FixQuality.values()[Integer.parseInt(quality.length()>0?quality:"0")];
		} catch (Exception e) {
			e.printStackTrace();
			return FixQuality.INVALID;
		}
	}
	
	public float getAltitudeMSL() {
		try {
			return Float.parseFloat(altitudeMSL);
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Course heading in degrees
	 */
	public float getCourseMadeGood() {
		try {
			return Float.parseFloat(courseMadeGood);
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Speed over ground in knots
	 */
	public float getGroundSpeed() {
		try {
			return Float.parseFloat(groundSpeed);
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Magnetic variation in heading degrees
	 */
	public float getMagneticVariation() {
		try {
			return Float.parseFloat((magneticVariationDirection.equals("W")?"-":"")+magneticVariation);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public boolean isWarning() {
		return status.equals("V");
	}
	
}
