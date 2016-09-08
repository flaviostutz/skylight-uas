package br.skylight.uav.plugins.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import br.skylight.commons.Alert;
import br.skylight.commons.Mission;
import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.TransferStatus;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.mission.MissionUploadDownloadStatus;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.services.AsyncSenderMessagingService;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.services.ScheduledMessageReporter;
import br.skylight.commons.dli.skylight.MissionAnnotationsMessage;
import br.skylight.commons.dli.skylight.TakeoffLandingConfiguration;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.commons.statemachine.StateMachine;
import br.skylight.commons.statemachine.StateMachineListener;
import br.skylight.uav.plugins.messaging.MessageScheduler;
import br.skylight.uav.plugins.storage.RepositoryService;

@ManagedMember
public class MissionPlanCommandStateMachine extends StateMachine<MissionPlanMode,MissionUploadCommand> implements MessageListener, ScheduledMessageReporter {

	private static final Logger logger = Logger.getLogger(MissionPlanCommandStateMachine.class.getName());
	private Mission tempMission;
	private SkylightMission tempSkylightMission;
	
	private TimedBoolean statusReportTimer = new TimedBoolean(1000);
	private int expectedMissionItems = -1;
	private int actualMissionItems;
	private boolean receivedTakeoffLanding;
	private boolean receivedRulesOfSafety;
	private boolean receivedAnnotations;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public MessagingService messagingService;
	@ServiceInjection
	public MessageScheduler messageScheduler;
	@MemberInjection
	public DownloadMissionState downloadMissionState;
	@MemberInjection
	public FlightEngineer flightEngineer;
	@MemberInjection
	public Pilot pilot;
	
	public MissionPlanCommandStateMachine() {
		tempMission = new Mission();
		tempSkylightMission = new SkylightMission();
	}

	@Override
	public void onActivate() throws Exception {
		messagingService.setMessageListener(MessageType.M800, this);
		messagingService.setMessageListener(MessageType.M801, this);
		messagingService.setMessageListener(MessageType.M802, this);
		messagingService.setMessageListener(MessageType.M803, this);
		messagingService.setMessageListener(MessageType.M804, this);
		messagingService.setMessageListener(MessageType.M2006, this);
		messagingService.setMessageListener(MessageType.M2007, this);
		messagingService.setMessageListener(MessageType.M2016, this);

		messageScheduler.setMessageReporter(MessageType.M900, this);
		createStates();
		
		setListener(new StateMachineListener<MissionPlanMode>() {
			@Override
			public void onStateChanged(MissionPlanMode newState, MissionPlanMode oldState) {
				logger.fine("Download/Upload state machine: entering " + newState);
			}
		});
	}
	
	@Override
	public void onMessageReceived(Message message) {
		//800 - mission upload command
		if(message instanceof MissionUploadCommand) {
			MissionUploadCommand m = (MissionUploadCommand)message;
			enterState(m.getMissionPlanMode(), m);

		//receive uploaded messages
		} else if(getCurrentStateId()!=null && getCurrentStateId().equals(MissionPlanMode.RECEIVE_MISSION)) {
			
			//801 - av route
			if(message instanceof AVRoute) {
				tempMission.getRoutes().add((AVRoute)message.createCopy());
				actualMissionItems++;
				
			//802 - av position waypoint
			} else if(message instanceof AVPositionWaypoint) {
				tempMission.getPositionWaypoints().add((AVPositionWaypoint)message.createCopy());
				actualMissionItems++;
			
			//803 - av loiter waypoint
			} else if(message instanceof AVLoiterWaypoint) {
				tempMission.getLoiterWaypoints().add((AVLoiterWaypoint)message.createCopy());
				actualMissionItems++;
			
			//804 - payload action waypoint
			} else if(message instanceof PayloadActionWaypoint) {
				tempMission.getPayloadActionWaypoints().add((PayloadActionWaypoint)message.createCopy());
				actualMissionItems++;
				
			//2006 - takeoff/landing runways
			} else if(message instanceof TakeoffLandingConfiguration) {
				IOHelper.copyState(tempSkylightMission.getTakeoffLandingConfiguration(), message);
				receivedTakeoffLanding = true;
				actualMissionItems++;
				
			//2007 - rules os safety
			} else if(message instanceof RulesOfSafety) {
				IOHelper.copyState(tempSkylightMission.getRulesOfSafety(), message);
				receivedRulesOfSafety = true;
				actualMissionItems++;

			//2016 - mission annotations
			} else if(message instanceof MissionAnnotationsMessage) {
				IOHelper.copyState(tempMission.getMissionAnnotations(), message);
				receivedAnnotations = true;
				System.out.println("RECEIVED ANNOTATIONS");
				actualMissionItems++;
			}
			
		} else {
			logger.warning("Won't process message " + message.getMessageType() + " because we are not receiving mission messages now");
		}
	}

	@Override
	public boolean prepareScheduledMessage(Message message) {
		//900 - mission upload download status
		if(message instanceof MissionUploadDownloadStatus) {
			MissionUploadDownloadStatus m = (MissionUploadDownloadStatus)message;
			if(getCurrentStateId().equals(MissionPlanMode.DOWNLOAD_MISSION)) {
				DownloadMissionState dm = (DownloadMissionState)getCurrentState();
				m.setPercentComplete(dm.getPercentComplete());
				m.setStatus(TransferStatus.IN_PROGRESS);
			} else if(getCurrentStateId().equals(MissionPlanMode.RECEIVE_MISSION)) {
				m.setPercentComplete((int)(100*(float)actualMissionItems/(float)expectedMissionItems));
				m.setStatus(TransferStatus.IN_PROGRESS);
			} else {
				m.setPercentComplete(100);
				m.setStatus(TransferStatus.COMPLETE);
			}
		}

		return true;
	}

	private void createStates() {
		addState(MissionPlanMode.CLEAR_MISSION, new StateAdapter() {
			public void onEntry() throws Exception {
				repositoryService.clearMission();
				enterState(MissionPlanMode.NOT_USED);
				flightEngineer.deactivateAlert(Alert.MISSION_WARNING, "Mission cleared");
				flightEngineer.deactivateAlert(Alert.MISSION_ERROR, "Mission cleared");
			}
		});
		addState(MissionPlanMode.LOAD_MISSION, new StateAdapter() {
			public void onEntry() throws Exception {
				if(actualMissionItems==expectedMissionItems) {
					String oldMissionId = "WILL_ALWAYS_BE_DIFFERENT";
					if(repositoryService.getMission()!=null) {
						oldMissionId = repositoryService.getMission().getMissionID();
					}
					
					//merge mission
					Mission candidateMission = repositoryService.getMission();
					if(candidateMission!=null) {
						candidateMission = candidateMission.createCopy();
					} else {
						candidateMission = new Mission();
					}
					candidateMission.setMissionID(getCurrentStateData().getMissionID());
					tempMission.computeWaypointsMap();
					for(WaypointDef wd : tempMission.getComputedWaypointsMap().values()) {
						candidateMission.removeWaypointAt(wd.getWaypointNumber(), false);
						if(wd.getPositionWaypoint()!=null) {
							candidateMission.getPositionWaypoints().add(wd.getPositionWaypoint());
						}
						if(wd.getLoiterWaypoint()!=null) {
							candidateMission.getLoiterWaypoints().add(wd.getLoiterWaypoint());
						}
						candidateMission.getPayloadActionWaypoints().clear();
						candidateMission.getPayloadActionWaypoints().addAll(wd.getPayloadActionWaypoints());
					}
					List<AVRoute> deleteRoutes = new ArrayList<AVRoute>();
					for(AVRoute ar : tempMission.getRoutes()) {
						for(AVRoute ear : candidateMission.getRoutes()) {
							if(ear.getInitialWaypointNumber()==ar.getInitialWaypointNumber()) {
								deleteRoutes.add(ear);
							}
						}
					}
					candidateMission.getRoutes().removeAll(deleteRoutes);
					candidateMission.getRoutes().addAll(tempMission.getRoutes());
					if(receivedAnnotations) {
						candidateMission.setMissionAnnotations(tempMission.getMissionAnnotations());
					}
					
					//merge skylight mission
					SkylightMission candidateSkylightMission = repositoryService.getSkylightMission();
					if(candidateSkylightMission!=null) {
						candidateSkylightMission = candidateSkylightMission.createCopy();
					} else {
						candidateSkylightMission = new SkylightMission();
					}
					if(receivedRulesOfSafety) {
						candidateSkylightMission.setRulesOfSafety(tempSkylightMission.getRulesOfSafety());
					}
					if(receivedTakeoffLanding) {
						candidateSkylightMission.setTakeoffLandingConfiguration(tempSkylightMission.getTakeoffLandingConfiguration());
					}

					//validate candidate missions
					VerificationResult vr = candidateMission.validate(repositoryService.getVehicleConfiguration());
					candidateSkylightMission.validate(vr, tempMission, repositoryService.getVehicleConfiguration(), repositoryService.getSkylightVehicleConfiguration());
					if(vr.getErrors().size()==0) {
						if(vr.getWarnings().size()>0) {
							logger.info("Mission accepted with warnings: " + vr.toString());
							flightEngineer.activateAlert(Alert.MISSION_WARNING, "Mission warnings: " + vr.getWarningsStr());
							flightEngineer.deactivateAlert(Alert.MISSION_ERROR, "Mission accepted (with warnings)");
						} else {
							flightEngineer.deactivateAlert(Alert.MISSION_WARNING, "Mission accepted (no warnings)");
							flightEngineer.deactivateAlert(Alert.MISSION_ERROR, "Mission accepted (no warnings)");
						}
						
						//send VehicleID message if mission ID has changed
						if(!tempMission.getMissionID().equals(oldMissionId)) {
//							messageScheduler.sendScheduledMessage(MessageType.M20);
						}
	
						//send mission complete status
						messageScheduler.sendScheduledMessage(MessageType.M900);
//						MissionUploadDownloadStatus m = messagingService.resolveMessageForSending(MissionUploadDownloadStatus.class);
//						m.setStatus(TransferStatus.COMPLETE);
//						m.setPercentComplete(100);
//						messagingService.sendMessage(m);
						
						repositoryService.setMission(candidateMission.createCopy());
						repositoryService.setSkylightMission(candidateSkylightMission.createCopy());
						repositoryService.getMission().computeWaypointsMap();
						flightEngineer.reloadMission();
						logger.info("Mission accepted");
						
					} else {
						logger.info("Mission not accepted: validation errors: " + vr.toString());
						flightEngineer.activateAlert(Alert.MISSION_ERROR, "Mission not accepted: Errors: " + vr.getErrorsStr());
						sendAbortedStatusMessage();
					}
				} else {
					logger.warning("Mission not accepted: expected/actual received items don't match. expected=" + expectedMissionItems + "; actual=" + actualMissionItems);
					flightEngineer.activateAlert(Alert.MISSION_ERROR, "Mission not accepted: because it is incomplete. received=" + actualMissionItems + "/" + expectedMissionItems);
					sendAbortedStatusMessage();
				}
				enterState(MissionPlanMode.NOT_USED);
			}
		});
		
		downloadMissionState.setStateMachine(this);
		addState(MissionPlanMode.DOWNLOAD_MISSION, downloadMissionState);

		addState(MissionPlanMode.DOWNLOAD_SINGLE_WAYPOINT, new StateAdapter() {
			public void onEntry() throws Exception {
				WaypointDef wp = repositoryService.getMission().computeWaypointsMap().get(getCurrentStateData().getWaypointNumber());
				if(wp.getLoiterWaypoint()!=null) {
					wp.getLoiterWaypoint().setTimeStamp(System.currentTimeMillis()/1000.0);
					sendMessageSynchronously(wp.getLoiterWaypoint());
				}
				for (PayloadActionWaypoint aw : wp.getPayloadActionWaypoints()) {
					aw.setTimeStamp(System.currentTimeMillis()/1000.0);
					sendMessageSynchronously(aw);
				}
				if(wp.getPositionWaypoint()!=null) {
					wp.getPositionWaypoint().setTimeStamp(System.currentTimeMillis()/1000.0);
					sendMessageSynchronously(wp.getPositionWaypoint());
				}
				enterState(MissionPlanMode.NOT_USED);
			}
		});
		addState(MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD, new StateAdapter() {
			public void onEntry() throws Exception {
				tempMission.clear();
				tempSkylightMission.clear();
				receivedRulesOfSafety = false;
				receivedTakeoffLanding = false;
				sendAbortedStatusMessage();
				enterState(MissionPlanMode.NOT_USED);
			}
		});
		addState(MissionPlanMode.RECEIVE_MISSION, new StateAdapter() {
			@Override
			public void onEntry() throws Exception {
				expectedMissionItems = getCurrentStateData().getWaypointNumber();
			}
			@Override
			public void onStep() throws Exception {
				//send status message
				if(statusReportTimer.checkTrue()) {
					messageScheduler.sendScheduledMessage(MessageType.M900);
				}
			}
		});
		addState(MissionPlanMode.NOT_USED, new StateAdapter() {
			public void onEntry() throws Exception {
				tempMission.clear();
				tempSkylightMission.clear();
				receivedRulesOfSafety = false;
				receivedTakeoffLanding = false;
				actualMissionItems = 0;
			}
		});
	}

	public void sendAbortedStatusMessage() {
		MissionUploadDownloadStatus m = messagingService.resolveMessageForSending(MissionUploadDownloadStatus.class);
		m.setStatus(TransferStatus.ABORTED_REJECTED);
		m.setPercentComplete(100);
		messagingService.sendMessage(m);
	}
	
	protected void sendMessageSynchronously(Message m) throws IOException {
		//if async sender is in place, ignore it so that send progress will be related to real mission transfer (not only its schedule)
		if(messagingService instanceof AsyncSenderMessagingService) {
			((AsyncSenderMessagingService)messagingService).sendMessageSynchronously(m);
		} else {
			messagingService.sendMessage(m);
		}
	}
	
}
