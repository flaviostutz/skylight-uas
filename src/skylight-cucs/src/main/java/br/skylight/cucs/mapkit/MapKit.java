package br.skylight.cucs.mapkit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.cucs.mapkit.painters.LayerPainter;
import br.skylight.cucs.mapkit.painters.PathRulerPainter;
import br.skylight.cucs.plugins.missionplan.map.PathMapElement;

public class MapKit extends JXMapKit {

	private static final long serialVersionUID = 1L;
	
	private static final String GROUP_RULER = "default-ruler";  //  @jve:decl-index=0:

	//new elements insertion control
	private Object newElementGroupIdToInsertOnClick = null;  //  @jve:decl-index=0:
	
	private List<JComponent> toolComponents = new ArrayList<JComponent>();
	protected List<MapActionListener<MapElement>> listeners = new CopyOnWriteArrayList<MapActionListener<MapElement>>();  //  @jve:decl-index=0:
    private NumberFormat numberFormatter;  //  @jve:decl-index=0:

    private Map<Object,MapElementGroup<? extends MapElement>> elementGroups = new HashMap<Object,MapElementGroup<? extends MapElement>>();  //  @jve:decl-index=0:
	private List<LayerPainter<JXMapViewer>> customPainters = new ArrayList<LayerPainter<JXMapViewer>>();  //  @jve:decl-index=0:
//    private Map<MapElementGroup<MapElement>, MapElementFactory<MapElement>> groupFactories = new HashMap<MapElementGroup<MapElement>, MapElementFactory<MapElement>>();
    
    // all map elements
    private MapElement lastSelected = null;  //  @jve:decl-index=0:
    private MapElement lastSelectedButton3 = null;  //  @jve:decl-index=0:
    
    private GeoPosition currentPosition;
    private boolean lastSelectedMoved;
    private boolean clearSelectionOnMapClick = true;
    
	private JButton googleMapsButton = null;  //  @jve:decl-index=0:visual-constraint="390,11"
	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="313,15"

	private JButton addElementToGroup = null;
	private JButton cancelCurrentOperation = null;
	private JButton rulerButton = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public MapKit() {
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        //GOOGLE MAPS
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        getMainMap().add(getGoogleMapsButton(), gridBagConstraints);
        
		//TOOLBAR
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 2;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints4.weightx = 0;
        gridBagConstraints4.weighty = 1;
        gridBagConstraints4.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints4.fill = GridBagConstraints.NONE;
        getMainMap().add(getJPanel(), gridBagConstraints4);
        
        this.setSize(new Dimension(274, 237));
        this.setMiniMapVisible(false);
        this.numberFormatter = DecimalFormat.getInstance();
        numberFormatter.setMaximumFractionDigits(6);
        numberFormatter.setMinimumFractionDigits(6);

		//info painter
        LayerPainter<JXMapViewer> infoPainter = new LayerPainter<JXMapViewer>() {
			public void paint(Graphics2D g, JXMapViewer arg1, int arg2, int arg3) {
				if(currentPosition!=null) {
					g.setPaint(new Color(0,0,0,150));
			        g.fillRoundRect(48, arg3-24, 423, 20, 10, 10);
			        g.setPaint(Color.WHITE);
			        Coordinates p = new Coordinates(currentPosition.getLatitude(), currentPosition.getLongitude(), 0);
					g.drawString("lat: " + numberFormatter.format(currentPosition.getLatitude()) + "° ("+ p.getFormattedLatitude() +")", 53, arg3-10);
					g.drawString("long: " + numberFormatter.format(currentPosition.getLongitude()) + "° ("+ p.getFormattedLongitude() +")", 250, arg3-10);
				}
			}
			@Override
			public int getLayerNumber() {
				return 0;
			}
		};
		addCustomPainter(infoPainter);
		
		//initialize events
		getMainMap().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent arg0) {}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent e) {
				if(lastSelected!=null && lastSelected.isEditable()) {
					if(e.getKeyCode()==KeyEvent.VK_DELETE) {
						int r = JOptionPane.showConfirmDialog(getThis(), "Do you confirm deletion?", "Confirmation", JOptionPane.YES_NO_OPTION);
						if(r==JOptionPane.OK_OPTION) { 
							lastSelected.getGroup().removeElement(lastSelected);
			        	    for (MapActionListener<MapElement> ml : listeners) {
								ml.onElementEvent(lastSelected, EventType.DELETED);
							}
							clearSelection(null);
							updateUI();
						}
					}
				}
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
					newElementGroupIdToInsertOnClick = null;
					clearSelection(null);
					updateToolButtons();
					updateUI();
				}
			}
		});
		
        getMainMap().addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		
        		Point2D mp = getMapPosition(e.getX(), e.getY());
        	    currentPosition = getMainMap().getTileFactory().pixelToGeo(mp, getMainMap().getZoom());

        	    //verify if any element was selected
        	    MapElement selectedElement = null;
        		for (MapElement me : getAllMapElements()) {
        			//element selected
					if(me.getMouseMask()!=null && me.getMouseMask().contains(mp.getX(), mp.getY())) {
						selectedElement = me;
					}
        		}
        		
        		if(e.getButton()==MouseEvent.BUTTON1) {
	        		//select only the last element with mouse over (avoid selecting multiple elements)
	        		if(selectedElement!=null) {
	        			getMainMap().setPanEnabled(false);
	    				if(!selectedElement.isSelected()) {
	    					selectElement(selectedElement);
	    				}
	
	    			//add a new element
	        		} else if(newElementGroupIdToInsertOnClick!=null) {
	        			MapElement me = createMapElement(newElementGroupIdToInsertOnClick, currentPosition, 0, MapElement.class);
	        			selectElement(me);
	        			
	        			//avoid "add new button" when it is full
	        			if(getMapElementGroup(newElementGroupIdToInsertOnClick, MapElement.class).isFull()) {
	        				newElementGroupIdToInsertOnClick = null;
	        				updateToolButtons();
	        			}
	    				
	        		} else {
	        			//deselect all elements
	        			if(clearSelectionOnMapClick) {
		        			clearSelection(null);
	        			}
	        			newElementGroupIdToInsertOnClick = null;
	            	    //broadcast mouse clicked on map event
	            	    for (MapActionListener<MapElement> ml : listeners) {
	    					ml.onMouseClickedOnMap(currentPosition, mp);
	    				}
	        		}
        		} else if(e.getButton()==MouseEvent.BUTTON3) {
        			lastSelectedButton3 = selectedElement;
        		}
        		
        	    updateUI();
        	}
        	

			@Override
        	public void mouseReleased(MouseEvent arg0) {
    			getMainMap().setPanEnabled(true);
    			if(lastSelectedMoved) {
    				lastSelectedMoved = false;
	        	    for (final MapActionListener<MapElement> ml : listeners) {
	        	    	SwingUtilities.invokeLater(new Runnable() {
	        	    		@Override
	        	    		public void run() {
	    						ml.onElementEvent(lastSelected, EventType.UPDATED);
	        	    		}
	        	    	});
					}
    			}
        	}
        	
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>=2) {
        			if(lastSelected!=null) {
		        	    for (MapActionListener<MapElement> ml : listeners) {
							ml.onElementDoubleClicked(lastSelected);
						}
		        	    if(lastSelected.isSetLabelOnDoubleClick()) {
	        				//set label for common elements
		        			String label = JOptionPane.showInputDialog(getThis(), "Label:", lastSelected.getLabel());
		        			if(label!=null && !label.equals("")) {
		        				lastSelected.setLabel(label);
		        				updateUI();
		        			}
		        	    }
        			}
        		}
        	}
        });
        
        getMainMap().addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		if(lastSelected!=null && lastSelected.isEditable()) {
        			getMainMap().setPanEnabled(false);
        			GeoPosition gpos = getGeoPosition(e.getX(), e.getY());
        			lastSelected.setPosition(gpos);
        			lastSelectedMoved = true;

            		//call element dragged event
            	    for (MapActionListener<MapElement> ml : listeners) {
    					ml.onElementDragged(lastSelected);
//    					ml.onElementEvent(lastSelected, EventType.UPDATED);
    				}
        		}
				Point2D mp = getMapPosition(e.getX(), e.getY());
        		currentPosition = getMainMap().getTileFactory().pixelToGeo(mp, getMainMap().getZoom());
				updateUI();
        	}

        	public void mouseMoved(MouseEvent e) {
				Point2D mp = getMapPosition(e.getX(), e.getY());

				//verify if any element was selected
        	    MapElement lastOver = null;
        		for (MapElement me : getAllMapElements()) {
        			//element selected
					if(me.getMouseMask()!=null && me.getMouseMask().contains(mp.getX(), mp.getY())) {
		        	    lastOver = me;
					}
        		}
        		
        		//select only the last element with mouse over (avoid selecting multiple elements)
        		if(lastOver!=null) {
    				if(!lastOver.isMouseOver()) {
    					lastOver.setMouseOver(true);
		        	    for (MapActionListener<MapElement> ml : listeners) {
							ml.onMouseOverElement(lastOver);
						}
    				}
        		}
        		
				//deselect all elements but the currently selected
        		for (MapElement me : getAllMapElements()) {
					if(me!=lastOver && me.isMouseOver()) {
						me.setMouseOver(false);
		        	    for (MapActionListener<MapElement> ml : listeners) {
							ml.onMouseOutElement(me);
						}
					}
        		}
        		
        		currentPosition = getMainMap().getTileFactory().pixelToGeo(mp, getMainMap().getZoom());
        	    updateUI();
        	}

        });
        
        //initialize tile factory with disk cache
        setupMapTileFactory(null);
        
        //register ruler map element group
        PathRulerPainter pp =new PathRulerPainter();
        MapElementGroup<PathMapElement> rulerGroup = new MapElementGroup<PathMapElement>(this, "Ruler", 0, 
        	new MapElementBridge<PathMapElement>() {
	        	@Override
	        	public PathMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<PathMapElement> group) {
	        		return new PathMapElement();
	        	}
        	}, pp);
        addMapElementGroup(rulerGroup, GROUP_RULER);
        toolComponents.add(getRulerButton());
	}
	
	public void setupMapTileFactory(File cacheDir) {
        GoogleMapsTileFactoryInfo tfi = new GoogleMapsTileFactoryInfo();
        DefaultTileFactory dtf = new GoogleMapsTileFactory(tfi);
		if(cacheDir==null) {
	        DiskTileCache cache = new DiskTileCache();
	        dtf.setTileCache(cache);
		} else {
	        DiskTileCache cache = new DiskTileCache(cacheDir);
	        dtf.setTileCache(cache);
		}
        getMainMap().setDesignTime(false);
		getMainMap().setTileFactory(dtf);

		//don't push google too much to avoid black list
//        if(getMainMap().getTileFactory() instanceof DefaultTileFactory) {
//        	((DefaultTileFactory)getMainMap().getTileFactory()).setThreadPoolSize(1);
//        }
	}

	private MapKit getThis() {
		return this;
	}

	public void getTilesFromGoogleMaps() {
        if(getMainMap().getTileFactory().getInfo() instanceof GoogleMapsTileFactoryInfo) {
        	final GoogleMapsTileFactory dtf = (GoogleMapsTileFactory)getMainMap().getTileFactory();
        	final DiskTileCache dtc = ((DiskTileCache)dtf.getTileCache());
        	dtc.setThrowExceptionOnNotFound(false);
    		getMainMap().updateUI();
    		new Thread(new Runnable() {
    			public void run() {
					try {
						Thread.sleep(5000); 
			        	dtc.setThrowExceptionOnNotFound(true);
					} catch (Exception e) {}
    			}
			}).start();
        }
	}

	protected void setupPainters() {
		//merge painters in one list
		List<LayerPainter<JXMapViewer>> painters = new ArrayList<LayerPainter<JXMapViewer>>();
		painters.addAll(customPainters);
		synchronized(elementGroups) {
			for (MapElementGroup<? extends MapElement> g : elementGroups.values()) {
				painters.add(g.getPainter());
			}
		}
		
		//order according to layer number
		Collections.sort(painters, new Comparator<LayerPainter<JXMapViewer>>() {
			@Override
			public int compare(LayerPainter<JXMapViewer> o1, LayerPainter<JXMapViewer> o2) {
				if(o1.getLayerNumber()<o2.getLayerNumber()) {
					return 1;
				} else if(o1.getLayerNumber()>o2.getLayerNumber()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		//create compound painter
		CompoundPainter<JXMapViewer> pg = new CompoundPainter<JXMapViewer>();
		pg.setPainters((Painter[])painters.toArray(new Painter[0]));
		pg.setCacheable(false);
		getMainMap().setOverlayPainter(pg);
	}

	private Point2D getMapPosition(float mousex, float mousey) {
	    Rectangle r = getMainMap().getViewportBounds();
	    float worldX = r.x + mousex;
	    float worldY = r.y + mousey;
	    Point2D p = new Point2D.Float(worldX, worldY);
	    return p;
	}
	private GeoPosition getGeoPosition(int mousex, int mousey) {
		Point2D pos = getMapPosition(mousex, mousey);
		return getMainMap().getTileFactory().pixelToGeo(pos, getMainMap().getZoom());
	}
	
	public void addMapActionListener(MapActionListener<MapElement> listener) {
		listeners.add(listener);
	}

	public void addCustomPainter(LayerPainter<JXMapViewer> painter) {
		customPainters.add(painter);
		setupPainters();
	}
	public void removeCustomPainter(LayerPainter<JXMapViewer> painter) {
		customPainters.remove(painter);
		setupPainters();
	}

	/**
	 * This method initializes googleMapsButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGoogleMapsButton() {
		if (googleMapsButton == null) {
			googleMapsButton = new JButton();
			googleMapsButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/mapkit/googleEarth.gif")));
			googleMapsButton.setSize(new Dimension(48, 26));
			googleMapsButton.setToolTipText("Load tiles with Google Maps");
			setupButton(googleMapsButton);
			googleMapsButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getTilesFromGoogleMaps();
				}
			});
	    }
		return googleMapsButton;
	}

	public void selectElement(MapElement element) {
		//clear selection
		clearSelection(element);
		
		//select the element
		element.setSelected(true);
		lastSelected = element;
		
		updateToolButtons();
		updateUI();
		
		//broadcast event
	    for (MapActionListener<MapElement> ml : listeners) {
			ml.onElementEvent(lastSelected, EventType.SELECTED);
		}
	}

	private void updateToolButtons() {
		jPanel.removeAll();
		//show add element button
		if(newElementGroupIdToInsertOnClick!=null) {
			jPanel.add(getCancelCurrentOperation());
			
		} else if(lastSelected!=null && lastSelected.getGroup().isEnabledToAddElements() && !lastSelected.getGroup().isFull()) {
			jPanel.add(getAddElementToGroup());
			
		//show normal tool components
		} else {
			for (JComponent tc : toolComponents) {
				jPanel.add(tc);
			}
		}
	    updateUI();
	}

	public void clearSelection() {
		clearSelection(null);
	}

	//clear selection
	private void clearSelection(MapElement elementToBeSelected) {
		for(MapElement elem : getAllMapElements()) {
			if(elementToBeSelected==null || elem!=elementToBeSelected) {
				if(elem.isSelected()) {
		    	    for (MapActionListener<MapElement> ml : listeners) {
						ml.onElementEvent(elem, EventType.DESELECTED);
					}
					elem.setSelected(false);
				}
			}
		}
		lastSelected = elementToBeSelected;
		updateToolButtons();
	}
	
	public MapElement getSelectedElement() {
		return lastSelected;
	}
	
	public Map<Object, MapElementGroup<? extends MapElement>> getMapElementGroups() {
		return elementGroups;
	}
	
	private void setupButton(JButton button) {
		button.setMargin(new java.awt.Insets(2, 2, 2, 2));
		button.setMaximumSize(new java.awt.Dimension(20, 20));
		button.setMinimumSize(new java.awt.Dimension(20, 20));
		button.setPreferredSize(new java.awt.Dimension(20, 20));
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setHgap(3);
			flowLayout.setVgap(3);
			jPanel = new JPanel();
			jPanel.setLayout(flowLayout);
			jPanel.setSize(new Dimension(27, 208));
			jPanel.setOpaque(false);
		}
		return jPanel;
	}
	
	public List<? extends MapElement> getAllMapElements() {
		List<MapElement> r = new ArrayList<MapElement>();
		synchronized(elementGroups) {
			for (MapElementGroup<? extends MapElement> group : elementGroups.values()) {
				r.addAll(group.getElements());
			}
		}
		return r;
	}

	public void zoomToElements() {
		synchronized(elementGroups) {
			if(elementGroups.keySet().size()>0) {
				int n = 0;
				double lats = 0;
				double longs = 0;
				Set<GeoPosition> pos = new HashSet<GeoPosition>();
				for (MapElement me : getAllMapElements()) {
					pos.add(me.getPosition());
					lats += me.getPosition().getLatitude();
					longs += me.getPosition().getLongitude();
					n++;
				}
				setCenterPosition(new GeoPosition(lats/n, longs/n));
				getMainMap().calculateZoomFrom(pos);
			}
		}
	}

	/**
	 * This method initializes addElementToGroup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddElementToGroup() {
		if (addElementToGroup == null) {
			addElementToGroup = new JButton();
			addElementToGroup.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/mapkit/add-point.gif")));
			addElementToGroup.setToolTipText("Add a new element to the selected group");
			addElementToGroup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(newElementGroupIdToInsertOnClick==null) {
						//insert the new point in the currently selected group of elements
						newElementGroupIdToInsertOnClick = lastSelected.getGroup().getId();
						updateToolButtons();
					}
				}
			});
			setupButton(addElementToGroup);
		}
		return addElementToGroup;
	}

	private JButton getCancelCurrentOperation() {
		if (cancelCurrentOperation == null) {
			cancelCurrentOperation = new JButton();
			cancelCurrentOperation.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/mapkit/cancel.gif")));
			cancelCurrentOperation.setToolTipText("Cancel operation");
			cancelCurrentOperation.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					newElementGroupIdToInsertOnClick = null;
					updateToolButtons();
				}
			});
			setupButton(cancelCurrentOperation);
		}
		return cancelCurrentOperation;
	}
	
	private JButton getRulerButton() {
		if (rulerButton == null) {
			rulerButton = new JButton();
			rulerButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/mapkit/ruler.gif")));
			rulerButton.setToolTipText("Ruler tool");
			rulerButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getMapElementGroup(GROUP_RULER, PathMapElement.class).clearElements();
					newElementGroupIdToInsertOnClick = GROUP_RULER;
					updateToolButtons();
				}
			});
			setupButton(rulerButton);
		}
		return rulerButton;
	}
	
	public void addNewElementOnClick(Object mapElementGroupId) {
		if(!elementGroups.containsKey(mapElementGroupId)) {
			throw new IllegalArgumentException("Group '" + mapElementGroupId + "' was not found");
		}
		newElementGroupIdToInsertOnClick = mapElementGroupId;
		updateToolButtons();
	}
	
	public boolean isClearSelectionOnMapClick() {
		return clearSelectionOnMapClick;
	}
	public void setClearSelectionOnMapClick(boolean clearSelectionOnMapClick) {
		this.clearSelectionOnMapClick = clearSelectionOnMapClick;
	}
	
	public GeoPosition getCurrentMousePosition() {
		return currentPosition;
	}
	
	public MapElement getLastSelectedButton3() {
		return lastSelectedButton3;
	}
	
	public void removeMapElementGroupsByIdPrefix(String stringPrefix) {
		//remove all map element groups
		List<Object> remove = new ArrayList<Object>();
		synchronized(elementGroups) {
			for (Object key: elementGroups.keySet()) {
				if(key.toString().startsWith(stringPrefix)) {
					remove.add(key);
				}
			}
			for (Object key : remove) {
				removeMapElementGroup(key);
			}
		}
		
		updateUI();
	}

	public void addMapElementGroup(MapElementGroup<? extends MapElement> elementGroup, Object groupId) {
		synchronized(elementGroups) {
			elementGroups.put(groupId, elementGroup);
			elementGroup.setMap(getMainMap());
			elementGroup.setId(groupId);
		}
		setupPainters();
	}
	public void removeMapElementGroup(Object groupId) {
		synchronized(elementGroups) {
			elementGroups.remove(groupId);
		}
		setupPainters();
		updateUI();
	}
	
	public <T extends MapElement> MapElementGroup<T> getMapElementGroup(Object groupId, Class<T> mapElementType) {
		return (MapElementGroup<T>)elementGroups.get(groupId);
	}

	public <T extends MapElement> T createMapElement(Object groupId, GeoPosition position, float altitude, Class<T> mapElementType) {
		MapElementGroup<T> g = getMapElementGroup(groupId, mapElementType);
		if(g==null) throw new IllegalArgumentException("Group '" + groupId + "' was not found");
		return createMapElement(g, position, altitude, mapElementType, g.guessBestElementIndex(position));
	}
	
	public <T extends MapElement> T createMapElement(Object groupId, GeoPosition position, float altitude, Class<T> mapElementType, int elementIndex) {
		MapElementGroup<T> g = getMapElementGroup(groupId, mapElementType);
		if(g==null) throw new IllegalArgumentException("Group '" + groupId + "' was not found");
		return createMapElement(g, position, altitude, mapElementType, elementIndex);
	}
	
	protected <T extends MapElement> T createMapElement(MapElementGroup<T> g, GeoPosition position, float altitude, Class<T> mapElementType, int elementIndex) {
		T me = g.createElement(position, altitude, mapElementType, elementIndex);
	    for (MapActionListener<MapElement> ml : listeners) {
			ml.onElementEvent(me, EventType.CREATED);
		}
		return me;
	}


	/**
	 * Add a map element to the group at indicated index
	 * If elementIndex is -1, them put it to tail
	 * @return
	 */
	public <T extends MapElement> T addMapElement(Object groupId, T mapElement, int elementIndex, Class<T> mapElementType) {
		MapElementGroup<T> g = getMapElementGroup(groupId, mapElementType);
		if(g==null) throw new IllegalArgumentException("Group '" + groupId + "' was not found");
		mapElement = g.addElement(mapElement, elementIndex, mapElementType);
	    for (MapActionListener<MapElement> ml : listeners) {
			ml.onElementEvent(mapElement, EventType.CREATED);
		}
	    return mapElement;
	}
	
	
	public void addToolComponent(JComponent component) {
		toolComponents.add(component);
	}
	
	public void removeToolComponent(JComponent component) {
		toolComponents.remove(component);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
