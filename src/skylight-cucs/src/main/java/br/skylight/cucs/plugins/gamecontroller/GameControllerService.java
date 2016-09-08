package br.skylight.cucs.plugins.gamecontroller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Controller.Type;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleSteeringListener;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=GameControllerService.class)
public class GameControllerService extends ThreadWorker implements VehicleSteeringListener {

	private static final Logger logger = Logger.getLogger(GameControllerService.class.getName());

	public static final int SPEED_CONTROL_ID = 101;
	public static final int ALTITUDE_CONTROL_ID = 102;
	public static final int COURSE_HEADING_CONTROL_ID = 103;
	public static final int ROLL_HEADING_CONTROL_ID = 104;

	public static final int PAYLOAD_AZIMUTH_CONTROL_ID = 301;
	public static final int PAYLOAD_ELEVATION_CONTROL_ID = 302;
	public static final int PAYLOAD_FOV_CONTROL_ID = 303;
	
	private ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
	private List<Controller> controllers = new ArrayList<Controller>();
	private List<String> processed = new ArrayList<String>();
	private ControllerListener controllerListener;
	
	private ControllerBindingProfile currentControllerProfile;
	private ArrayList<ControllerBindingProfile> controllerProfiles = new ArrayList<ControllerBindingProfile>();
	private List<ControllerBinding> activeBindings = new ArrayList<ControllerBinding>();
	private List<ControllerBinding> customBindings = new ArrayList<ControllerBinding>();
	private List<GameControllerServiceListener> serviceListeners = new ArrayList<GameControllerServiceListener>();
	
	private List<GameControllerComponentListener> listeners = new CopyOnWriteArrayList<GameControllerComponentListener>();

    private Map<String,Float> lastNotificatedValues = new HashMap<String,Float>();
    
    private List<ControllerBindingDefinition> bindingDefinitions = new ArrayList<ControllerBindingDefinition>();

    @ServiceInjection
    public SubscriberService subscriberService;
    
    @ServiceInjection
    public PluginManager pluginManager;
    
    @ServiceInjection
    public StorageService storageService;
    
    @ServiceInjection
    public VehicleControlService vehicleControlService;
    
    @ExtensionPointsInjection
    public List<BindingDefinitionsExtensionPoint> bindingExtensionPoints;
    
    public GameControllerService() {
    	super(15, 200, -1);
    }
    
	@Override
	public void onActivate() throws Exception {
    	refreshControllers();
    	controllerListener = new ControllerListener() {
			public void controllerRemoved(ControllerEvent arg0) {
				refreshControllers();
			}
			public void controllerAdded(ControllerEvent arg0) {
				refreshControllers();
			}
		};
	    ce.addControllerListener(controllerListener);
	    
	    subscriberService.addVehicleSteeringListener(this);
	    
	    pluginManager.executeAfterStartup(new Runnable() {
	    	public void run() {
	    		//gather all extension definitions
	    		for (BindingDefinitionsExtensionPoint ep : bindingExtensionPoints) {
	    			bindingDefinitions.addAll(ep.getControllerBindingDefinitions());
				}
	    		
	    	    //load saved controller bindings and set them to extension points
	    		FileInputStream fis = null;
    		    try {
					File f = storageService.getFile("controller-profiles.dat");
		    	    if(f.exists()) {
		    		    fis = new FileInputStream(f);
		    		    DataInputStream dis = new DataInputStream(fis);
	    		    	IOHelper.readArrayList(dis, ControllerBindingProfile.class, controllerProfiles);
	    		    	if(dis.readBoolean()) {
	    		    		ControllerBindingProfile p = IOHelper.readState(ControllerBindingProfile.class, dis);
	    		    		selectControllerBindingProfile(p);
	    		    	}
		    	    }
    		    } catch (Exception e) {
    		    	logger.warning("Problem loading controller bindinds. e=" + e.toString());
    		    } finally {
    		    	IOHelper.close(fis);
    		    }

		    	//create a default profile if no profile could be found
		    	if(controllerProfiles.size()==0) {
		    		createControllerBindingProfile("Default binding profile");
		    	}
    		    
    		    //select initial profile
				if(currentControllerProfile==null && controllerProfiles.size()>0) {
					selectControllerBindingProfile(controllerProfiles.get(0));
				}
				
				for (GameControllerServiceListener sl : serviceListeners) {
					sl.onGameControllerServiceStartup();
				}
	    	}
	    });
    }
	
	@Override
	public void onDeactivate() throws Exception {
	    ce.removeControllerListener(controllerListener);
	    //save controller bindings
	    File f = storageService.resolveFile("controller-profiles.dat");
	    FileOutputStream fos = new FileOutputStream(f);
	    DataOutputStream dos = new DataOutputStream(fos);
	    try {
	    	IOHelper.writeArrayList(dos, controllerProfiles);
	    	dos.writeBoolean(currentControllerProfile!=null);
	    	if(currentControllerProfile!=null) {
	    		IOHelper.writeState(currentControllerProfile, dos);
	    	}
	    } catch (Exception e) {
	    	logger.warning("Problem saving controller bindinds. e=" + e.toString());
	    } finally {
	    	IOHelper.close(fos);
	    }
	}

	public void selectControllerBindingProfile(ControllerBindingProfile profile) {
		this.currentControllerProfile = null;//avoid controller events while selecting new profile
		
		prepareProfileBindings(profile);
		
		//define current binding
		activeBindings.clear();
		activeBindings.addAll(profile.getBindings());
		activeBindings.addAll(customBindings);
		
		this.currentControllerProfile = profile;
	}
	
	private void prepareProfileBindings(ControllerBindingProfile profile) {
		List<ControllerBinding> remove = new ArrayList<ControllerBinding>();
		for (ControllerBinding cb : profile.getBindings()) {
			for (ControllerBindingDefinition ep : bindingDefinitions) {
				if(ep.getBindingDefinitionId()==cb.getDefinitionId()) {
					cb.setControllerBindingDefinition(ep);
					ep.getValueResolver().setInverse(cb.isInverse());
					ep.getValueResolver().setIncremental(cb.isIncremental());
					ep.getValueResolver().setExponential(cb.isExponential());
					ep.getValueResolver().setTimeAutoTriggerWhileTraveling(cb.getTimeAutoTriggerWhileTraveling());
				}
			}
			//remove bindings whose extension point was not found
			if(cb.getControllerBindingDefinition()==null) {
				remove.add(cb);
			}
		}
		//remove orphan bindings
		profile.getBindings().removeAll(remove);
	}

	public ControllerBindingProfile getCurrentControllerProfile() {
		return currentControllerProfile;
	}
	
	public ArrayList<ControllerBindingProfile> getControllerProfiles() {
		return controllerProfiles;
	}
	
	@Override
	public void step() {
		//indicates to refresh the list of connected controllers
		boolean needToRefresh = false;
		ValueResolver vr;
		
		//get queued events for each controller and broadcast them to listeners
		for (Controller controller : controllers) {
			if(currentControllerProfile==null ||
				controller.getType().equals(Type.MOUSE) ||
				controller.getType().equals(Type.KEYBOARD)) {
				continue;
			}

			try {
				//poll controller
				if(controller.poll()) {
					//notify listeners
					processed.clear();
					Component[] comps = controller.getComponents();
					for (int i = 0; i < comps.length; i++) {
						Component component = comps[i];
						if(validComponent(component) && !processed.contains(component.getName())) {
							Float value = component.getPollData();
							String key = controller.getName()+"#"+component.getName();
							//verify if value has changed
							if(!value.equals(lastNotificatedValues.get(key))) {
								//notify listeners
								for (GameControllerComponentListener l : listeners) {
									l.onComponentValueChanged(controller, component, value, value);
								}
								//look for bindings that match this controller/component
								for (ControllerBinding cb : activeBindings) {
//									System.out.println(cb.getDefinitionId() + " " + cb.isActive());
									if(cb.isActive()) {
										if(cb.getControllerName().equals(controller.getName()) && cb.getComponentName().equals(component.getIdentifier().getName())) {
											vr = cb.getControllerBindingDefinition().getValueResolver();
											vr.updateControllerValue(value);
											if(vr.shouldTriggerChangeEvent()) {
												cb.getControllerBindingDefinition().onComponentValueChanged(cb, vr.getResolvedValue(), subscriberService.getLastSelectedVehicle());
											}
										}
									}
								}
								lastNotificatedValues.put(key, value);
							}
							//avoid processing the same controller twice (solving a bug in a specific controller)
							processed.add(component.getName());
						}
						//verify if any binding wants to throw an arbitrary change event
						for (ControllerBinding cb : activeBindings) {
							if(cb.isActive()) {
								vr = cb.getControllerBindingDefinition().getValueResolver();
								if(vr.shouldTriggerAsyncChangeEvent()) {
									cb.getControllerBindingDefinition().onComponentValueChanged(cb, vr.getResolvedValue(), subscriberService.getLastSelectedVehicle());
								}
							}
						}
					}

				} else {
					needToRefresh = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				needToRefresh = true;
			}
		}
		
		if(needToRefresh) {
			refreshControllers();
		}
	}
	
    private boolean validComponent(Component component) {
    	if(component.isRelative()) return false;
    	return true;
	}

    private void refreshControllers() {
	    Controller[] cs = ce.getControllers();
	    controllers = Arrays.asList(cs);
	}
	
    public ControllerBindingProfile createControllerBindingProfile(String name) {
    	ControllerBindingProfile p = new ControllerBindingProfile();
    	p.setProfileName(name);
    	controllerProfiles.add(p);
    	return p;
    }
    
	public void showControllerBindingDialog(int bindingDefinitionId) {
		GameControllerComponentSelector g = new GameControllerComponentSelector(null, this);
		for (ControllerBindingDefinition ep : bindingDefinitions) {
			if(ep.getBindingDefinitionId()==bindingDefinitionId) {
				if(currentControllerProfile!=null) {
					ControllerBinding cb = currentControllerProfile.resolveControllerBinding(bindingDefinitionId);
					if(!activeBindings.contains(cb)) {
						activeBindings.add(cb);
						prepareProfileBindings(currentControllerProfile);
					}
					g.showDialog(ep, cb);
					prepareProfileBindings(currentControllerProfile);
				}
			}
		}
	}

	public void addListener(GameControllerComponentListener l) {
		listeners.add(l);
	}
	
	public void removeListener(GameControllerComponentListener l) {
		listeners.remove(l);
	}

	public JPopupMenu createSetupControlsMenu(int ... bindingDefinitionIds) {
		JPopupMenu pm = new JPopupMenu();
		for (final int bid : bindingDefinitionIds) {
			ControllerBindingDefinition bd = getBindingDefinition(bid);
			if(bd!=null) {
				JMenuItem mi = new JMenuItem(bd.getName());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showControllerBindingDialog(bid);
					}
				});
				pm.add(mi);
			} else {
				logger.warning("Binding definition " + bid + " was not found and won't be available in menu. Ignoring.");
			}
		}
		return pm;
	}
	
	private ControllerBindingDefinition getBindingDefinition(int bindingDefinitionId) {
		for (ControllerBindingDefinition bd : bindingDefinitions) {
			if(bd.getBindingDefinitionId()==bindingDefinitionId) {
				return bd;
			}
		}
		return null;
	}

	public void addCustomBinding(ControllerBinding controllerBinding) {
		if(controllerBinding==null) throw new IllegalArgumentException("'controllerBinding' cannot be null");
		if(controllerBinding.getControllerBindingDefinition()==null) throw new IllegalArgumentException("'controllerBinding.controllerBindingDefinition' cannot be null");
		customBindings.add(controllerBinding);
	}
	
	public void removeCustomBinding(ControllerBinding cb) {
		customBindings.remove(cb);
	}
	
	public void addGameControllerServiceListener(GameControllerServiceListener l) {
		serviceListeners.add(l);
	}

	@Override
	public void onSteeringEvent(Vehicle vehicle) {
		for (ControllerBinding cb : activeBindings) {
			VehicleSteeringCommand m = vehicle.getVehicleSteeringCommand();
			if(cb.getControllerBindingDefinition().getBindingDefinitionId()==ALTITUDE_CONTROL_ID) {
				cb.getControllerBindingDefinition().getValueResolver().setCurrentValue(m.getCommandedAltitude());
			} else if(cb.getControllerBindingDefinition().getBindingDefinitionId()==COURSE_HEADING_CONTROL_ID) {
				cb.getControllerBindingDefinition().getValueResolver().setCurrentValue(m.getCommandedCourse());
			} else if(cb.getControllerBindingDefinition().getBindingDefinitionId()==ROLL_HEADING_CONTROL_ID) {
				cb.getControllerBindingDefinition().getValueResolver().setCurrentValue(m.getCommandedHeading());
			} else if(cb.getControllerBindingDefinition().getBindingDefinitionId()==SPEED_CONTROL_ID) {
				cb.getControllerBindingDefinition().getValueResolver().setCurrentValue(m.getCommandedSpeed());
			}
		}
	}

	public void reloadSteerings() {
		for(Vehicle v : vehicleControlService.getKnownVehicles().values()) {
			onSteeringEvent(v);
		}
	}

}
