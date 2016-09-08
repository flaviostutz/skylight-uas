package br.skylight.commons.plugin;

public interface PluginManagerListener {

	public void onStartupStatusChanged(String message, int percent);
	public void onPluginsStartupFinished(boolean partial);
	public void onPluginsStartupFailed();
	
}
