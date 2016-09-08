package br.skylight.uav.tests;

import br.skylight.uav.plugins.onboardintegration.OnboardActuatorsService;


public class ActuatorsTest {

	public static void main(String[] args) throws Exception {
		OnboardActuatorsService gateway = new OnboardActuatorsService();
		System.out.println("ActuatorsService started");
		
		int throttle = 64;
		int aileron = 0;
		int rudder = 0;
		int elevator = 0;
		
		while (true) {

			System.out.println("Setting all to mid position");
			throttle = 64;
			aileron = 0;
			rudder = 0;
			elevator = 0;					
			gateway.setAileron(aileron);
			gateway.setElevator(elevator);
			gateway.setRudder(rudder);
			gateway.setThrottle(throttle);
			gateway.step();
			Thread.sleep(5000);
			System.out.println("Setting all to full positive.");
			aileron = 127;
			rudder = 127;
			elevator = 127;
			throttle = 127;					
			gateway.setAileron(aileron);
			gateway.setElevator(elevator);
			gateway.setRudder(rudder);
			gateway.setThrottle(throttle);					
			gateway.step();
			Thread.sleep(5000);
			System.out.println("Setting all to full negative.");
			aileron = -127;
			rudder = -127;
			elevator = -127;
			throttle = 0;					
			gateway.setAileron(aileron);
			gateway.setElevator(elevator);
			gateway.setRudder(rudder);
			gateway.setThrottle(throttle);					 
			gateway.step();
			Thread.sleep(5000);
//					
			System.out.println("Sweeping servos.");
			int i = -127;
			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis()-start)<30000){
				if (i > 127){
					i = -127;
				}
				aileron = i;
				rudder = i;
				elevator = i;
				throttle = (i > 0) ? i : 0;						
				gateway.setAileron(aileron);
				gateway.setElevator(elevator);
				gateway.setRudder(rudder);
				gateway.setThrottle(throttle);
				gateway.step();
				i++;
				System.out.println("Val: " + i);
				Thread.sleep(5);
			}
		}
	}
}
					
//					System.out.println("Fetching streams...");
//					OutputStream os = UAVAvionicsProxy.getActuatorsOutputStream();
//					InputStream is = UAVAvionicsProxy.getSensorsInputStream();
//					System.out.println("Calculating time to write to outputstream.");
//					int maxTime = 0;
//					int averageTime = 0;
//					int blocks = 5;
//					int counter = 0;									
//					
//					while (counter < blocks){
//						
//						long start = System.currentTimeMillis();
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.write('$');
//						os.flush();
//						int time = (int) (System.currentTimeMillis() - start);
//						if (time > maxTime)
//							maxTime = time;
//						
//						averageTime += time;
//						counter++;						
//					}
//					
//					System.out.println("Average block time: " + (averageTime/blocks) + " ms. Max: " + maxTime + " ms");
//					
//					Thread.sleep(10000);
//					
//					System.out.println("Calculating time to read from buffer.");
//				
//				
//					counter = 0;
//					averageTime = 0;
//					maxTime = 0;
//					
//					while (counter < 5){
//						
//						
//						long start = System.currentTimeMillis();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						is.read();
//						int time = (int) (System.currentTimeMillis() - start);
//						if (time > maxTime)
//							maxTime = time;
//						
//						averageTime += time;
//						counter++;		
//						System.out.println("Read block " + counter);
//					}
//					
//					System.out.println("Average block time: " + (averageTime/blocks) + " ms. Max: " + maxTime + " ms. Total blocks read: " + counter);
//					
//					Thread.sleep(15000);
					
//					System.out.println("Pitch feedback mode");
//					
////					long end = System.currentTimeMillis() + 20000;
//					while (running){
//									
//						int value = 0; 
//						// update instruments
//						instruments.step();
//						
//						if (instruments.getPitch() > -40 && instruments.getPitch() < 40) 
//							value = (int) ((instruments.getPitch() / 40) * 127);
//						
//						gateway.setAileron(value);
//						
//						// update actuators
//						gateway.step();
//						
////						Thread.sleep(5);
//					}
									
//					System.out.println("Releasing payload.");					 					 
//					gateway.releasePayload();
//					Thread.sleep(5000);
//					System.out.println("Activating parachute.");					 					 					
//					gateway.activateParachute();
					
//					System.out.println("Waiting for gateways to start.");
//										
//					System.out.println("Will check for ready");
//					
//					while (gateway.isReady() == false){}
//					//while (instruments.isReady() == false){}
//					
//					int sentVal = 0;
//					
//					System.out.println("Services ready.");
//					System.out.println("Services ready");
//					
//					System.out.println("Will send something");
//					gateway.setThrottle(sentVal);
//					sentVal++;
//					System.out.println("Will get roll");
//					float rollValue = instruments.getRoll();
//					
//					String latencies = "";
//					long lastScreenUpdate = 0;
//					
//					System.out.println("Will start loop");
//					
//					while (running){
//						
//						long start = System.currentTimeMillis();
//						System.out.println("Setting throttle");
//						gateway.setThrottle(sentVal);
//						sentVal++;
//						System.out.println("Waiting for response");
//						while (instruments.getRoll() == rollValue){
//							Thread.sleep(10);
//							
//						}
//						System.out.println("Got response!");
//						rollValue = instruments.getRoll();
//						latencies += (System.currentTimeMillis() - start) + ",";
//						
//						if (System.currentTimeMillis() - lastScreenUpdate > 1000){
//							System.out.println(latencies);
//							latencies = "";
//							lastScreenUpdate = System.currentTimeMillis();
//						}
//						
//						if (sentVal == 128)
//							sentVal = 0;
//					}					
