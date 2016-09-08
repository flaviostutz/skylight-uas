package br.skylight.cucs.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import br.skylight.commons.EventType;
import br.skylight.commons.MessageConfiguration;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;

public class VehicleMessageRefreshButton extends FeedbackButton {

	private static final long serialVersionUID = 1L;
	
	private List<MessageType> messageTypes;
	private SubscriberService subscriberService;
	private MessagingService messagingService;
	
	private Vehicle vehicle;
	private Color originalColor;

	private JMenuItem enableItem;
	private JMenuItem disableItem;
	
	public VehicleMessageRefreshButton() {
	}

	public void setup(final SubscriberService subscriberService, final MessagingService messagingService, final MessageType ... messageTypes) {
		if(this.messagingService!=null) {
			throw new IllegalStateException("'setup' can be invoked only once");
		}
		if(messageTypes==null || subscriberService==null || messagingService==null) {
			throw new IllegalArgumentException("Neither messageTypes, subscriberService nor messagingService can be null");
		}
		this.messageTypes = Arrays.asList(messageTypes);
		this.subscriberService = subscriberService;
		this.messagingService = messagingService;

		//setup popup
		final JPopupMenu menu = new JPopupMenu();
		menu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				menu.removeAll();
				if(getVehicle()!=null) {
					boolean showDisable = false;
					List<MessageConfiguration> mcs = resolveMessageConfigurations();
					for (MessageConfiguration mc : mcs) {
						if(mc.getScheduledFrequency()>0F) {
							showDisable = true;
						}
					}
					menu.add(getEnableItem());
					if(showDisable) {
						menu.add(getDisableItem());
					}
					menu.updateUI();
				}
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		addMouseListener(new JPopupMenuMouseListener(menu));

		//request message on click
		addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				for (MessageType mt : messageTypes) {
					messagingService.sendRequestGenericInformation(mt, getVehicle().getVehicleID().getVehicleID());
				}
			}
		});
	}

	private JMenuItem getEnableItem() {
		if(enableItem==null) {
			enableItem = new JMenuItem();
			enableItem.setText("Schedule auto refresh...");
			enableItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//request frequency for each message type
					List<MessageConfiguration> mcs = resolveMessageConfigurations();
					for (MessageConfiguration mc : mcs) {
						Double r = NumberInputDialog.showInputDialog(getThis(), mc.getMessageType().toString() + " frequency (Hz):", mc.getScheduledFrequency(), 0, Double.MAX_VALUE, 1, 0, 2);
						if(r!=null) {
							mc.setScheduledFrequency(r.floatValue());
							mc.sendConfigurationToVehicle(getVehicle().getVehicleID().getVehicleID(), messagingService);
						} else {
							break;
						}
					}
					subscriberService.notifyVehicleEvent(getVehicle(), EventType.UPDATED, null);
				}
			});
		}
		return enableItem;
	}

	private JMenuItem getDisableItem() {
		if(disableItem==null) {
			disableItem = new JMenuItem();
			disableItem.setText("Disable auto refresh");
			disableItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<MessageConfiguration> mcs = resolveMessageConfigurations();
					for (MessageConfiguration mc : mcs) {
						mc.setScheduledFrequency(0);
						mc.sendConfigurationToVehicle(getVehicle().getVehicleID().getVehicleID(), messagingService);
					}
					subscriberService.notifyVehicleEvent(getVehicle(), EventType.UPDATED, null);
				}
			});
		}
		return disableItem;
	}
	
	private VehicleMessageRefreshButton getThis() {
		return this;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
		setEnabled(vehicle!=null);
		updateUI();
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public List<MessageConfiguration> resolveMessageConfigurations() {
		List<MessageConfiguration> messages = new ArrayList<MessageConfiguration>();
		
		for (MessageType mt : messageTypes) {
			
			//find and existing message configuration
			MessageConfiguration nm = null;
			for (MessageConfiguration mc : vehicle.getMessageConfigurations()) {
				if(mc.getMessageType().equals(mt)) {
					nm = mc;
				}
			}
			
			//create a new message configuration if not found
			if(nm==null) {
				nm = new MessageConfiguration();
				nm.setMessageType(mt);
				nm.setScheduledFrequency(0);
				vehicle.getMessageConfigurations().add(nm);
			}
			
			messages.add(nm);
		}
		
		return messages;
	}	
	
	@Override
	public void updateUI() {
		if(vehicle!=null) {
			int numberEnabled = 0;
			for (MessageConfiguration mc : vehicle.getMessageConfigurations()) {
				if(messageTypes.contains(mc.getMessageType())) {
					if(mc.getScheduledFrequency()>0) {
						numberEnabled++;
					}
					break;
				}
			}
			
			if(numberEnabled==0) {
				setBackground(originalColor);
			} else if(numberEnabled<messageTypes.size()) {
				setBackground(Color.YELLOW);
			} else {
				setBackground(Color.GREEN);
			}
		}
		
		super.updateUI();
	}
	
}
