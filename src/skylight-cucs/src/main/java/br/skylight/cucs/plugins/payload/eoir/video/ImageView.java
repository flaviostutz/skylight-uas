package br.skylight.cucs.plugins.payload.eoir.video;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImageView extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private BufferedImage image;
	private boolean maintainAspectRatio = true;

	public void setMaintainAspectRatio(boolean maintainAspectRatio) {
		this.maintainAspectRatio = maintainAspectRatio;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
	public BufferedImage getImage() {
		return image;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(image!=null) {
			int width = getWidth();
			int height = getHeight();
			int xoffset = 0;
			int yoffset = 0;
			if(maintainAspectRatio) {
				if(width>height) {
					height = width * (image.getHeight()/image.getWidth());
					yoffset = (getHeight()-height)/2;
				} else {
					width = height * (image.getWidth()/image.getHeight());
					xoffset = (getWidth()-width)/2;
				}
			}
			g.drawImage(image, xoffset, yoffset, width, height, Color.LIGHT_GRAY, null);
		}
	}
	
}
