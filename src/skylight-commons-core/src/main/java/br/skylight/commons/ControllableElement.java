package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class ControllableElement implements SerializableState {

	//serializable state
	private String name = "";
	private Map<Integer,CUCSControl> cucsControls = new HashMap<Integer,CUCSControl>();//per cucsid
	protected VehicleID vehicleID;
	
	//transient
	private boolean authorizeAnyCUCS = false;
	private boolean authorizeOverrideAnyCUCS = false;

	public VehicleID getVehicleID() {
		return vehicleID;
	}
	public void setVehicleID(VehicleID vehicleID) {
		this.vehicleID = vehicleID;
	}

	public CUCSControl resolveCUCSControl(int cucsId) {
		synchronized(cucsControls) {
			CUCSControl c = cucsControls.get(cucsId);
			if(c==null) {
				c = new CUCSControl();
				if(authorizeAnyCUCS) {
					if(this instanceof Vehicle) {
						c.getAuthorizedLOIs().setLOIs(2,3,4,5);
					} else {
						c.getAuthorizedLOIs().setLOIs(2,3);
					}
				}
				if(authorizeOverrideAnyCUCS) {
					if(this instanceof Vehicle) {
						c.getAuthorizedOverrideLOIs().setLOIs(2,3,4,5);
					} else {
						c.getAuthorizedOverrideLOIs().setLOIs(2,3);
					}
				}
				cucsControls.put(cucsId, c);
			}
			return c;
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<Integer, CUCSControl> getCucsControls() {
		return cucsControls;
	}
	
	public void setAuthorizeOverrideAnyCUCS(boolean authorizeOverrideAnyCUCS) {
		this.authorizeOverrideAnyCUCS = authorizeOverrideAnyCUCS;
	}
	public boolean isAuthorizeOverrideAnyCUCS() {
		return authorizeOverrideAnyCUCS;
	}
	
	public void setAuthorizeAnyCUCS(boolean authorizeAnyCUCS) {
		this.authorizeAnyCUCS = authorizeAnyCUCS;
	}
	public boolean isAuthorizeAnyCUCS() {
		return authorizeAnyCUCS;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		name = in.readUTF();
		IOHelper.readMapStateIntKey(cucsControls, CUCSControl.class, in);
		vehicleID = IOHelper.readState(VehicleID.class, in);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeUTF(name);
		IOHelper.writeMapStateIntKey(cucsControls, out);
		IOHelper.writeState(vehicleID, out);
	}

	
}
