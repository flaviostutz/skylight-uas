package br.skylight.commons.infra.dted;

import java.io.File;

public class DTEDLoader {

	private DTEDFrameCache fc;

	/**
	 * Initializes the dted file loader
	 * @param dtedDir directory with dted files organized in subdirs as "w047/s16.dt1" for example
	 * @param maxCacheSize max number of dted files to be cached in memory 
	 */
	public DTEDLoader(File dtedDir, int maxCacheSize) {
		fc = new DTEDFrameCache(new String[]{dtedDir.toString()}, maxCacheSize);
	}
	
	/**
	 * Gets interpolated elevation by searching dted data files.
	 * If location cannot be found in data files, -500 is returned
	 */
	public int getElevation(double latitude, double longitude) {
		//try to get a level 1 data
		DTEDFrame dtedf = fc.get(latitude, longitude, 1);
		if(dtedf!=null) {
			int r = dtedf.interpElevationAt((float)latitude, (float)longitude);
			if(r==-500) {
				//try to get a level 0 data
				r = fc.get(latitude, longitude, 0).interpElevationAt((float)latitude, (float)longitude);
			}
			return r;
		}
		return -500;
	}
	
}
