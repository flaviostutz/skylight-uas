/*
*   GeoMagJ  -- Implemenation of the DMA/NIMA GEOMAG algorithm for a World Magnetic Model.
*
*   Copyright (C) 2004-2005 by Joseph A. Huwaldt
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*  Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package br.skylight.uav.plugins.control.instruments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Calendar;

import br.skylight.commons.infra.MathHelper;


/**
*  <p> The National Geospatial-Intelligence Agency (NGIA) World Magnetic Model,
*      implemented using a Java port of the public domain FORTRAN GEOMAG algorithm.
*      This is a model of the entire world's geomagnetic system for a
*      period of 5 years. </p>
*
*  <p> As geomagnetic model data is only reliable for a few years from
*      the epoch date of the model, computing data for a date that exceeds
*      the life of the model may produce inaccurate results. </p>
*
*  <p> The following notes were included with the original FORTRAN GEOMAG code:
*      <code>
*     Software and Model Support
*     	National Geophysical Data Center
*     	NOAA EGC/2
*     	325 Broadway
*     	Boulder, CO 80303 USA
*     	Attn: Susan McLean or Stefan Maus
*     	Phone:  (303) 497-6478 or -6522
*     	Email:  Susan.McLean@noaa.gov or Stefan.Maus@noaa.gov
*		Web: http://www.ngdc.noaa.gov/seg/WMM/
*
*     Sponsoring Government Agency
*	   National Geospatial-Intelligence Agency
*    	   PRG / CSAT, M.S. L-41
*    	   3838 Vogel Road
*    	   Arnold, MO 63010
*    	   Attn: Craig Rollins
*    	   Phone:  (314) 263-4186
*    	   Email:  Craig.M.Rollins@Nga.Mil
*
*      GEOMAG PROGRAMMED BY:  JOHN M. QUINN     7/19/90
*      NOW AT:                GEOMAGNETICS GROUP
*                             U.S. GEOLOGICAL SURVEY
*                             EMAIL: quinn@ghtmail.cr.usgs.gov
*
*      MODEL: THE WMM SERIES GEOMAGNETIC MODELS ARE COMPOSED OF TWO PARTS:
*      THE MAIN FIELD MODEL, WHICH IS VALID AT THE BASE EPOCH OF
*      THE CURRENT MODEL, AND A SECULAR VARIATION MODEL, WHICH
*      ACCOUNTS FOR SLOW TEMPORAL VARIATIONS IN THE MAIN
*      GEOMAGNETIC FIELD FROM THE BASE EPOCH TO A MAXIMUM OF 5
*      YEARS BEYOND THE BASE EPOCH.  FOR EXAMPLE, THE BASE EPOCH
*      OF THE WMM-2000 MODEL IS 2000.0.  THIS MODEL IS THEREFORE
*      CONSIDERED VALID BETWEEN 2000.0 AND 2005.0.  THE COMPUTED
*      MAGNETIC PARAMETERS ARE REFERENCED TO THE WGS-84 ELLIPSOID.
*
*      ACCURACY:  IN OCEAN AREAS AT THE EARTH'S SURFACE OVER THE ENTIRE
*      5-YEAR LIFE, THE ESTIMATED RMS ERRORS FOR THE VARIOUS
*      MAGNETIC COMPONENTS ARE:
*
*      DECLINATION   -   0.5 Degrees
*      INCLINATION   -   0.5 Degrees
*      Total Intensity (F)- 280.0 nanoTeslas (1 nanoTesla = 1 gamma)
*      GV    -   0.5 Degrees
*
*      OVER LAND THE RMS ERRORS ARE EXPECTED TO BE SOMEWHAT
*      HIGHER, ALTHOUGH THE RMS ERRORS FOR DEC, DIP, AND GV ARE
*      STILL ESTIMATED TO BE LESS THAN 1.0 DEGREE, FOR THE ENTIRE
*      5-YEAR LIFE OF THE MODEL AT THE EARTH'S SURFACE.
*
*      THE ACCURACY AT ANY GIVEN TIME OF ALL FOUR GEOMAGNETIC
*      PARAMETERS DEPENDS ON THE GEOMAGNETIC LATITUDE.  THE
*      ERRORS ARE LEAST AT THE EQUATOR AND GREATEST AT THE
*      MAGNETIC POLES.
*
*      IT IS VERY IMPORTANT TO NOTE THAT A DEGREE AND ORDER 12
*      MODEL, SUCH AS WMM, DESCRIBES ONLY THE LONG WAVELENGTH
*      SPATIAL MAGNETIC FLUCTUATIONS DUE TO EARTH'S CORE.  NOT
*      INCLUDED IN THE WMM SERIES MODELS ARE INTERMEDIATE AND
*      SHORT WAVELENGTH SPATIAL FLUCTUATIONS OF THE GEOMAGNETIC
*      FIELD WHICH ORIGINATE IN THE EARTH'S MANTLE AND CRUST.
*      CONSEQUENTLY, ISOLATED ANGULAR ERRORS AT VARIOUS POSITIONS
*      ON THE SURFACE (PRIMARILY OVER LAND, IN CONTINENTAL MARGINS
*      AND OVER OCEANIC SEAMOUNTS, RIDGES AND TRENCHES) OF SEVERAL
*      DEGREES MAY BE EXPECTED. ALSO NOT INCLUDED IN THE MODEL ARE
*      NONSECULAR TEMPORAL FLUCTUATIONS OF THE GEOMAGNETIC FIELD
*      OF MAGNETOSPHERIC AND IONOSPHERIC ORIGIN.  DURING MAGNETIC
*      STORMS, TEMPORAL FLUCTUATIONS CAN CAUSE SUBSTANTIAL
*      DEVIATIONS OF THE GEOMAGNETIC FIELD FROM MODEL VALUES.  IN
*      ARCTIC AND ANTARCTIC REGIONS, AS WELL AS IN EQUATORIAL
*      REGIONS, DEVIATIONS FROM MODEL VALUES ARE BOTH FREQUENT AND
*      PERSISTENT.
*      </code></p>
*
*  <p> For more information, see:
*      <a href="http://www.ngdc.noaa.gov/seg/geomag/models.shtml">
*      <code>http://www.ngdc.noaa.gov/seg/geomag/models.shtml</code></a>.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  August 8, 2004
*  @version   January 6, 2005
**/
public class GeoMagJ {

	//  Debug flag.
	private static final boolean DEBUG = false;
	
	//  -------------------------INPUTS--------------------------
	//  The geodetic latitude in degrees.
	private double glat;
	
	//  The geodetic longitude in degrees.
	private double glon;
	
	//  The geodetic altitude in kilometers.
	private double alt;
	
	//  The time in decimal years (e.g. 1 July 2000 = 2000.500).
	private double time;
	
	//  Indicates if the magnetic model needs to be computed or not.
	private boolean needsUpdate = true;
	
	//  -------------------------OUTPUTS--------------------------
	private double bx;		//  NORTH GEOMAGNETIC COMPONENT (nT)
	private double by;		//  EAST GEOMAGNETIC COMPONENT (nT)
	private double bz;		//  VERTICALLY DOWN GEOMAGNETIC COMPONENT (nT)
	private double bh;		//  HORIZONTAL GEOMAGNETIC COMPONENT (nT)
	private double ti;		//  GEOMAGNETIC TOTAL INTENSITY (nT)
	private double dec;		//  GEOMAGNETIC DECLINATION (DEG.), +=to East
	private double dip;		//  GEOMAGNETIC INCLINATION (DEG.), +=down
	private double gv;		//  GEOMAGNETIC GRID VARIATION (DEG.), referenced to grid north.
	
	
	//  -------------------------CONSTANTS--------------------------
	private static final int maxord = 12;			//  MAXIMUM ORDER OF SPHERICAL HARMONIC MODEL
	private static final double a = 6378.137;		//  SEMIMAJOR AXIS OF WGS-84 ELLIPSOID (KM)
	private static final double b = 6356.7523142;   //  SEMIMINOR AXIS OF WGS-84 ELLIPSOID (KM)
	private static final double re = 6371.2;		//  MEAN RADIUS OF IAU-66 ELLIPSOID (KM)
	private static final double a2 = a*a;
	private static final double b2 = b*b;
	private static final double c2 = a2 - b2;
	private static final double a4 = a2*a2;
	private static final double b4 = b2*b2;
	private static final double c4 = a4 - b4;
	private static final double DTR = Math.PI/180.; //  DEGREE TO RADIAN CONVERSION
	
	//  -------------------------VARIABLES--------------------------
	//  The epoch or base time of the model in years (read from the WMM coefficients file).
	private double epoch;
	
	//  Description of the model being used (read from the WMM coefficients file).
	private String model;
	
	//  GEODETIC LONGITUDE ON PREVIOUS CALL TO GEOMAG (DEG.)
	private double olon = -1000;
	
	//  GEODETIC ALTITUDE ON PREVIOUS CALL TO GEOMAG (YRS)
	private double oalt = -1000;
	
	//  GEODETIC LATITUDE ON PREVIOUS CALL TO GEOMAG (DEG.)
	private double olat = -1000;
	
	//  TIME ON PREVIOUS CALL TO GEOMAG (YRS)
	private double otime = -1000;
	
	//  Results of conversion from geodetic to spherical coordinates.
	private double st;								//  SINE OF (SPHERICAL COORD. LATITUDE)
	private double ct;								//  COSINE OF (SPHERICAL COORD. LATITUDE)
	private double r;								//  SPHERICAL COORDINATE RADIAL POSITION (KM)
	private double ca;								//  COSINE OF SPHERICAL TO GEODETIC VECTOR ROTATION ANGLE
	private double sa;								//  SINE OF SPHERICAL TO GEODETIC VECTOR ROTATION ANGLE
	
	private double[][] c = new double[13][13];		//  GAUSS COEFFICIENTS OF MAIN GEOMAGNETIC MODEL (nT)
	private double[][] cd = new double[13][13];		//  GAUSS COEFFICIENTS OF SECULAR GEOMAGNETIC MODEL (nT/YR)
	private double[][] tc = new double[13][13];		//  TIME ADJUSTED GEOMAGNETIC GAUSS COEFFICIENTS (nT)
	private double[][] dp = new double[13][13];		//  THETA DERIVATIVE OF P(N,M) (UNNORMALIZED)
	private double[][] snorm = new double[13][13];  //  SCHMIDT NORMALIZATION FACTORS
	private double[] sp = new double[13];			//  SINE OF (M*SPHERICAL COORD. LONGITUDE)
	private double[] cp = new double[13];			//  COSINE OF (M*SPHERICAL COORD. LONGITUDE)
	private double[] fn = new double[13];
	private double[] fm = new double[13];
	private double[] pp = new double[13];			//  ASSOCIATED LEGENDRE POLYNOMIALS FOR M=1 (UNNORMALIZED)
	private double[][] k = new double[13][13];
	private double[][] p = snorm;					//  ASSOCIATED LEGENDRE POLYNOMIALS (UNNORMALIZED)

	private static GeoMagJ instance;
	static {
		try {
			instance = new GeoMagJ();
			Calendar c = Calendar.getInstance();
			instance.setTime(MathHelper.clamp((((double)c.get(Calendar.YEAR)) + (((double)c.get(Calendar.DAY_OF_YEAR))/366.0)), instance.getMinTime(), instance.getMaxTime()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public GeoMagJ() throws NumberFormatException, IOException {
		this(GeoMagJ.class.getResourceAsStream("/br/skylight/uav/plugins/control/instruments/WMM.COF"));
	}

	/**
	*  Construct a GeoMagJ object that reads magnetic model spherical harmonic coefficients
	*  from the specified WMM coefficients file input stream.
	**/
	public GeoMagJ(InputStream coefInput) throws IOException, NumberFormatException {
		//  Initialize constants.
		cp[0] = p[0][0] = pp[0] = 1.0;
		
		/* READ WORLD MAGNETIC MODEL SPHERICAL HARMONIC COEFFICIENTS */
		BufferedReader reader = new BufferedReader(new InputStreamReader(coefInput));
		
		//  Create a stream tokenizer using this reader.
		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.eolIsSignificant(false);
		
		//  Get the epoch.
		int ttype = tokenizer.nextToken();
		if (ttype != StreamTokenizer.TT_NUMBER)
			throw new IOException("Could not find the epoch on line #1.");
		epoch = tokenizer.nval;
		
		//  Get the model name.
		ttype = tokenizer.nextToken();
		if (ttype != StreamTokenizer.TT_WORD)
			throw new IOException("Could not find the model name on line #1.");
		model = tokenizer.sval;
		
		if (DEBUG)
			System.out.println("epoch = " + epoch + ", model = " + model);
		
		//  Skip the date.
		ttype = tokenizer.nextToken();

		//  Begin reading in the main part of the file.
		ttype = tokenizer.nextToken();
		while (ttype != StreamTokenizer.TT_EOF) {
		
			//  Read in Parameters.
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			if (tokenizer.nval >= 9999.)	break;
			int n = (int)tokenizer.nval;
			
			ttype = tokenizer.nextToken();
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			int m = (int)tokenizer.nval;
			
			ttype = tokenizer.nextToken();
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			double gnm = tokenizer.nval;
			
			ttype = tokenizer.nextToken();
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			double hnm = tokenizer.nval;
			
			ttype = tokenizer.nextToken();
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			double dgnm = tokenizer.nval;
			
			ttype = tokenizer.nextToken();
			if (ttype != StreamTokenizer.TT_NUMBER)
				throw new IOException("Expected a number but didn't get it on line #" + tokenizer.lineno() + ".");
			double dhnm = tokenizer.nval;
			
			if (DEBUG) {
				System.out.print("    n = " + n + ", m = " + m + ", gnm = " + (float)(gnm) + ", hnm = " + (float)(hnm));
				System.out.println(", dgnm = " + (float)(dgnm) + ", dhnm = " + (float)(dhnm) );
			}
			
			if (m <= n) {
				c[m][n] = gnm;
				cd[m][n] = dgnm;
				
				if (m != 0) {
					c[n][m-1] = hnm;
					cd[n][m-1] = dhnm;
				}
			}
			
			ttype = tokenizer.nextToken();
		}   //  end while()
				
		//  S4:
		/* CONVERT SCHMIDT NORMALIZED GAUSS COEFFICIENTS TO UNNORMALIZED */
		snorm[0][0] = 1.0;
		for (int n=1; n <= maxord; ++n) {
			snorm[0][n] = snorm[0][n-1]*(double)(2*n - 1)/(double)n;
			int j = 2;
			for (int m=0; m <= n; ++m) {
				k[m][n] = (double)(((n-1)*(n-1))-(m*m))/(double)((2*n-1)*(2*n-3));
				if (m > 0) {
					double flnmj = (double)((n-m+1)*j)/(double)(n+m);
					snorm[m][n] = snorm[m-1][n]*Math.sqrt(flnmj);
					j = 1;
					c[n][m-1] = snorm[m][n]*c[n][m-1];
					cd[n][m-1] = snorm[m][n]*cd[n][m-1];
				}
				c[m][n] = snorm[m][n]*c[m][n];
				cd[m][n] = snorm[m][n]*cd[m][n];
			}
			fn[n] = (double)(n + 1);
			fm[n] = (double)n;
		}
		k[1][1] = 0.0;
		
	}

	/**
	 * Returns current declination at location
	 * Latitude/longitude in degrees
	 * Altitude in meters above WGS-84
	 */
	public static double getCurrentDeclination(double latitude, double longitude, double altitude) {
		instance.setLatitude(latitude);
		instance.setLongitude(longitude);
		instance.setAltitude(altitude);
		return instance.getDeclination();
	}
		
	/**
	*  Sets the geodetic latitude in degrees for evaluating the model.
	*  Latitude is valid for -90 to +90 deg.  No range check is made.
	**/
	public void setLatitude(double latitude) {
		glat = latitude;
		needsUpdate = true;
	}
	
	/**
	*  Set the geodetic longitude in degrees for evaluating the model.
	*  Longitude is valid for -180 to +180 deg.  No range check is made.
	**/
	public void setLongitude(double longitude) {
		glon = longitude;
		needsUpdate = true;
	}
	
	/**
	*  Set the geodetic altitude in meters for evaluating the model.
	*  The altitude is  referenced to the  World Geodetic System 1984 (WGS 84) ellipsoid.
	*  Altitude is valid for 0 (Sea Level) to 1,000,000 meters.  No range check is made.
	**/
	public void setAltitude(double altitude) {
		alt = altitude/1000.;		//  Convert from meters to kilometers.
		needsUpdate = true;
	}
	
	/**
	*  Set the time in decimal years for evaluating the model
	*  (e.g. 1 July 2000 = 2000.500).
	**/
	public void setTime(double time) {
		this.time = time;
		needsUpdate = true;
	}
	
	/**
	*  Get the earliest time when this model is valid in decimal years
	*  (the beginning of the epoch for this model).  As geomagnetic
	*  model data is only reliable for a few years from the epoch date
	*  of the model, computing data for a date that exceeds the life
	*  of the model may produce inaccurate results.
	**/
	public double getMinTime() {
		return epoch;
	}
	
	/**
	*  Get the latest time when this model is valid in decimal years
	*  (the end of the epoch for this model).  As geomagnetic
	*  model data is only reliable for a few years from the epoch date
	*  of the model, computing data for a date that exceeds the life
	*  of the model may produce inaccurate results.
	**/
	public double getMaxTime() {
		return epoch + 5.;
	}
	
	/**
	*  Calculate and return the total intensity of the geomagnetic field in nanoTeslas.
	**/
	public double getTotalIntensity() {
		if (needsUpdate)	geomag1();
		return ti;
	}
	
	/**
	*  Calculate and return the horizontal intensity of the geomagnetic field in nanoTeslas.
	**/
	public double getHorizontalIntensity() {
		if (needsUpdate)	geomag1();
		return bh;
	}
	
	/**
	*  Calculate and return the North component of the geomagnetic field in nanoTeslas.
	**/
	public double getNorthComponent() {
		if (needsUpdate)	geomag1();
		return bx;
	}
	
	/**
	*  Calculate and return the East component of the geomagnetic field in nanoTeslas.
	**/
	public double getEastComponent() {
		if (needsUpdate)	geomag1();
		return by;
	}
	
	/**
	*  Calculate and return the Vertical component of the geomagnetic field in nanoTeslas.
	**/
	public double getVerticalComponent() {
		if (needsUpdate)	geomag1();
		return bz;
	}
	
	/**
	*  Calculate and return the geomagnetic inclination, positive angles are down.
	**/
	public double getInclination() {
		if (needsUpdate)	geomag1();
		return dip;
	}
	
	/**
	*  Calculate and return the geomagnetic declination, positive angles are to east.
	**/
	public double getDeclination() {
		if (needsUpdate)	geomag1();
		return dec;
	}
	
	
	/**
	*  Method that actually calculates the geomagnetic model properties.
	**/
	private void geomag1() {
		//  Clear the needsUpdate flag.
		needsUpdate = false;
		
		double dt = time - epoch;
		double rlat = glat*DTR;
		double rlon = glon*DTR;
		double srlon = Math.sin(rlon);
		double crlon = Math.cos(rlon);
		double srlat = Math.sin(rlat);
		double srlat2 = srlat*srlat;
		double crlat = Math.cos(rlat);
		double crlat2 = crlat*crlat;
		sp[1] = srlon;
		cp[1] = crlon;
		
		/* CONVERT FROM GEODETIC COORDS. TO SPHERICAL COORDS. */
		if (alt != oalt || glat != olat) {
			double q = Math.sqrt(a2-c2*srlat2);
			double q1 = alt*q;
			double tmp = (q1+a2)/(q1+b2);
			double q2 = tmp*tmp;
			ct = srlat/Math.sqrt(q2*crlat2+srlat2);
			st = Math.sqrt(1.0-(ct*ct));
			double r2 = (alt*alt)+2.0*q1+(a4-c4*srlat2)/(q*q);
			r = Math.sqrt(r2);
			double d = Math.sqrt(a2*crlat2+b2*srlat2);
			ca = (alt+d)/r;
			sa = c2*crlat*srlat/(r*d);
		}
		
		if (glon != olon) {
			for (int m=2; m <= maxord; m++) {
				sp[m] = sp[1]*cp[m-1]+cp[1]*sp[m-1];
				cp[m] = cp[1]*cp[m-1]-sp[1]*sp[m-1];
			}
		}
		
		double aor = re/r;
		double ar = aor*aor;
		double br=0;			//  RADIAL COMPONENT OF GEOMAGNETIC FIELD (NT)
		double bt=0;			//  THETA COMPONENT OF GEOMAGNETIC FIELD (NT)
		double bp=0;			//  PHI COMPONENT OF GEOMAGNETIC FIELD (NT)
		double bpp = 0;
		for (int n=1; n <= maxord; n++) {
			ar = ar*aor;
			for (int m=0; m <= n; ++m) {
				/*
				   COMPUTE UNNORMALIZED ASSOCIATED LEGENDRE POLYNOMIALS
				   AND DERIVATIVES VIA RECURSION RELATIONS
				*/
				if (alt != oalt || glat != olat) {
					if (n == m) {
						p[m][n] = st*p[m-1][n-1];
						dp[m][n] = st*dp[m-1][n-1]+ct*p[m-1][n-1];
						//  goto S50;
						
					} else if (n == 1 && m == 0) {
						p[m][n] = ct*p[m][n-1];
						dp[m][n] = ct*dp[m][n-1]-st*p[m][n-1];
						//  goto S50;
						
					} else if (n > 1 && n != m) {
						if (m > n-2) {
							p[m][n-2] = 0.0;
							dp[m][n-2] = 0.0;
						}
						p[m][n] = ct*p[m][n-1] - k[m][n]*p[m][n-2];
						dp[m][n] = ct*dp[m][n-1] - st*p[m][n-1]-k[m][n]*dp[m][n-2];
					}
				}
				//  S50:
				
				/*
					TIME ADJUST THE GAUSS COEFFICIENTS
				*/
				if (time != otime) {
					tc[m][n] = c[m][n]+dt*cd[m][n];
					if (m != 0) tc[n][m-1] = c[n][m-1]+dt*cd[n][m-1];
				}
				
				/*
					ACCUMULATE TERMS OF THE SPHERICAL HARMONIC EXPANSIONS
				*/
				double par = ar*p[m][n];
				double temp1=0,temp2=0;
				if (m == 0) {
					temp1 = tc[m][n]*cp[m];
					temp2 = tc[m][n]*sp[m];
					
				} else {
					temp1 = tc[m][n]*cp[m]+tc[n][m-1]*sp[m];
					temp2 = tc[m][n]*sp[m]-tc[n][m-1]*cp[m];
				}
				bt -= ar*temp1*dp[m][n];
				bp += fm[m]*temp2*par;
				br += fn[n]*temp1*par;
				
				/*
					SPECIAL CASE:  NORTH/SOUTH GEOGRAPHIC POLES
				*/
				if (st == 0.0 && m == 1) {
					if (n == 1) pp[n] = pp[n-1];
					else pp[n] = ct*pp[n-1]-k[m][n]*pp[n-2];
					double parp = ar*pp[n];
					bpp += fm[m]*temp2*parp;
				}
			}
		}
		
		if (st == 0.0) bp = bpp;
		else bp /= st;
		
		/*
			ROTATE MAGNETIC VECTOR COMPONENTS FROM SPHERICAL TO
			GEODETIC COORDINATES
		*/
		bx = -bt*ca-br*sa;
		by = bp;
		bz = bt*sa-br*ca;
		
		/*
			COMPUTE DECLINATION (DEC), INCLINATION (DIP) AND
			TOTAL INTENSITY (TI)
		*/
		bh = Math.sqrt((bx*bx)+(by*by));
		ti = Math.sqrt((bh*bh)+(bz*bz));
		dec = Math.atan2(by,bx)/DTR;
		dip = Math.atan2(bz,bh)/DTR;

		/*
			COMPUTE MAGNETIC GRID VARIATION IF THE CURRENT
			GEODETIC POSITION IS IN THE ARCTIC OR ANTARCTIC
			(I.E. GLAT > +55 DEGREES OR GLAT < -55 DEGREES)

			OTHERWISE, SET MAGNETIC GRID VARIATION TO -999.0
		*/
		gv = -999.0;
		if (Math.abs(glat) >= 55.) {
			if (glat > 0.0 && glon >= 0.0) gv = dec-glon;
			else if (glat > 0.0 && glon < 0.0) gv = dec+Math.abs(glon);
			else if (glat < 0.0 && glon >= 0.0) gv = dec+glon;
			else if (glat < 0.0 && glon < 0.0) gv = dec-Math.abs(glon);
			
			if (gv > +180.0) gv -= 360.0;
			if (gv < -180.0) gv += 360.0;
		}
		
		//  Save off old values.
		otime = time;
		oalt = alt;
		olon = glon;
		olat = glat;
	}
	
	
	/**
	*  Main method used to test this class.
	 * @throws IOException 
	 * @throws NumberFormatException 
	**/
	public static void main(String[] args) throws NumberFormatException, IOException {
		System.out.println("Creating GeoMagJ object using WWM.COF file...");
		
		GeoMagJ test = new GeoMagJ();
		
		System.out.println("Computing model...");
		double glat = 40.0;
		double glon = 240.0;
		double alt = 0.0;
		double time = 2007.5;
		System.out.println();
		System.out.println("Inputs:");
		System.out.println("   LATITUDE:\t" + glat + " deg");
		System.out.println("   LONGITUDE:\t" + glon + " deg");
		System.out.println("   ALTITUDE:\t" + alt + " meters");
		System.out.println("   DATE:\t" + time + " year");
		
		test.setLatitude(glat);
		test.setLongitude(glon);
		test.setAltitude(alt);
		test.setTime(time);
		double ti = test.getTotalIntensity();
		
		System.out.println();
		System.out.println("Outputs:");
		System.out.println("   MAIN FIELD COMPONENTS");
		System.out.println("   ---------------------");
		System.out.println("   TI \t= " + Math.round(ti) + " nT,\t\tshould be 51025 nT");
		System.out.println("   HI \t= " + Math.round(test.getHorizontalIntensity()) + " nT,\t\tshould be 22591 nT");
		System.out.println("   X \t= " + Math.round(test.getNorthComponent()) + " nT,\t\tshould be 21846 nT");
		System.out.println("   Y \t= " + Math.round(test.getEastComponent()) + " nT,\t\tshould be 5753 nT");
		System.out.println("   Z \t= " + Math.round(test.getVerticalComponent()) + " nT,\t\tshould be 45752 nT");
		System.out.println("   DEC \t= " + (float)test.getDeclination() + " deg,\tshould be 14.75 deg");
		System.out.println("   DIP \t= " + (float)test.getInclination() + " deg,\t\tshould be 63.72 deg");
			
	}
}
