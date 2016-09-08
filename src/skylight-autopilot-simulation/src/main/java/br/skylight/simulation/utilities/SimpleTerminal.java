package br.skylight.simulation.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import br.skylight.commons.io.SerialConnectionParams;

public class SimpleTerminal {

	public static void main(String[] args) throws IOException {
		if(args.length<2) {
			System.out.println("Usage: java -jar terminal.jar [properties file] [properties name prefix]");
		} else {
			File pf = new File(args[0]);
			Properties f = new Properties();
			FileInputStream fis = new FileInputStream(pf);
			f.load(fis);
			fis.close();
			
			final SerialConnectionParams p = new SerialConnectionParams(f, args[1]);
			
			//READ STREAM
			p.resolveConnection();
			Thread t = new Thread() {
				public void run() {
					while(true) {
						try {
							System.out.print((char)p.resolveConnection().getInputStream().read());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
			};
			t.setDaemon(true);
			t.start();
			
			//WRITE STREAM
			while(true) {
				int i = System.in.read();
				p.resolveConnection().getOutputStream().write(i);
			}
		}
	}
	
}
