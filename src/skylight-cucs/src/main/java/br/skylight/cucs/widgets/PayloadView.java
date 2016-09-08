package br.skylight.cucs.widgets;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.payload.MessageTargetedToStation;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleListener;

public abstract class PayloadView<P extends Serializable> extends View<P>implements VehicleListener {

	private JComboBox payload = null;
	
	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public UserService userService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;

	private Vehicle currentVehicle;
	private Payload currentPayload;
	
	public PayloadView(ViewExtensionPoint<P> viewExtensionPoint) {
		super(viewExtensionPoint);
		setTitleText(getBaseTitle());
	}
	
	@Override
	protected void onActivate() throws Exception {
		subscriberService.addVehicleListener(this);
	}

	public BitmappedLOI getGrantedLOIsForCurrentPayload() {
		if(currentPayload==null) {
			return new BitmappedLOI();
		} else {
			CUCSControl cc = currentPayload.getCucsControls().get(userService.getCurrentCucsId());
			if(cc!=null) {
				return cc.getGrantedLOIs();
			} else {
				return new BitmappedLOI();
			}
		}
	}

	public boolean cucsHasGrantedLOI(BitmappedLOI lois) {
		return getGrantedLOIsForCurrentPayload().matchAny(lois);
	}
	
	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		currentVehicle = av;
		if(type.equals(EventType.SELECTED)) {
			updatePayloadsCombo();
		}
	}
	
	private void updatePayloadsCombo() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		if(currentVehicle!=null) {
			for (Payload p : currentVehicle.getPayloads().values()) {
				m.addElement(p);
			}
		}
		getPayloadComboBox().setModel(m);
	}

	protected JComboBox getPayloadComboBox() {
		if (payload == null) {
			payload = new JComboBox();
			payload.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					Payload p = (Payload)payload.getSelectedItem();
					if(p!=null) {
						subscriberService.notifyPayloadEvent(p, EventType.SELECTED, null);
					}
				}
			});
		}
		return payload;
	}
	
	@Override
	public void onPayloadEvent(final Payload p, EventType type) {
		if(type.equals(EventType.SELECTED) || currentPayload==null || (type.equals(EventType.UPDATED) && p.equals(currentPayload))) {
			String label = p.getLabel() + " [No Control]";
			if(getGrantedLOIsForCurrentPayload().getLOIs().size()>0) {
				label = p.getLabel() + " " + Arrays.deepToString(getGrantedLOIsForCurrentPayload().getLOIs().toArray()) + "";
			}
			setTitleText(getBaseTitle() + " - " + label + "@" + vehicleControlService.getKnownVehicles().get(p.getVehicleID().getVehicleID()).getLabel());
			if(getPayloadComboBox().getSelectedItem()==null || !getPayloadComboBox().getSelectedItem().equals(p)) {
				getPayloadComboBox().setSelectedItem(p);
			}
			if(currentPayload==null) {
				updatePayloadsCombo();
			}
			currentPayload = p;
			updateGUI();
		}
	}
	
	public Payload getCurrentPayload() {
		return currentPayload;
	}
	
	public Vehicle getCurrentVehicle() {
		return currentVehicle;
	}
	
	protected abstract void updateGUI();
	protected abstract String getBaseTitle();
	
	public boolean isMessageFromCurrentPayload(Message message) {
		if(currentVehicle!=null && currentPayload!=null && message instanceof MessageTargetedToStation) {
			MessageTargetedToStation m = (MessageTargetedToStation)message;
			return (currentVehicle.getVehicleID().getVehicleID()==message.getVehicleID() && m.getTargetStations().isStation(currentPayload.getUniqueStationNumber()));
		}
		return false;
	}

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
	
}
