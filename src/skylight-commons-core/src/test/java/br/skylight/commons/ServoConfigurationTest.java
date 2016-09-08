package br.skylight.commons;

public class ServoConfigurationTest {

	public static void main(String[] args) {
		ServoConfiguration sc = new ServoConfiguration();

		sc.setServo(Servo.AILERON_LEFT);
		sc.setMinUs(1000);
		sc.setMaxUs(2000);
		sc.setRangeAngle(180);
		sc.setTrimUs(0);
		if(sc.getServoTimeForZeroCenteredSetpoint(-127)!=1000) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(-127));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(0)!=1500) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(0));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(127)!=2000) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(127));
		}

		sc.setTrimUs(200);
		if(sc.getServoTimeForZeroCenteredSetpoint(0)!=1600) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(0));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(-127)!=1200) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(-127));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(127)!=2000) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(127));
		}

		sc.setTrimUs(-400);
		if(sc.getServoTimeForZeroCenteredSetpoint(-127)!=1000) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(-127));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(127)!=1600) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(127));
		}
		if(sc.getServoTimeForZeroCenteredSetpoint(0)!=1300) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForZeroCenteredSetpoint(0));
		}

		//SERVO
		sc.setServo(Servo.THROTTLE);
		sc.setTrimUs(100);
		if(sc.getServoTimeForSetpoint(0)!=1100) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForSetpoint(0));
		}
		if(sc.getServoTimeForSetpoint(255)!=2000) {
			throw new AssertionError("Wrong results " + sc.getServoTimeForSetpoint(127));
		}

		System.out.println("All OK");
	}

}
