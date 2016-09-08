package br.skylight.uav.infra;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import br.skylight.commons.JVMHelper;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.SerialDataTerminal;
import br.skylight.commons.io.dataterminal.UDPMulticastDataTerminal;
import br.skylight.commons.io.dataterminal.ac4790.AC4790DataTerminal;

public class UAVHelper {

	private static Map<String,String> notifiedStates = new HashMap<String,String>();

	private static final Logger logger = Logger.getLogger(UAVHelper.class.getName());
	
	public static void notifyStateFine(Logger logger, String msg, String stateId) {
//		System.out.println(">>>> " + msg);
		if(msg!=null && !msg.equals(notifiedStates.get(stateId))) {
			logger.fine(msg);
			notifiedStates.put(stateId, msg);
		}
	}

	public static DataTerminal loadDataTerminal(Properties props, String propertyPrefix, DataTerminalType dataTerminalType, int dataLinkId, int udpSendPort, int udpReceivePort) {
		DataTerminal dt = null;
		SerialConnectionParams connectionParams = new SerialConnectionParams(props, propertyPrefix);
		if(connectionParams.isValid()) {
			if(JVMHelper.isTrue(props, propertyPrefix+".useAC4790")) {
				logger.info("Using AC4790 "+ dataTerminalType +" for " + propertyPrefix);
				dt = new AC4790DataTerminal(connectionParams, dataTerminalType, dataLinkId, false, false);
			} else {
				logger.info("Using Serial "+ dataTerminalType +" for " + propertyPrefix);
				dt = new SerialDataTerminal(connectionParams, dataTerminalType, dataLinkId, false, false);
			}
		} else {
			logger.info("Using UDP "+ dataTerminalType +" for " + propertyPrefix + " out=" + udpSendPort + "; in=" + udpReceivePort);
			dt = new UDPMulticastDataTerminal(DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE, DataTerminal.DEFAULT_MULTICAST_ADDRESS, udpSendPort, udpReceivePort, dataTerminalType, dataLinkId);
		}
		return dt;
	}
	
}
