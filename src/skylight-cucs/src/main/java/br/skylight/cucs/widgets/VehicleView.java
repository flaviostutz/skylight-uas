package br.skylight.cucs.widgets;

import java.io.Serializable;
import java.util.Arrays;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleListener;

public abstract class VehicleView<P extends Serializable> extends View<P>implements VehicleListener {

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public UserService userService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;

	private Vehicle currentVehicle;
	
	public VehicleView(ViewExtensionPoint<P> viewExtensionPoint) {
		super(viewExtensionPoint);
		setTitleText(getBaseTitle());
	}
	
	@Override
	protected void onActivate() throws Exception {
		subscriberService.addVehicleListener(this);
		if(subscriberService.getLastSelectedVehicle()!=null) {
			onVehicleEvent(subscriberService.getLastSelectedVehicle(), EventType.SELECTED);
		}
		//TODO REFRESH GUI DEPOIS DA INICIALIZACAO
//		updateGUI();
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		currentVehicle = av;
		if(type.equals(EventType.SELECTED) || type.equals(EventType.UPDATED)) {
			setTitleText(getBaseTitle() + " - " + getVehicleControlLabel());
			updateGUI();
		}
	}
	
	public String getVehicleControlLabel() {
		if(currentVehicle!=null) {
			String label = currentVehicle.getLabel() + " [No Control]";
			if(getGrantedLOIsForCurrentVehicle().getLOIs().size()>0) {
				label = currentVehicle.getLabel() + " " + Arrays.deepToString(getGrantedLOIsForCurrentVehicle().getLOIs().toArray()) + "";
			}
			return label;
		} else {
			return "[No vehicle selected]";
		}
	}

	public BitmappedLOI getGrantedLOIsForCurrentVehicle() {
		if(currentVehicle==null) {
			return new BitmappedLOI();
		} else {
			CUCSControl cc = currentVehicle.getCucsControls().get(userService.getCurrentCucsId());
			if(cc!=null) {
				return cc.getGrantedLOIs();
			} else {
				return new BitmappedLOI();
			}
		}
	}

	public boolean cucsHasGrantedLOI(BitmappedLOI lois) {
		return getGrantedLOIsForCurrentVehicle().matchAny(lois);
	}
	
	protected <T extends Message> T getLastReceivedMessageForCurrentVehicle(MessageType messageType) {
		if(getCurrentVehicle()!=null) {
			return vehicleControlService.getLastReceivedMessage(getCurrentVehicle().getVehicleID().getVehicleID(), messageType);
		} else {
			return null;
		}
	}

	@Override
	public void onPayloadEvent(Payload p, EventType type) {
	}
	
	public Vehicle getCurrentVehicle() {
		return currentVehicle;
	}
	
	protected abstract void updateGUI();
	protected abstract String getBaseTitle();

	@Override
	protected void onStateUpdated() {
	}

	@Override
	protected void prepareState() {
	}
	
	@Override
	protected P instantiateState() {
		return null;
	}

	protected boolean isMessageFromCurrentVehicle(Message m) {
		return getCurrentVehicle()!=null && getCurrentVehicle().getVehicleID().getVehicleID()==m.getVehicleID();
	}

}
