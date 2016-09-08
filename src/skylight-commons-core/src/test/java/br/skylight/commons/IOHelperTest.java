package br.skylight.commons;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.infra.IOHelper;

@Test
public class IOHelperTest {

	public void testCopyState() throws IOException {
		VehicleConfigurationMessage v1 = new VehicleConfigurationMessage();
		v1.setNumberOfEngines(54);
		VehicleConfigurationMessage v2 = new VehicleConfigurationMessage();
		IOHelper.copyState(v2, v1);
		Assert.assertEquals(v2.getNumberOfEngines(), v1.getNumberOfEngines());
	}
	
	public static void main(String[] args) throws IOException {
		new IOHelperTest().testCopyState();
	}
	
}
