package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.dli.enums.LoiterDirection;
import br.skylight.commons.dli.enums.LoiterType;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.cucs.plugins.loiterdirector.LoiterMapElement;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class DataLinkRecoveryLoiterMapElement extends LoiterMapElement {

	private RulesOfSafety rulesOfSafety;
	
	public DataLinkRecoveryLoiterMapElement() {
		setSetLabelOnDoubleClick(false);
	}
	
	public void setRulesOfSafety(RulesOfSafety rulesOfSafety) {
		this.rulesOfSafety = rulesOfSafety;
		super.setPosition(CUCSViewHelper.toGeoPosition(rulesOfSafety.getDataLinkRecoveryLoiterLocation()));
	}
	
	@Override
	public void setPosition(GeoPosition position) {
		super.setPosition(position);
		CUCSViewHelper.copyCoordinates(rulesOfSafety.getDataLinkRecoveryLoiterLocation(), position);
	
		LoiterConfiguration lc = getLoiterConfiguration();
		lc.setAltitudeType(rulesOfSafety.getDataLinkRecoveryLoiterAltitudeType());
		lc.setLoiterAltitude(rulesOfSafety.getDataLinkRecoveryLoiterLocation().getAltitude());
		lc.setLoiterBearing(0);
		lc.setLoiterDirection(LoiterDirection.VEHICLE_DEPENDENT);
		lc.setLoiterLength(0);
		//FIXME get from vehicle configuration
		lc.setLoiterRadius(150);
		lc.setLoiterType(LoiterType.CIRCULAR);
	}
	
	@Override
	public void setAltitude(float altitude) {
		super.setAltitude(altitude);
		rulesOfSafety.getDataLinkRecoveryLoiterLocation().setAltitude(altitude);
	}
	
}
