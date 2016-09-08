package br.skylight.cucs.plugins.gamecontroller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class ControllerBindingProfile implements SerializableState {

	private String profileName = "";
	private ArrayList<ControllerBinding> bindings = new ArrayList<ControllerBinding>();
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		IOHelper.readArrayList(in, ControllerBinding.class, bindings);
		profileName = in.readUTF();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		IOHelper.writeArrayList(out, bindings);
		out.writeUTF(profileName);
	}

	public ArrayList<ControllerBinding> getBindings() {
		return bindings;
	}
	
	public ControllerBinding resolveControllerBinding(int bindingDefinitionId) {
		ControllerBinding br = null;
		for (ControllerBinding b : bindings) {
			if(b.getDefinitionId()==bindingDefinitionId) {
				br = b;
				break;
			}
		}
		if(br==null) {
			br = new ControllerBinding();
			br.setDefinitionId(bindingDefinitionId);
			bindings.add(br);
		}
		return br;
	}
	
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getProfileName() {
		return profileName;
	}
	
	@Override
	public String toString() {
		return profileName;
	}
	
}
