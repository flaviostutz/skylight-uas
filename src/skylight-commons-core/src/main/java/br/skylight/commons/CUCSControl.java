package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.infra.SerializableState;

public class CUCSControl implements SerializableState {

	private BitmappedLOI authorizedLOIs = new BitmappedLOI();
	private BitmappedLOI authorizedOverrideLOIs = new BitmappedLOI();
	private BitmappedLOI grantedLOIs = new BitmappedLOI();
	private boolean overrideMode = false;
	
	public BitmappedLOI getAuthorizedLOIs() {
		return authorizedLOIs;
	}
	public BitmappedLOI getGrantedLOIs() {
		return grantedLOIs;
	}
	public boolean isOverrideMode() {
		return overrideMode;
	}
	public BitmappedLOI getAuthorizedOverrideLOIs() {
		return authorizedOverrideLOIs;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		authorizedLOIs.setData(in.readLong());
		authorizedOverrideLOIs.setData(in.readLong());
		grantedLOIs.setData(in.readLong());
		overrideMode = in.readBoolean();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeLong(authorizedLOIs.getData());
		out.writeLong(authorizedOverrideLOIs.getData());
		out.writeLong(grantedLOIs.getData());
		out.writeBoolean(overrideMode);
	}
	
}
