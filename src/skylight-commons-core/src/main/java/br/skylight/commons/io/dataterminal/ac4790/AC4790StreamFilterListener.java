package br.skylight.commons.io.dataterminal.ac4790;

public interface AC4790StreamFilterListener {

	public void notifyTransmittedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, boolean successfulDelivery);
	public void notifyReceivedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, int payloadLen);

}
