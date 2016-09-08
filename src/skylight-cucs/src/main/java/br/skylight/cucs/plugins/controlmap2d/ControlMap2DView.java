package br.skylight.cucs.plugins.controlmap2d;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.plugins.subscriber.MissionListener;
import br.skylight.cucs.widgets.VehicleView;

public class ControlMap2DView extends VehicleView<ControlMap2DState> implements MissionListener {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="-27,53"
	private JPanel jPanel = null;
	private MapKit mapKit = null;
	
//	private boolean addMissionBoundariesOnClick;

	@ServiceInjection
	public StorageService storageService;
	
	@ServiceInjection
	public PluginManager pluginManager;

	@ExtensionPointsInjection
	public List<ControlMapExtensionPoint> controlMapExtensionPoints;  //  @jve:decl-index=0:

	private JPopupMenu contextMenu = null;  //  @jve:decl-index=0:visual-constraint="251,67"
	
	public ControlMap2DView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
		setTitleIcon(new ImageIcon(ControlMap2DView.class.getResource("/br/skylight/cucs/images/map.gif")));
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		pluginManager.executeAfterStartup(new Runnable() {
			public void run() {
		        //connect control map to extensions
				for (ControlMapExtensionPoint ep : controlMapExtensionPoints) {
					ep.setMapKit(getMapKit());
				}
			}
		});
	}
	
	@Override
	protected void updateGUI() {
		getMapKit().updateUI();
		rebuildToolBar();
	}

	private void rebuildToolBar() {
		getJPanel().removeAll();
		for (ControlMapExtensionPoint ep : controlMapExtensionPoints) {
			if(ep.isCompatibleWithVehicle(getCurrentVehicle())) {
				if(ep.getToolComponent()!=null) {
					getJPanel().add(ep.getToolComponent());
				}
			}
		}
		getJPanel().updateUI();
	}

	protected JPanel getContents() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.weightx = 1.0;
			gridBagConstraints51.gridy = 1;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.weightx = 1.0;
			gridBagConstraints41.weighty = 1.0;
			gridBagConstraints41.fill = GridBagConstraints.BOTH;
			gridBagConstraints41.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setSize(new Dimension(247, 198));
			jContentPane.add(getMapKit(), gridBagConstraints41);
			jContentPane.add(getJPanel(), gridBagConstraints51);
		}
		return jContentPane;
	}

	private MapKit getMapKit() {
		if (mapKit == null) {
			mapKit = new MapKit();
			mapKit.setMiniMapVisible(false);
			mapKit.setZoomButtonsVisible(false);
			mapKit.setupMapTileFactory(storageService.resolveDir("mapkit-cache"));
	        mapKit.getMainMap().addMouseListener(new MouseAdapter() {
	        	@Override
	        	public void mousePressed(MouseEvent e) {
	        		if(e.getButton()==MouseEvent.BUTTON3) {
		        		getContextMenu().removeAll();
		        		for (ControlMapExtensionPoint ep : controlMapExtensionPoints) {
		        			if(ep.isCompatibleWithVehicle(getCurrentVehicle())) {
			        			List<JMenuItem> mis = ep.prepareContextMenuItems(getMapKit().getMainMap().convertPointToGeoPosition(new Point2D.Float(e.getX(), e.getY())));
			        			if(mis!=null) {
									for (JMenuItem mi : mis) {
										getContextMenu().add(mi);
									}
			        			}
		        			}
						}
		        		if(getContextMenu().getComponentCount()==0) {
		        			JMenuItem mi = new JMenuItem("No action available");
		        			mi.setFont(new Font(Font.DIALOG, Font.ITALIC, 12));
		        			mi.setEnabled(false);
		        			getContextMenu().add(mi);
		        		}
		        		getContextMenu().show(e.getComponent(), e.getX(), e.getY());
	        		}
	        	}
			});
		}
		return mapKit;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		}
		return jPanel;
	}

	public Coordinates getCenterMapCoordinates() {
		return new Coordinates(getMapKit().getMainMap().getCenterPosition().getLatitude(), getMapKit().getMainMap().getCenterPosition().getLongitude(), 0);
	}

//	public void addPlacemark(String label, float latitude, float longitude) {
//		MapElementGroup<MapElement> group = getMapKit().resolveElementGroup(GROUP_PLACEMARKS, placemarkPainterFactory);
//		MapElement me = new MapElement(getMapKit().getMainMap(), group);
//		me.setPosition(new GeoPosition(latitude, longitude));
//		me.setLabel(label);
//		me.setEditable(false);
//		group.getElements().add(me);
//	}

//	public void initiateEditMissionBoundaries() {
//		MapElementGroup<MapElement> group = getMapKit().resolveElementGroup(GROUP_MISSION_BOUNDARIES, missionBoundariesPainterFactory);
//		getMapKit().addElementOnClick(new MapElementFactory<MapElement>(getMapKit().getMainMap(), group) {
//			@Override
//			public MapElement createInstance() {
//				Coordinates gp = new Coordinates(0,0,0);
//				mission.getMissionBoundaries().add(gp);
//				return new RegionMapElement(getMap(), getGroup(), gp);
//			}
//		});
//	}

	
	@Override
	protected ControlMap2DState instantiateState() {
		return new ControlMap2DState();
	}

	@Override
	protected void onStateUpdated() {
		getMapKit().setAddressLocation(new GeoPosition(getState().getLastLatitude(), getState().getLastLongitude()));
		getMapKit().setZoom(getState().getLastZoomLevel());
	}

	@Override
	protected void prepareState() {
		GeoPosition pos = getMapKit().getMainMap().getCenterPosition();
		getState().setLastLatitude(pos.getLatitude());
		getState().setLastLongitude(pos.getLongitude());
		getState().setLastZoomLevel(getMapKit().getMainMap().getZoom());
	}

	@Override
	protected String getBaseTitle() {
		return "Control Map 2D";
	}

	/**
	 * This method initializes contextMenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getContextMenu() {
		if (contextMenu == null) {
			contextMenu = new JPopupMenu();
		}
		return contextMenu;
	}

	@Override
	public void onMissionEvent(Mission mission, EventType type) {
		if(!mission.getVehicle().equals(getCurrentVehicle())) {
			onVehicleEvent(mission.getVehicle(), EventType.SELECTED);
		}
	}

	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type) {
		if(!mission.getVehicle().equals(getCurrentVehicle())) {
			onVehicleEvent(mission.getVehicle(), EventType.SELECTED);
		}
	}
	
}
