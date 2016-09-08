package br.skylight.cucs.widgets.artificialhorizon;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.SpeedType;

public interface ArtificialHorizonListener {

	public void onTargetAltitudeSet(AltitudeType altitudeType, float value);
	public void onTargetSpeedSet(SpeedType speedType, float value);
	public void onTargetHeadingSet(float value);

}
