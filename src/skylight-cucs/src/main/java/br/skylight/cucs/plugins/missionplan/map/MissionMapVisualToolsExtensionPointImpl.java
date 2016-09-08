package br.skylight.cucs.plugins.missionplan.map;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.Path;
import br.skylight.commons.Placemark;
import br.skylight.commons.Region;
import br.skylight.commons.TextAnnotation;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.PathPainter;
import br.skylight.cucs.mapkit.painters.PlacemarkPainter;
import br.skylight.cucs.mapkit.painters.PolygonPainter;
import br.skylight.cucs.mapkit.painters.TextAnnotationPainter;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RegionMapElement;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RegionMapElementGroup;
import br.skylight.cucs.plugins.subscriber.MissionListener;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class MissionMapVisualToolsExtensionPointImpl extends ControlMapExtensionPoint implements MissionListener {

	private static final String PREFIX_MISSION = "missionTools-";  //  @jve:decl-index=0:
	private static final String GROUP_TEXT = PREFIX_MISSION+"texts";  //  @jve:decl-index=0:
	private static final String GROUP_PLACEMARKS = PREFIX_MISSION+"placemarks";
	private static final String PREFIX_POLYGON = PREFIX_MISSION+"polygon-";
	private static final String PREFIX_PATH = PREFIX_MISSION+"path-";

	private JButton textButton = null;
	private int counter = 1;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	private JButton placemarkButton = null;  //  @jve:decl-index=0:visual-constraint="13,65"
	private JButton pathButton = null;  //  @jve:decl-index=0:visual-constraint="87,54"
	private JButton polygonButton = null;  //  @jve:decl-index=0:visual-constraint="141,18"
	
	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMissionListener(this);
	}
	
	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
        getMapKit().addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				//handle only visual type of elements
				if(mapElement instanceof RegionMapElement) {
					RegionMapElement r = (RegionMapElement)mapElement;
					//process only events for visual tool regions (authorized/prohibited regions are RegionMapElements too, so avoid confusion with other plugin)
					if(r.getMission().getMissionAnnotations().getPolygons().contains(r.getRegion())) {
						MissionMapElement me = (MissionMapElement)mapElement;
						subscriberService.notifyMissionEvent(me.getMission(), EventType.UPDATED, getThis());
					}
				}
				if(mapElement instanceof PathMapElement || mapElement instanceof PlacemarkMapElement || mapElement instanceof TextMapElement) {
					MissionMapElement me = (MissionMapElement)mapElement;
					subscriberService.notifyMissionEvent(me.getMission(), EventType.UPDATED, getThis());
				}
			}
		});
        
        //add tool components
        getMapKit().addToolComponent(getTextButton());
        getMapKit().addToolComponent(getPlacemarkButton());
        getMapKit().addToolComponent(getPathButton());
        getMapKit().addToolComponent(getPolygonButton());
        updateButtonsState();
	}

	protected MissionMapVisualToolsExtensionPointImpl getThis() {
		return this;
	}

	private void reloadAllMissions() {
		if(getMapKit()!=null) {
			getMapKit().removeMapElementGroupsByIdPrefix(PREFIX_MISSION);
			
			//draw mission visual artifacts
			counter = 1;
			for (Vehicle v : vehicleControlService.getKnownVehicles().values()) {
				if(v.getMission()!=null) {
					
					//create text annotation elements
					for (TextAnnotation ta : v.getMission().getMissionAnnotations().getTexts()) {
						TextMapElement t = new TextMapElement();
						t.setMission(v.getMission());
						t.setTextAnnotation(ta);
						resolveMissionTextGroup(v.getMission()).addElement(t, -1, TextMapElement.class);
					}

					//create placemark annotation elements
					for (Placemark ta : v.getMission().getMissionAnnotations().getPlacemarks()) {
						PlacemarkMapElement t = new PlacemarkMapElement();
						t.setMission(v.getMission());
						t.setPlacemark(ta);
						resolveMissionPlacemarksGroup(v.getMission()).addElement(t, -1, PlacemarkMapElement.class);
					}

					//create paths annotation elements
					for (Path ta : v.getMission().getMissionAnnotations().getPaths()) {
						MapElementGroup<PathMapElement> g = resolveMissionPathsGroup(v.getMission(), ta, counter++);
						for (Coordinates p : ta.getPoints()) {
							PathMapElement t = new PathMapElement();
							t.setMission(v.getMission());
							t.setPath(ta);
							t.setPoint(p);
							g.addElement(t, -1, PathMapElement.class);
						}
					}
					
					//create polygons annotation elements
					for (Region ta : v.getMission().getMissionAnnotations().getPolygons()) {
						MapElementGroup<RegionMapElement> g = resolveMissionPolygonsGroup(v.getMission(), ta, counter++);
						for (Coordinates p : ta.getPoints()) {
							RegionMapElement t = new RegionMapElement(v.getMission(), ta);
							t.setPoint(p);
							g.addElement(t, -1, RegionMapElement.class);
						}
					}
					
				}
			}
			getMapKit().updateUI();
		}
	}
	
	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}

	@Override
	public void onMissionEvent(Mission mission, EventType type) {
		updateButtonsState();
		reloadAllMissions();
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		super.onVehicleEvent(av, type);
		updateButtonsState();
		reloadAllMissions();
	}
	
	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type) {
		updateButtonsState();
		reloadAllMissions();
	}

	private void updateButtonsState() {
		boolean buttonsEnabled = false;
		String message = null;
		if(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null) {
			buttonsEnabled = true;
		} else {
			message = "No mission selected";
		}
		
        getTextButton().setEnabled(buttonsEnabled);
        getTextButton().setToolTipText(message);

        getPlacemarkButton().setEnabled(buttonsEnabled);
        getPlacemarkButton().setToolTipText(message);

        getPathButton().setEnabled(buttonsEnabled);
        getPathButton().setToolTipText(message);

        getPolygonButton().setEnabled(buttonsEnabled);
        getPolygonButton().setToolTipText(message);
	}

	private MapElementGroup<TextMapElement> resolveMissionTextGroup(final Mission mission) {
		String groupId = GROUP_TEXT+mission.getMissionID();
		MapElementGroup<TextMapElement> g = getMapKit().getMapElementGroup(groupId, TextMapElement.class);
		if(g==null) {
			g = new MapElementGroup<TextMapElement>(getMapKit(),
					"Annotations for mission " + mission.getMissionID(), 0,
					new MapElementBridge<TextMapElement>() {
						@Override
						public TextMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<TextMapElement> group) {
							TextMapElement t = new TextMapElement();
							t.setMission(mission);
							t.setTextAnnotation(new TextAnnotation());
							String label = JOptionPane.showInputDialog("Enter text:");
							if(label!=null) {
								t.setLabel(label);
							}
							mission.getMissionAnnotations().getTexts().add(t.getTextAnnotation());
							return t;
						};
						@Override
						public void onElementDeleted(TextMapElement element, MapElementGroup<TextMapElement> group) {
							mission.getMissionAnnotations().getTexts().remove(element.getTextAnnotation());
						}
					}, new TextAnnotationPainter()
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}

	private MapElementGroup<PlacemarkMapElement> resolveMissionPlacemarksGroup(final Mission mission) {
		String groupId = GROUP_PLACEMARKS+mission.getMissionID();
		MapElementGroup<PlacemarkMapElement> g = getMapKit().getMapElementGroup(groupId, PlacemarkMapElement.class);
		if(g==null) {
			g = new MapElementGroup<PlacemarkMapElement>(getMapKit(),
					"Placemarks for mission " + mission.getMissionID(), 0,
					new MapElementBridge<PlacemarkMapElement>() {
						@Override
						public PlacemarkMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<PlacemarkMapElement> group) {
							PlacemarkMapElement t = new PlacemarkMapElement();
							t.setMission(mission);
							t.setPlacemark(new Placemark());
							mission.getMissionAnnotations().getPlacemarks().add(t.getPlacemark());
							return t;
						};
						@Override
						public void onElementDeleted(PlacemarkMapElement element, MapElementGroup<PlacemarkMapElement> group) {
							mission.getMissionAnnotations().getPlacemarks().remove(element.getPlacemark());
						}
					}, new PlacemarkPainter()
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}

	private MapElementGroup<PathMapElement> resolveMissionPathsGroup(final Mission mission, final Path path, int pathId) {
		String groupId = PREFIX_PATH+mission.getMissionID()+pathId;
		MapElementGroup<PathMapElement> g = getMapKit().getMapElementGroup(groupId, PathMapElement.class);
		if(g==null) {
			g = new MapElementGroup<PathMapElement>(getMapKit(),
					"Path #"+ pathId +" for mission " + mission.getMissionID(), 0,
					new MapElementBridge<PathMapElement>() {
						@Override
						public PathMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<PathMapElement> group) {
							PathMapElement t = new PathMapElement();
							t.setMission(mission);
							t.setPath(path);
							path.addPoint(elementIndex, new Coordinates(position.getLatitude(), position.getLongitude(), 0));
							return t;
						};
						@Override
						public void onElementDeleted(PathMapElement element, MapElementGroup<PathMapElement> group) {
							mission.getMissionAnnotations().getPaths().remove(element.getPath());
						}
					}, new PathPainter<PathMapElement>()
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	private MapElementGroup<RegionMapElement> resolveMissionPolygonsGroup(final Mission mission, final Region region, int regionId) {
		String groupId = PREFIX_POLYGON+mission.getMissionID()+regionId;
		MapElementGroup<RegionMapElement> g = getMapKit().getMapElementGroup(groupId, RegionMapElement.class);
		if(g==null) {
			PolygonPainter<RegionMapElement> pp = new PolygonPainter<RegionMapElement>();
			pp.setPolygonContourColor(new Color(180,180,180,180));
			
			g = new RegionMapElementGroup(getMapKit(), "Polygon #"+ regionId +" for mission " + mission.getMissionID(), 5, mission, region, pp);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	private JButton getTextButton() {
		if (textButton == null) {
			textButton = new JButton();
			textButton.setToolTipText("Create a text annotation");
			textButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/text.gif")));
			textButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object gid = resolveMissionTextGroup(getCurrentVehicle().getMission()).getId();
					getMapKit().addNewElementOnClick(gid);
				}
			});
			setupButton(textButton);
		}
		return textButton;
	}

	/**
	 * This method initializes placemarkButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPlacemarkButton() {
		if (placemarkButton == null) {
			placemarkButton = new JButton();
			placemarkButton.setToolTipText("Create a placemark");
			placemarkButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/placemark.gif")));
			placemarkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object gid = resolveMissionPlacemarksGroup(getCurrentVehicle().getMission()).getId();
					getMapKit().addNewElementOnClick(gid);
				}
			});
			setupButton(placemarkButton);
		}
		return placemarkButton;
	}

	/**
	 * This method initializes pathButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPathButton() {
		if (pathButton == null) {
			pathButton = new JButton();
			pathButton.setToolTipText("Create a path");
			pathButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/path.gif")));
			pathButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Path path = new Path();
					getCurrentVehicle().getMission().getMissionAnnotations().getPaths().add(path);
					Object gid = resolveMissionPathsGroup(getCurrentVehicle().getMission(), path, counter++).getId();
					getMapKit().addNewElementOnClick(gid);
				}
			});
			setupButton(pathButton);
		}
		return pathButton;
	}

	/**
	 * This method initializes polygonButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPolygonButton() {
		if (polygonButton == null) {
			polygonButton = new JButton();
			polygonButton.setToolTipText("Create a polygon");
			polygonButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/polygon.gif")));
			polygonButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Region region = new Region();
					getCurrentVehicle().getMission().getMissionAnnotations().getPolygons().add(region);
					Object gid = resolveMissionPolygonsGroup(getCurrentVehicle().getMission(), region, counter++).getId();
					getMapKit().addNewElementOnClick(gid);
				}
			});
			setupButton(polygonButton);
		}
		return polygonButton;
	}

	private void setupButton(JButton button) {
		button.setMargin(new java.awt.Insets(2, 2, 2, 2));
		button.setMaximumSize(new java.awt.Dimension(20, 20));
		button.setMinimumSize(new java.awt.Dimension(20, 20));
		button.setPreferredSize(new java.awt.Dimension(20, 20));
	}
	
}
