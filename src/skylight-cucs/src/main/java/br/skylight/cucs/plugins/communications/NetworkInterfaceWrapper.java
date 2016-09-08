package br.skylight.cucs.plugins.communications;

import java.net.NetworkInterface;

public class NetworkInterfaceWrapper {

	private NetworkInterface networkInterface;

	public NetworkInterfaceWrapper(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}

	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}
	
	@Override
	public String toString() {
		return networkInterface.getDisplayName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((networkInterface == null) ? 0 : networkInterface.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkInterfaceWrapper other = (NetworkInterfaceWrapper) obj;
		if (networkInterface == null) {
			if (other.networkInterface != null)
				return false;
		} else if (!networkInterface.equals(other.networkInterface))
			return false;
		return true;
	}
	
}
