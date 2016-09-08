package br.skylight.cucs.plugins.payload.eoir.video;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

public class FrameGrabberExample {

	public static void main(String[] args) throws Exception {
		// Create capture device
		CaptureDeviceInfo deviceInfo = CaptureDeviceManager.getDevice("vfw:Microsoft WDM Image Capture	(Win32):0");
		Player player = Manager.createRealizedPlayer(deviceInfo.getLocator());
		player.start();

		// Wait a few seconds for camera to initialise (otherwise img==null)
		Thread.sleep(5000);

		// Grab a frame from the capture device
		FrameGrabbingControl frameGrabber = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
		Buffer buf = frameGrabber.grabFrame();

		// Convert frame to an buffered image so it can be processed and saved
		Image img = (new BufferToImage((VideoFormat) buf.getFormat()).createImage(buf));
		BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();
		g.drawImage(img, null, null);

		ImageIO.write(buffImg, "jpg", new File("C:/Test.jpg"));

		// Stop using webcam
		player.close();
		player.deallocate();
		System.exit(0);
	}

}
