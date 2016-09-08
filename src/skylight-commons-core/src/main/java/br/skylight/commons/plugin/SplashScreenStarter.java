package br.skylight.commons.plugin;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

public class SplashScreenStarter {

	private static SplashScreen splash = null;  //  @jve:decl-index=0:
	private static Graphics2D g = null;  //  @jve:decl-index=0:
	
	public static void startupPlugins(PluginManager pm) {
		//show splash screen
		splash = SplashScreen.getSplashScreen();
		if(splash!=null) {
			g = (Graphics2D) splash.createGraphics();
		}

		try {
			pm.addPluginManagerListener(new PluginManagerListener() {
				@Override
				public void onStartupStatusChanged(String message, int percent) {
					updateLoadingStatus(message, percent);
				}
				@Override
				public void onPluginsStartupFinished(boolean partial) {
				}
				@Override
				public void onPluginsStartupFailed() {
				}
			});
			
			//STARTUP
			pm.startupPlugins();
			
		} catch (Exception e) {
			e.printStackTrace();
			updateLoadingStatus("Startup error: "+e.getMessage(), 100);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}
	}

	private static void updateLoadingStatus(String message, int percent) {
		if(g!=null) {
	        g.setComposite(AlphaComposite.Clear);
	        g.fillRect(0,220,(int)splash.getSize().getWidth(),40);
	        g.setPaintMode();
	        g.setColor(Color.WHITE);
	        g.drawString(message, 20, 236);
	        g.setColor(Color.GRAY);
	        g.fillRect((int)(splash.getSize().getWidth()*0.05), 243,
	        				(int)((percent/100F)*splash.getSize().getWidth()*0.9F),5);
			if(splash!=null) {
				splash.update();
			}
		} else {
			System.out.println(message + " (" + percent + ")");
		}
	}
	
}
