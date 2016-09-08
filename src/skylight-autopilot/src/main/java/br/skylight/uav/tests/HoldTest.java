package br.skylight.uav.tests;

import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.uav.plugins.control.pids.PIDControllers;

public class HoldTest {

	public static void main(String[] args) throws Exception {
		int step = 0;
		PluginManager pluginManager = PluginManager.getInstance();
		PIDControllers pidControllers = new PIDControllers();
		pluginManager.manageObject(pidControllers);
		pluginManager.startupPlugins();
		
		while (true) {
			if (step == 0) {
				System.out.println("Hold roll with aileron: 20");
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, 20);
			} else if (step == 1) {
				System.out.println("Hold roll with aileron: -20");
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, -20);
			} else if (step == 2) {
				System.out.println("Hold roll with aileron: 0");
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, 0);
			} else if (step == 3) {
				System.out.println("Hold pitch with elevator: 7");
				pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 7);
			} else if (step == 4) {
				System.out.println("Hold pitch with elevator: -7");
				pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, -7);
			} else if (step == 5) {
				System.out.println("Hold pitch with elevator: 0");
				pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, 0);
			} else if (step == 6) {
				System.out.println("Hold ktias with throttle: 40");
				pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, 40);
			} else if (step == 7) {
				System.out.println("Hold altitude with pitch: 400");
				pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, 400);
			} else if (step == 8) {
				System.out.println("Hold altitude with pitch: 0");
				pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, 0);
			} else if (step == 9) {
				System.out.println("Hold heading with roll: 0");
				pidControllers.holdSetpoint(PIDControl.HOLD_COURSE_WITH_ROLL, 0);
			} else if (step == 10) {
//				System.out.println("Hold ktias with pitch: 40");
//				actuatorsService.setThrottle(0);
				pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, 40);
				step = -1;
			}
			Thread.sleep(7000);
			pidControllers.unholdAll();
			step++;
			
			pluginManager.shutdownPlugins();
		}

	}
}
