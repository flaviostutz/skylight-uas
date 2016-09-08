package br.skylight.cucs.plugins.subscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageSentListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.repository.Target;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=SubscriberService.class)
public class SubscriberService extends Worker implements MessageListener, MessageSentListener {

	private static final Logger logger = Logger.getLogger(SubscriberService.class.getName());
	
	private Map<MessageType,List<MessageListener>> messageListeners;
	private Map<MessageType,List<MessageSentListener>> messageSentListeners;
	
	private List<TargetListener> targetListeners;
	private List<MissionListener> missionListeners;
	private List<RunwayListener> runwayListeners;
	private List<VehicleListener> vehicleListeners;
	private List<VehicleSteeringListener> vehicleSteeringListeners;
	private List<PayloadSteeringListener> payloadSteeringListeners;
	private List<LoiterListener> loiterListeners;
	private List<PreferencesListener> preferencesListeners;

	private Vehicle lastSelectedVehicle;
	private Payload lastSelectedPayload;
	
	//used to distribute messages using Swing's thread
	private MessageRunner messageRunner;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public void onActivate() {
		messageListeners = new HashMap<MessageType,List<MessageListener>>();
		messageSentListeners = new HashMap<MessageType,List<MessageSentListener>>();
		targetListeners = new CopyOnWriteArrayList<TargetListener>();
		missionListeners = new CopyOnWriteArrayList<MissionListener>();
		runwayListeners = new CopyOnWriteArrayList<RunwayListener>();
		vehicleListeners = new CopyOnWriteArrayList<VehicleListener>();
		loiterListeners = new CopyOnWriteArrayList<LoiterListener>();
		vehicleSteeringListeners = new CopyOnWriteArrayList<VehicleSteeringListener>();
		payloadSteeringListeners = new CopyOnWriteArrayList<PayloadSteeringListener>();
		preferencesListeners = new CopyOnWriteArrayList<PreferencesListener>();
		
		//register itself as listener for all incoming messages so it can distribute to listeners
		messagingService.setMessageListener(this);
		messagingService.setMessageSentListener(this);
		
		messageRunner = new MessageRunner();
	}
	
	@Override
	public void onDeactivate() {
		messagingService.setMessageListener(this);
	}
	
	public void addMessageListener(MessageType messageType, MessageListener ml) {
		resolveListeners(messageType).add(ml);
	}

	public void addMessageSentListener(MessageType messageType, MessageSentListener ml) {
		resolveSentListeners(messageType).add(ml);
	}

	@Override
	public void onMessageReceived(Message message) {
		if(pluginManager.isPluginsStarted()) {
			List<MessageListener> r = messageListeners.get(message.getMessageType());
			if(r!=null) {
				for (MessageListener ml : r) {
					try {
						//run this task in Swing's dispatcher thread to avoid problems 
						//with components that are not thread safe
						messageRunner.setMessage(message);
						messageRunner.setMessageListener(ml);
						SwingUtilities.invokeAndWait(messageRunner);
					} catch (Exception e) {
						logger.warning("There was a problem delivering message to " + ml + ". e=" + e.toString());
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onMessageSent(Message message) {
		List<MessageSentListener> r = messageSentListeners.get(message.getMessageType());
		if(r!=null) {
			for (MessageSentListener ml : r) {
				ml.onMessageSent(message);
			}
		}
	}
	
	private List<MessageListener> resolveListeners(MessageType messageType) {
		synchronized(messageListeners) {
			List<MessageListener> r = messageListeners.get(messageType);
			if(r==null) {
				r = new CopyOnWriteArrayList<MessageListener>();
				messageListeners.put(messageType, r);
			}
			return r;
		}
	}

	private List<MessageSentListener> resolveSentListeners(MessageType messageType) {
		synchronized(messageSentListeners) {
			List<MessageSentListener> r = messageSentListeners.get(messageType);
			if(r==null) {
				r = new CopyOnWriteArrayList<MessageSentListener>();
				messageSentListeners.put(messageType, r);
			}
			return r;
		}
	}

	public void addTargetListener(TargetListener tl) {
		targetListeners.add(tl);
	}
	public void notifyTargetEvent(final Target target, EventType type, TargetListener notifierAvoidLoop) {
		for (TargetListener tl : targetListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=tl) {
				tl.onTargetEvent(target, type);
			}
		}
	}
	
	
	public void addMissionListener(MissionListener wl) {
		missionListeners.add(wl);
	}
	public void notifyMissionWaypointEvent(Mission mission, final AVPositionWaypoint pw, EventType type, MissionListener notifierAvoidLoop) {
		for (MissionListener tl : missionListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=tl) {
				tl.onWaypointEvent(mission, pw, type);
			}
		}
	}

	public void notifyMissionEvent(final Mission mission, EventType type, MissionListener notifierAvoidLoop) {
		for (MissionListener tl : missionListeners) {
//			System.out.println("tl: " + tl + "; notifier=" + notifierAvoidLoop);
			if(notifierAvoidLoop==null || notifierAvoidLoop.getClass()!=tl.getClass()) {
				tl.onMissionEvent(mission, type);
			}
		}
	}
	
	public void addRunwayListener(RunwayListener wl) {
		runwayListeners.add(wl);
	}
	public void notifyRunwayEvent(final Runway rw, EventType type, RunwayListener notifierAvoidLoop) {
		for (RunwayListener tl : runwayListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=tl) {
				tl.onRunwayEvent(rw, type);
			}
		}
	}
	
	public void addVehicleListener(VehicleListener vl) {
		vehicleListeners.remove(vl);
		vehicleListeners.add(vl);
	}
	public void notifyVehicleEvent(final Vehicle av, EventType type, VehicleListener notifierAvoidLoop) {
		lastSelectedVehicle = av;
		for (VehicleListener tl : vehicleListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=tl) {
				tl.onVehicleEvent(av, type);
			}
		}
	}
	public void notifyPayloadEvent(final Payload p, EventType type, VehicleListener notifierAvoidLoop) {
		lastSelectedPayload = p;
		for (VehicleListener tl : vehicleListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=tl) {
				tl.onPayloadEvent(p, type);
			}
		}
	}

	public void addLoiterListener(LoiterListener ll) {
		loiterListeners.add(ll);
	}
	public void notifyLoiterEvent(LoiterConfiguration lc, EventType type, Object notifierAvoidLoop) {
		for (LoiterListener ll : loiterListeners) {
			if(notifierAvoidLoop==null || notifierAvoidLoop!=ll) {
				ll.onLoiterEvent(lc, type);
			}
		}
	}
	
	public Vehicle getLastSelectedVehicle() {
		return lastSelectedVehicle;
	}

	public void removeMessageListener(MessageType messageType, MessageListener ml) {
		resolveListeners(messageType).remove(ml);
	}

	public void notifyPreferencesUpdated() {
		for (PreferencesListener ll : preferencesListeners) {
			ll.onPreferencesUpdated();
		}
	}
	public void addPreferencesListener(PreferencesListener pl) {
		preferencesListeners.add(pl);
	}

	public void removeVehicleListener(VehicleListener listener) {
		vehicleListeners.remove(listener);
	}

	public void notifyVehicleSteeringEvent(Vehicle vehicle) {
		for (VehicleSteeringListener ll : vehicleSteeringListeners) {
			ll.onSteeringEvent(vehicle);
		}
	}
	public void addVehicleSteeringListener(VehicleSteeringListener pl) {
		vehicleSteeringListeners.add(pl);
	}

	public void notifyPayloadSteeringEvent(Payload payload) {
		for (PayloadSteeringListener ll : payloadSteeringListeners) {
			ll.onPayloadSteeringEvent(payload);
		}
	}
	public void addPayloadSteeringListener(PayloadSteeringListener pl) {
		payloadSteeringListeners.add(pl);
	}
	
	public Payload getLastSelectedPayload() {
		return lastSelectedPayload;
	}
	
}
