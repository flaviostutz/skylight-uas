package br.skylight.uav.plugins.payload;

import br.skylight.commons.Payload;
import br.skylight.commons.dli.payload.PayloadConfigurationMessage;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ExtensionPointDefinition
public abstract class PayloadOperatorExtensionPoint extends Worker {

	private Payload payload;

	@ServiceInjection
	public PayloadService payloadService;

	public PayloadOperatorExtensionPoint(Payload payload) {
		this.payload = payload;
	}
	
	public Payload getPayload() {
		return payload;
	}

	public boolean prepareScheduledPayloadMessage(Message message) {	
		//M300
		if(message instanceof PayloadConfigurationMessage) {
			PayloadConfigurationMessage m = (PayloadConfigurationMessage)message;
			m.setNumberOfPayloadRecordingDevices(getPayload().getNumberOfPayloadRecordingDevices());
			m.setPayloadStationsAvailable(payloadService.getAvailablePayloadStations());
			m.getStationNumber().addStation(getPayload().getUniqueStationNumber());
			m.setPayloadType(getPayload().getPayloadType());
			return true;
		}
		return false;
	}
	
	public abstract void onPayloadMessageReceived(Message message);
	
}
