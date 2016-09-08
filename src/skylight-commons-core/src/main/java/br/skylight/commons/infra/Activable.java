package br.skylight.commons.infra;

public interface Activable {

	public boolean isActive();
	public boolean isInitialized();
	public void init() throws Exception;
	public void activate() throws Exception;
	public void deactivate() throws Exception;
	
}
