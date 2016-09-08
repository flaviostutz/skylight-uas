package br.skylight.cucs.plugins.vehiclecontrol;

import br.skylight.commons.StringHelper;

public class CUCS {

	private int cucsId;
	private String name;
	
	public int getCucsId() {
		return cucsId;
	}
	public void setCucsId(int cucsId) {
		this.cucsId = cucsId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		if(getName()!=null) {
			return getName() + " ("+ StringHelper.formatId(cucsId) +")";
		} else {
			return StringHelper.formatId(cucsId);
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cucsId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CUCS other = (CUCS) obj;
		if (cucsId != other.cucsId)
			return false;
		return true;
	}
	
}
