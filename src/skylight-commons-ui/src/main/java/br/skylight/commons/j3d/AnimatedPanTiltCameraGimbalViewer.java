package br.skylight.commons.j3d;

import javax.swing.JFrame;

import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedValue;

public class AnimatedPanTiltCameraGimbalViewer extends PanTiltCameraGimbalViewer {

	private static final long serialVersionUID = 1L;
	
	private ThreadWorker animator;
	private double pitchSpeed = Math.PI;
	private double yawSpeed = Math.PI;
	
	private TimedValue pitchValue = new TimedValue(-Double.MAX_VALUE, Double.MAX_VALUE);
	private TimedValue yawValue = new TimedValue(-Double.MAX_VALUE, Double.MAX_VALUE);

	public AnimatedPanTiltCameraGimbalViewer() {
		throw new IllegalStateException("This was not finished yet. Not working right!");
	}
	
	@Override
	public void setCameraGimbalOrientation(double pitch, double yaw) {
		System.out.println("SET ORIENTATION " + pitch + " " + yaw);
		double currentPitch = pitchValue.getValue();
//		pitch = MathHelper.normalizeAngle2(pitch);
//		currentPitch = MathHelper.normalizeAngle2(currentPitch);
		pitch = currentPitch + MathHelper.getNormalizedErrorTwoPi(currentPitch-pitch);
		pitchValue.setMin(Math.min(pitch,currentPitch));
		pitchValue.setMax(Math.max(pitch,currentPitch));
		pitchValue.start(currentPitch, pitch, (pitch-currentPitch)/pitchSpeed, false);

		double currentYaw = yawValue.getValue();
//		yaw = MathHelper.normalizeAngle2(yaw);
//		currentYaw = MathHelper.normalizeAngle2(currentYaw);
		yaw = currentYaw + MathHelper.getNormalizedErrorTwoPi(currentYaw-yaw);
		yawValue.setMin(Math.min(yaw,currentYaw));
		yawValue.setMax(Math.max(yaw,currentYaw));
		yawValue.start(currentYaw, yaw, (yaw-currentYaw)/yawSpeed, false);
	}

	protected void stepAnimation() {
		if(!Double.isNaN(pitchValue.getValue()) && !Double.isNaN(yawValue.getValue())) {
			System.out.println(pitchValue.getValue() + " " + yawValue.getValue());
			super.setCameraGimbalOrientation(pitchValue.getValue(), yawValue.getValue());
		} else {
			System.out.println("NaN!");
		}
		super.updateUI();
	}
	
	public void startAnimation(int fps) throws Exception {
		if(animator!=null && animator.isActive()) {
			animator.deactivate();
		}
		animator = new ThreadWorker(fps) {
			@Override
			public void step() throws Exception {
				stepAnimation();
			}	
		};
		animator.activate();
	}
	
	public void stopAnimation() throws Exception {
		if(animator!=null) {
			animator.deactivate();
		}
	}
	
	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		final AnimatedPanTiltCameraGimbalViewer cv = new AnimatedPanTiltCameraGimbalViewer();
		f.setSize(300,300);
		f.add(cv);
		f.setVisible(true);
		cv.startAnimation(25);
		
		System.out.println("STEP 1");
		cv.setCameraGimbalOrientation(Math.toRadians(0), Math.toRadians(0));
		
		Thread.sleep(5000);
		System.out.println("STEP 2");
		cv.setCameraGimbalOrientation(Math.toRadians(-45), Math.toRadians(0));
		
		Thread.sleep(5000);
		System.out.println("STEP 3");
		cv.setCameraGimbalOrientation(Math.toRadians(-45), Math.toRadians(45));
		
		Thread.sleep(5000);
		System.out.println("STEP 4");
		cv.setCameraGimbalOrientation(Math.toRadians(-90), Math.toRadians(45));
		
		Thread.sleep(5000);
		System.out.println("STEP 5");
		cv.setCameraGimbalOrientation(Math.toRadians(-90), Math.toRadians(180));
		
		Thread.sleep(5000);
		System.out.println("STEP 6");
		cv.setCameraGimbalOrientation(Math.toRadians(-135), Math.toRadians(180));
		
		Thread.sleep(5000);
		System.out.println("STEP 7");
		cv.setCameraGimbalOrientation(Math.toRadians(-90), Math.toRadians(270));
		
		cv.updateUI();
	}
	
}
