package br.skylight.cucs.plugins.payload.eoir.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CameraSnapshotImage extends ImageView {

	private static final long serialVersionUID = 1L;
	
	private File imageFile;
	private CameraImageInfo cameraImageInfo;
	private ImageView imageView = null;

	private CameraSnapshotImage(File imageFile, CameraImageInfo cameraImageInfo) {
		this.cameraImageInfo = cameraImageInfo;
		this.imageFile = imageFile;
		initialize();
	}
	
	private void initialize() {
		setLayout(new BorderLayout());
		this.setSize(new Dimension(189, 174));
		this.add(getImageView(), BorderLayout.CENTER);
		
	}
	
	public static CameraSnapshotImage createImage(File dir, String fileName, BufferedImage image, CameraImageInfo info) throws IOException {
		//save image contents
		File imageFile = new File(dir, fileName);
		if(imageFile.exists()) {
			throw new IllegalArgumentException("Image file already exists");
		}
		ImageIO.write(image, "png", imageFile);
		
		//save image descriptor
		File imageDescr = new File(dir, fileName);
		if(imageDescr.exists()) imageDescr.delete();
		FileOutputStream fos = new FileOutputStream(imageDescr);
		DataOutputStream dos = new DataOutputStream(fos);
		info.writeState(dos);
		fos.close();
		
		//return image
		return loadImage(dir, fileName);
	}

	public static CameraSnapshotImage loadImage(File dir, String fileName) throws IOException {
		//read image contents
		File imageFile = new File(dir, fileName);
		if(!imageFile.exists()) {
			throw new IllegalArgumentException("Image file doesn't exist");
		}
		BufferedImage image = ImageIO.read(imageFile);
		BufferedImage thumbImage = (BufferedImage)image.getScaledInstance(150, 150 * (image.getHeight()/image.getWidth()), Image.SCALE_SMOOTH);
		
		//read image descriptor
		File imageDescr = new File(dir, fileName);
		if(!imageDescr.exists()) {
			throw new IllegalArgumentException("Image descriptor file doesn't exist");
		}
		FileInputStream fis = new FileInputStream(imageDescr);
		DataInputStream dis = new DataInputStream(fis);
		CameraImageInfo info = new CameraImageInfo();
		info.readState(dis);
		dis.close();
		
		//return loaded image
		CameraSnapshotImage r = new CameraSnapshotImage(imageFile, info);
		r.getImageView().setImage(thumbImage);
		return r;
	}

	/**
	 * This method initializes imageView	
	 * 	
	 * @return br.skylight.groundstation.widgets.camera.ImageView	
	 */
	private ImageView getImageView() {
		if (imageView == null) {
			imageView = new ImageView();
		}
		return imageView;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
