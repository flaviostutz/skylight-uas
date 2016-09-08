// **********************************************************************
//
// <copyright>
//
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/com/bbn/openmap/layer/dted/DTEDFrameUHL.java,v $
// $RCSfile: DTEDFrameUHL.java,v $
// $Revision: 1.8.2.1 $
// $Date: 2002/08/07 22:10:31 $
// $Author: dietrick $
//
// **********************************************************************
package br.skylight.commons.infra.dted;

import java.io.IOException;
import java.util.logging.Logger;

public class DTEDFrameUHL {
	private static final Logger logger = Logger.getLogger(DTEDFrameUHL.class.getName());

	public int abs_vert_acc = -1; // in meters
	public float lat_origin; // lower left, in degrees
	public int lat_post_interval; // in seconds

	// UHL fields in order of appearance - filler has been left out.
	public float lon_origin; // lower left, in degrees
	public int lon_post_interval; // in seconds
	public int num_lat_points;
	public int num_lon_lines;
	public String sec_code;
	public String u_ref;

	public DTEDFrameUHL(BinaryFile binFile) {
		try {
			binFile.seek(0);
			String checkUHL = binFile.readFixedLengthString(3);

			binFile.skipBytes(1);
			lon_origin = DTEDFrameUtil.stringToLon(binFile.readFixedLengthString(8));
			lat_origin = DTEDFrameUtil.stringToLat(binFile.readFixedLengthString(8));
			try {
				lon_post_interval = Integer.parseInt(binFile.readFixedLengthString(4), 10);
			} catch (NumberFormatException pExp) {
				logger.fine("DTEDFrameUHL: lon_post_interval number bad, using 0");
				lon_post_interval = 0;
			}
			try {
				lat_post_interval = Integer.parseInt(binFile.readFixedLengthString(4), 10);
			} catch (NumberFormatException pExp) {
				logger.fine("DTEDFrameUHL: lat_post_interval number bad, using 0");
				lat_post_interval = 0;
			}
			String s_abs_vert_acc = binFile.readFixedLengthString(4);

			try {
				if ((s_abs_vert_acc.indexOf("NA") == -1) && (s_abs_vert_acc.indexOf("N/A") == -1)) {
					abs_vert_acc = Integer.parseInt(s_abs_vert_acc, 10);
				}
			} catch (NumberFormatException pExp) {
				logger.fine("DTEDFrameUHL: abs_vert_acc number bad, using 0");
				abs_vert_acc = 0;
			}

			sec_code = binFile.readFixedLengthString(3);
			u_ref = binFile.readFixedLengthString(12);
			try {
				num_lon_lines = Integer.parseInt(binFile.readFixedLengthString(4), 10);
			} catch (NumberFormatException pExp) {
				logger.fine("DTEDFrameUHL: num_lon_lines number bad, using 0");
				num_lon_lines = 0;
			}
			try {
				num_lat_points = Integer.parseInt(binFile.readFixedLengthString(4), 10);
			} catch (NumberFormatException pExp) {
				logger.fine("DTEDFrameUHL: num_lat_points number bad, using 0");
				num_lat_points = 0;
			}
		} catch (IOException e) {
			logger.severe("DTEDFrameUHL: File IO Error!\n" + e.toString());
		} catch (FormatException f) {
			logger.severe("DTEDFrameUHL: File IO Format error!\n" + f.toString());
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();

		s.append("***UHL***" + "\n");
		s.append("  lon_origin: " + lon_origin + "\n");
		s.append("  lat_origin: " + lat_origin + "\n");
		s.append("  lon_post_interval: " + lon_post_interval + "\n");
		s.append("  lat_post_interval: " + lat_post_interval + "\n");
		s.append("  abs_vert_acc: " + abs_vert_acc + "\n");
		s.append("  sec_code: " + sec_code + "\n");
		s.append("  u_ref: " + u_ref + "\n");
		s.append("  num_lon_lines: " + num_lon_lines + "\n");
		s.append("  num_lat_points: " + num_lat_points + "\n");
		return s.toString();
	}

}
