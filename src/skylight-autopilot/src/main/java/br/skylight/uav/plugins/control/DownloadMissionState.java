package br.skylight.uav.plugins.control;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import br.skylight.commons.Mission;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.TransferStatus;
import br.skylight.commons.dli.mission.MissionUploadDownloadStatus;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.VehicleIdService;

@ManagedMember
public class DownloadMissionState extends StateAdapter {

	private static final Logger logger = Logger.getLogger(DownloadMissionState.class.getName());
	private MissionPlanCommandStateMachine stateMachine;
	
	private Queue<Message> missionMessageQueue = new LinkedList<Message>();
	private int sentItems = 0;
	private int expectedItems = 0;
	private TimedBoolean statusReportTimer = new TimedBoolean(1000);
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public MessageScheduler messageScheduler;
	
	@ServiceInjection
	public VehicleIdService vehicleIdService;
	
	public void onEntry() throws Exception {
		Mission mission = repositoryService.getMission();
		SkylightMission skylightMission = repositoryService.getSkylightMission();
		missionMessageQueue.clear();
		
		//normal mission messages
		List<Message> missionMessages = mission.getAllMissionMessages();
		for (Message mm : missionMessages) {
			missionMessageQueue.offer(mm);
		}

		//skylight specific mission messages
		List<Message> skylightMissionMessages = skylightMission.getAllMissionMessages();
		for (Message mm : skylightMissionMessages) {
			missionMessageQueue.offer(mm);
		}
		
		//TEST
//		for(int i=1; i<=10; i++) {
//			AVPositionWaypoint p = new AVPositionWaypoint();
//			p.setWaypointNumber(i);
//			p.setNextWaypoint(i+1);
//			missionMessageQueue.offer(p);
//		}
		//TEST
		
		sentItems = 0;
		expectedItems = missionMessageQueue.size();
	}

	public void onStep() throws Exception {
		Message m = missionMessageQueue.poll();
		if(m!=null) {
			m.setVehicleID(vehicleIdService.getInitialVehicleID().getVehicleID());
			m.setCucsID(Message.BROADCAST_ID);
			m.setTimeStamp(System.currentTimeMillis()/1000.0);
			stateMachine.sendMessageSynchronously(m);
			sentItems++;

			//send status message
			if(statusReportTimer.checkTrue()) {
				messageScheduler.sendScheduledMessage(MessageType.M900);
			}
		} else {
			//transfer has ended. notify cucs
			MissionUploadDownloadStatus ms = messagingService.resolveMessageForSending(MissionUploadDownloadStatus.class);
			ms.setStatus(TransferStatus.COMPLETE);
			ms.setPercentComplete(100);
			messagingService.sendMessage(ms);
			
			stateMachine.enterState(MissionPlanMode.NOT_USED);
		}
	}
	
//	private void sendMessageSynchronously(Message m) throws IOException {
//		//if async sender is in place, ignore it so that send progress will be related to real mission transfer (not only its schedule)
//		if(messagingService instanceof AsyncSenderMessagingService) {
//			((AsyncSenderMessagingService)messagingService).sendMessageSynchronously(m);
//		} else {
//			messagingService.sendMessage(m);
//		}
//	}

	public int getPercentComplete() {
		if(expectedItems==0) {
			return 100;
		} else {
			return (int)(100*((float)sentItems/(float)expectedItems));
		}
	}
	
	public void setStateMachine(MissionPlanCommandStateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}
	
}
