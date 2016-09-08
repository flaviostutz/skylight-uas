package br.skylight.commons.dli;

import java.util.ArrayList;
import java.util.List;

public class BitmappedStation extends Bitmapped {

	private List<Integer> stations = new ArrayList<Integer>();
	private long stationsData =-1;
	
	public BitmappedStation(long ... stationNumber) {
		for (long n : stationNumber) {
			addStation(n);
		}
	}

	@Override
	public void setBit(int pos, boolean value) {
		super.setBit(pos, value);
		stations.clear();
	}
	
	public void addStation(long number) {
		if(number>0) {
			setBit((int)number-1, true);
		}
	}
	
	public List<Integer> getStations() {
//		if(stationsData!=getData()) {//avoid recalculating list when data has not changed
			stations.clear();
			for (int i=0; i<(int)getData(); i++) {
				if(isBit(i)) {
					stations.add(i+1);
					stationsData = getData();
				}
			}
//		}
		return stations;
	}

	public boolean isStation(int key) {
		return isBit(key-1);
	}

	public void setUniqueStationNumber(int uniqueStationNumber) {
		setData(0);
		addStation(uniqueStationNumber);
	}
	
}
