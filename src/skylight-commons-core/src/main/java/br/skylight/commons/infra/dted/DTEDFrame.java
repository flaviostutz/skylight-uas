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
// $Source: /cvs/distapps/openmap/com/bbn/openmap/layer/dted/DTEDFrame.java,v $
// $RCSfile: DTEDFrame.java,v $
// $Revision: 1.32 $
// $Date: 2002/04/25 20:18:04 $
// $Author: dietrick $
// 
// **********************************************************************

package br.skylight.commons.infra.dted;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The DTEDFrame is the representation of the DTED (Digital Terrain Elevation
 * Data) data from a single dted data file. It keeps track of all the attribute
 * information of it's data, and also maintains an array of images
 * (DTEDFrameSubframe) that represent views of the elevation posts.
 */
public class DTEDFrame implements Closeable {

	private static final Logger logger = Logger.getLogger(DTEDFrame.class.getName());

	public final static int UHL_SIZE = 80;
	public final static int DSI_SIZE = 648;
	public final static int ACC_SIZE = 2700;
	public final static int ACC_SR_SIZE = 284;
	/** The binary buffered file to read the data from the file. */
	protected BinaryFile binFile;
	/** The path to the frame, including the frame name. */
	protected String path;
	/**
	 * The array of elevation posts. Note: the 0 index of the array in both
	 * directions is in the lower left corner of the matrix. As you increase
	 * indexes in both dimensions, you go up-right.
	 */
	protected short[][] elevations; // elevation posts

	/** Data set indentification section of the file. */
	public DTEDFrameDSI dsi;
	/** User header label section of the file. */
	public DTEDFrameUHL uhl;
	/** Validity flag for the quality of the data file. */
	public boolean frame_is_valid = false;

	/**
	 * The frame image is divided into 200x200 pixel subframes, with a leftover
	 * frame at the end. This is how many horizontal subframes there are.
	 */
	public int number_horiz_subframes;
	/**
	 * The frame image is divided into 200x200 pixel subframes, with a leftover
	 * frame at the end. This is how many vertical subframes there are.
	 */
	public int number_vert_subframes;

	// ////////////////
	// Administrative methods
	// ////////////////

	/**
	 * Constructor with colortable and presentation information.
	 * 
	 * @param filePath
	 *            complete path to the DTED frame.
	 * @param cTable
	 *            the colortable to use for the images.
	 * @param info
	 *            presentation parameters.
	 * @param readWholeFile
	 *            If true, all of the elevation data will be read at load time.
	 *            If false, elevation post data will be read in per longitude
	 *            column depending on the need. False is recommended for DTED
	 *            level 1 and 2.
	 */
	public DTEDFrame(String filePath, boolean readWholeFile) {

		try {
			binFile = new BinaryBufferedFile(filePath);

			read(binFile, readWholeFile);
			if (readWholeFile)
				close(true);
			else
				binFile.addClosable(this);

		} catch (FileNotFoundException e) {
			logger.severe("DTEDFrame: file " + filePath + " not found");
		} catch (java.io.IOException e) {
			logger.severe("DTEDFrame: File IO Error!\n" + e.toString());
		}

		path = filePath;
	}

	/**
	 * Reads the DTED frame file. Assumes that the File f is valid/exists.
	 * 
	 * @param binFile
	 *            the binary buffere file opened on the DTED frame file
	 * @param readWholeFile
	 *            flag controlling whether all the row data is read at this
	 *            time. Otherwise, the rows are read as needed.
	 */
	protected void read(BinaryFile binFile, boolean readWholeFile) {
		binFile.byteOrder(true); // boolean msbfirst
		dsi = new DTEDFrameDSI(binFile);
		uhl = new DTEDFrameUHL(binFile);
		// Allocate just the columns now - we'll do the rows as needed...
		elevations = new short[uhl.num_lon_lines][];
		if (readWholeFile)
			read_data_records();
		frame_is_valid = true;
	}

	/**
	 * This must get called to break a reference cycle that prevents the garbage
	 * collection of frames.
	 */
	public void dispose() {
		// System.out.println("DTED Frame Disposed " + me);
		BinaryFile.removeClosable(this);
	}

	// public void finalize() {
	// System.out.println("DTED Frame Finalized!" + me);
	// }

	/**
	 * Part of the Closable interface. Closes the BinaryFile pointer, because
	 * someone else needs another file open, and the system needs a file
	 * pointer. Sets the binFile variable to null.
	 */
	public boolean close(boolean done) {
		try {
			binFile.close();
			binFile = null;
			return true;
		} catch (java.io.IOException e) {
			logger.severe("DTEDFrame close(): File IO Error!\n" + e.toString());
			return false;
		}
	}

	/**
	 * If the BinaryBufferedFile was closed, this method attempts to reopen it.
	 * 
	 * @return true if the opening was successful.
	 */
	protected boolean reopen() {
		try {
			binFile = new BinaryBufferedFile(path);
			// binFile = new BinaryFile(path);
			return true;
		} catch (FileNotFoundException e) {
			logger.severe("DTEDFrame reopen(): file " + path + " not found");
			return false;
		} catch (java.io.IOException e) {
			logger.severe("DTEDFrame close(): File IO Error!\n" + e.toString());
			return false;
		}
	}

	// ////////////////
	// These functions can be called from the outside,
	// as queries about the data
	// ////////////////

	/**
	 * The elevation at the closest SW post to the given lat/lon. This is just a
	 * go-to-the-closest-post solution.
	 * 
	 * @param lat
	 *            latitude in decimal degrees.
	 * @param lon
	 *            longitude in decimal degrees.
	 * @return elevation at lat/lon in meters.
	 */
	public int elevationAt(float lat, float lon) {
		if (frame_is_valid == true) {
			if (lat >= dsi.sw_lat && lat <= dsi.ne_lat && lon >= dsi.sw_lon && lon <= dsi.ne_lon) {

				// lat/lon_post_intervals are *10 too big -
				// extra 0 in 36000 to counteract
				int lat_index = Math.round((lat - dsi.sw_lat) * 36000 / uhl.lat_post_interval);
				int lon_index = Math.round((lon - dsi.sw_lon) * 36000 / uhl.lon_post_interval);

				if (elevations[lon_index] == null)
					read_data_record(lon_index);

				return (int) elevations[lon_index][lat_index];
			}
		}
		return -32767; // Considered a null elevation value
	}

	/**
	 * Interpolated elevation at a given lat/lon - should be more precise than
	 * elevationAt(), but that depends on the resolution of the data.
	 * 
	 * @param lat
	 *            latitude in decimal degrees.
	 * @param lon
	 *            longitude in decimal degrees.
	 * @return elevation at lat/lon in meters.
	 */
	public int interpElevationAt(float lat, float lon) {
		if (frame_is_valid == true) {
			if (lat >= dsi.sw_lat && lat <= dsi.ne_lat && lon >= dsi.sw_lon && lon <= dsi.ne_lon) {

				// lat/lon_post_intervals are *10 too big -
				// extra 0 in 36000 to counteract
				float lat_index = (lat - dsi.sw_lat) * 36000F / uhl.lat_post_interval;
				float lon_index = (lon - dsi.sw_lon) * 36000F / uhl.lon_post_interval;

				int lflon_index = (int) Math.floor(lon_index);
				int lclon_index = (int) Math.ceil(lon_index);
				int lflat_index = (int) Math.floor(lat_index);
				int lclat_index = (int) Math.ceil(lat_index);

				if (elevations[lflon_index] == null)
					read_data_record(lflon_index);
				if (elevations[lclon_index] == null)
					read_data_record(lclon_index);

				// ////////////////////////////////////////////////////
				// Print out grid of 20x20 elevations with
				// the "asked for" point being in the middle
				// System.out.println("***Elevation Map***");
				// for(int l = lclat_index + 5; l > lflat_index - 5; l--) {
				// System.out.println();
				// for(int k = lflon_index - 5; k < lclon_index + 5; k++) {
				// if (elevations[k]==null) read_data_record(k);
				// System.out.print(elevations[k][l] + " ");
				// }
				// }
				// System.out.println();System.out.println();
				// ////////////////////////////////////////////////////

				int ul = elevations[lflon_index][lclat_index];
				int ur = elevations[lclon_index][lclat_index];
				int ll = elevations[lflon_index][lclat_index];
				int lr = elevations[lclon_index][lclat_index];

				float answer = resolve_four_points(ul, ur, lr, ll, lat_index, lon_index);
				return Math.round(answer);
			}
		}
		return -32767; // Considered a null elevation value
	}

	/**
	 * Return an index of ints representing the starting x, y and ending x, y of
	 * elevation posts given a lat lon box. It does check to make sure that the
	 * upper lat is larger than the lower, and left lon is less than the right.
	 * 
	 * @param ullat
	 *            upper latitude in decimal degrees.
	 * @param ullon
	 *            left longitude in decimal degrees.
	 * @param lrlat
	 *            lower latitude in decimal degrees.
	 * @param lrlon
	 *            right longitude in decimal degrees.
	 * @return int[4] array of start x, start y, end x, and end y.
	 */
	public int[] getIndexesFromLatLons(float ullat, float ullon, float lrlat, float lrlon) {
		float upper = ullat;
		float lower = lrlat;
		float right = lrlon;
		float left = ullon;

		// Since matrix indexes depend on these being in the right
		// order, we'll double check and flip values, just to make
		// sure lower is lower, and higher is higher.
		if (ullon > lrlon) {
			right = ullon;
			left = lrlon;
		}

		if (lrlat > ullat) {
			upper = lrlat;
			lower = ullat;
		}

		int[] ret = new int[4];
		float ullat_index = (upper - dsi.sw_lat) * 36000F / uhl.lat_post_interval;
		float ullon_index = (left - dsi.sw_lon) * 36000F / uhl.lon_post_interval;
		float lrlat_index = (lower - dsi.sw_lat) * 36000F / uhl.lat_post_interval;
		float lrlon_index = (right - dsi.sw_lon) * 36000F / uhl.lon_post_interval;

		ret[0] = (int) Math.round(ullon_index);
		ret[1] = (int) Math.round(lrlat_index);
		ret[2] = (int) Math.round(lrlon_index);
		ret[3] = (int) Math.round(ullat_index);

		if (ret[0] < 0)
			ret[0] = 0;
		if (ret[0] > uhl.num_lon_lines - 2)
			ret[0] = uhl.num_lon_lines - 2;
		if (ret[1] < 0)
			ret[1] = 0;
		if (ret[1] > uhl.num_lat_points - 2)
			ret[1] = uhl.num_lat_points - 2;
		if (ret[2] < 0)
			ret[2] = 0;
		if (ret[2] > uhl.num_lon_lines - 2)
			ret[2] = uhl.num_lon_lines - 2;
		if (ret[3] < 0)
			ret[3] = 0;
		if (ret[3] > uhl.num_lat_points - 2)
			ret[3] = uhl.num_lat_points - 2;
		return ret;

	}

	/**
	 * Return a two dimensional array of posts between lat lons.
	 * 
	 * @param ullat
	 *            upper latitude in decimal degrees.
	 * @param ullon
	 *            left longitude in decimal degrees.
	 * @param lrlat
	 *            lower latitude in decimal degrees.
	 * @param lrlon
	 *            right longitude in decimal degrees.
	 * @return array of elevations in meters. The spacing of the posts depends
	 *         on the DTED level.
	 */
	public short[][] getElevations(float ullat, float ullon, float lrlat, float lrlon) {
		int[] indexes = getIndexesFromLatLons(ullat, ullon, lrlat, lrlon);
		return getElevations(indexes[0], indexes[1], indexes[2], indexes[3]);
	}

	/**
	 * Return a two dimensional array of posts between lat lons. Assumes that
	 * the indexes are checked to not exceed their bounds as defined in the
	 * file. getIndexesFromLatLons() checks this.
	 * 
	 * @param startx
	 *            starting index (left) of the greater matrix to make the left
	 *            side of the returned matrix.
	 * @param starty
	 *            starting index (lower) of the greater matrix to make the
	 *            bottom side of the returned matrix.
	 * @param endx
	 *            ending index (right) of the greater matrix to make the left
	 *            side of the returned matrix.
	 * @param endy
	 *            ending index (top) of the greater matrix to make the top side
	 *            of the returned matrix.
	 * @return array of elevations in meters. The spacing of the posts depends
	 *         on the DTED level.
	 */
	public short[][] getElevations(int startx, int starty, int endx, int endy) {
		int upper = endy;
		int lower = starty;
		int right = endx;
		int left = startx;

		// Since matrix indexes depend on these being in the right
		// order, we'll double check and flip values, just to make
		// sure lower is lower, and higher is higher.
		if (startx > endx) {
			right = startx;
			left = endx;
		}

		if (starty > endy) {
			upper = starty;
			lower = endy;
		}

		short[][] matrix = new short[right - left + 1][upper - lower + 1];
		int matrixColumn = 0;
		for (int x = left; x <= right; x++) {
			if (elevations[x] == null)
				read_data_record(x);
			System.arraycopy(elevations[x], lower, matrix[matrixColumn], 0, (upper - lower + 1));
			matrixColumn++;
		}
		return matrix;
	}

	// ////////////////
	// Internal methods
	// ////////////////

	/**
	 * A try at interoplating the corners of the surrounding posts, given a lat
	 * lon. Called from a function where the data for the lon has been read in.
	 */
	private float resolve_four_points(int ul, int ur, int lr, int ll, float lat_index, float lon_index) {
		float top_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (float) (ur - ul)) + ul;
		float bottom_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (float) (lr - ll)) + ll;
		float right_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (float) (ur - lr)) + lr;
		float left_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (float) (ul - ll)) / 100.0F + ll;

		float lon_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (top_avg - bottom_avg)) + bottom_avg;
		float lat_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (right_avg - left_avg)) + left_avg;

		float result = (lon_avg + lat_avg) / 2.0F;
		return result;
	}

	/**
	 * Reads one longitude line of posts. Assumes that the binFile is valid.
	 * 
	 * @return true if the column of data was successfully read
	 */
	protected boolean read_data_record(int lon_index) {
		try {
			if (binFile == null)
				if (!reopen())
					return false;

			// Set to beginning of file section, then skip to index data
			// 12 = 1+3+2+2+4 = counts and checksum
			// 2*uhl....size of elevation post space
			binFile.seek(UHL_SIZE + DSI_SIZE + ACC_SIZE + (lon_index * (12 + (2 * uhl.num_lat_points))));
			int sent = binFile.read();
			binFile.skipBytes(3); // 3 byte data_block_count
			short lon_count = binFile.readShort();
			short lat_count = binFile.readShort();
			// Allocate the rows of the row
			elevations[lon_index] = new short[uhl.num_lat_points];
			for (int j = 0; j < uhl.num_lat_points; j++)
				elevations[lon_index][j] = binFile.readShort();

		} catch (IOException e3) {
			logger.severe("DTEDFrame.RDR: Error reading file.");
			e3.printStackTrace();
			elevations[lon_index] = null;
			return false;
		} catch (FormatException f) {
			logger.severe("DTEDFrame.RDR: File IO Format error!");
			elevations[lon_index] = null;
			return false;
		}
		return true;
	}

	/**
	 * Read all the elevation posts, at one time. Assumes that the file is open
	 * and ready.
	 * 
	 * @return true if the elevation columns were read.
	 */
	protected boolean read_data_records() {
		boolean ret = true;
		for (int lon_index = 0; lon_index < uhl.num_lon_lines; lon_index++) {
			if (read_data_record(lon_index) == false)
				ret = false;
		}
		return ret;
	}

}
