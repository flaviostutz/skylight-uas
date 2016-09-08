package br.skylight.cucs.mapkit;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.mapviewer.TileCache;

public class DiskTileCache extends TileCache {

	private File cacheDir;
	private boolean throwExceptionOnNotFound = false;
	public static String EXCEPTION_MESSAGE = "NOT PERMITED NOW";
	
	public DiskTileCache() {
		try {
			File tmp = File.createTempFile("test", ".tmp");
			cacheDir = new File(tmp.getParentFile(), "mapkit-cache");
			tmp.delete();
			if(!cacheDir.exists()) {
				cacheDir.mkdir();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DiskTileCache(File cacheDir) {
		if(!cacheDir.exists()) {
			cacheDir.mkdir();
		} else {
			if(!cacheDir.isDirectory()) {
				throw new IllegalArgumentException("'cacheDir' must be a directory");
			}
		}
		this.cacheDir = cacheDir;
	}
	
	@Override
	public BufferedImage get(URI uri) throws IOException {
//		System.out.println("get " + uri);
		try {
			File cacheFile = new File(cacheDir, getFileNameFromUri(uri));
			FileInputStream fis = new FileInputStream(cacheFile);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			readInputStreamIntoOutputStream(fis, bos);
			bos.close();
			fis.close();
			return ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
//			return PaintUtils.loadCompatibleImage(new ByteArrayInputStream(bos.toByteArray()));
		} catch (FileNotFoundException e) {
			if(throwExceptionOnNotFound) {
				throw new RuntimeException(EXCEPTION_MESSAGE);
			} else {
				//return null so that the tile will be downloaded from URL
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void put(URI uri, byte[] bimg, BufferedImage img) {
//		System.out.println("put " + uri);
		try {
			File cacheFile = new File(cacheDir, getFileNameFromUri(uri));
			FileOutputStream fos = new FileOutputStream(cacheFile);
			ByteArrayInputStream bis = new ByteArrayInputStream(bimg);
			readInputStreamIntoOutputStream(bis, fos);
			bis.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private String getFileNameFromUri(URI uri) {
		if(uri==null) return "temp.tmp";
		int i = uri.toString().lastIndexOf("?");
		if(i==-1) i = uri.toString().lastIndexOf("/");
		if(uri.getHost().indexOf("google.com")==-1) {
			return uri.getHost()+"-"+uri.toString().substring(i+1);
		} else {
			String urs = uri.toString();
			return "google.com-" + urs.substring(urs.lastIndexOf("t="));
		}
	}
	
	
	private void readInputStreamIntoOutputStream(InputStream is, OutputStream os) throws IOException {
	    byte[] buf = new byte[512];
	    while(true) {
	        int n = is.read(buf);
	        if(n == -1) break;
	        os.write(buf,0,n);
	    }
	}

	public void setThrowExceptionOnNotFound(boolean throwExceptionOnNotFound) {
		this.throwExceptionOnNotFound = throwExceptionOnNotFound;
	}

}
