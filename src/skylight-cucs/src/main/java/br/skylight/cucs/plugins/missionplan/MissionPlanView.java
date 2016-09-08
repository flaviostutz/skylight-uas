package br.skylight.cucs.plugins.missionplan;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.TreeTableNode;

import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.LocationType;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.RouteType;
import br.skylight.commons.dli.enums.TransferStatus;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.mission.MissionUploadDownloadStatus;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.subscriber.MissionListener;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.RoundButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.tables.JXTreeTable2;
import br.skylight.cucs.widgets.tables.TypedTreeTableNode;

public class MissionPlanView extends VehicleView implements MissionListener, MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private RoundButton activate = null;
	private final ImageIcon ICON_CANCEL = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/cancel.gif"));
	private final ImageIcon ICON_OK = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/rollback.gif"));

	private JButton add = null;
	private JButton remove = null;
	private JButton properties = null;
	private JPanel jPanel = null;
	private JPopupMenu addMenu = null;  //  @jve:decl-index=0:visual-constraint="448,18"
	private JMenuItem addRoute = null;
	private JMenuItem addWaypoint = null;
	private JScrollPane scroll = null;
	private JXTreeTable2 missionTreeTable = null;
	private JButton open = null;
	private JButton save = null;
	private JButton newMission = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JButton validate = null;
	private JPopupMenu treePopupMenu;
	private AVRoute selectedRoute;  //  @jve:decl-index=0:
	private AVPositionWaypoint selectedPositionWaypoint;  //  @jve:decl-index=0:
	private File lastFile = null;

	private JButton saveAs = null;
//	private JProgressBar transferProgress = null;
//	private JButton cancelTransfer = null;
	private JPanel transferPanel = null;
	private JPanel idleTransfer = null;
	private JButton upload = null;
	private JButton download = null;
	private JPanel activeTransfer = null;
	private JProgressBar transferProgress = null;
	private JButton endTransfer = null;
	
	private boolean transferCancel = true;

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public StorageService storageService;
	
	@ServiceInjection
	public PluginManager pluginManager;

	public MissionPlanView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMissionListener(this);
		subscriberService.addMessageListener(MessageType.M900, this);
		subscriberService.addMessageListener(MessageType.M106, this);
	}
	
	@Override
	protected void updateGUI() {
		updateButtons();

		//refresh tree table contents
		if(getCurrentVehicle()!=null) {
			((MissionPlanTreeModel)getMissionTreeTable().getTreeTableModel()).updateMission(getCurrentVehicle().getMission());
		}

		getMissionTreeTable().expandAll();
		getMissionTreeTable().updateUI();
	}
	
	private void updateButtons() {
		getDownload().setEnabled(getCurrentVehicle()!=null);
		getNewMission().setEnabled(getCurrentVehicle()!=null);
		getOpen().setEnabled(getCurrentVehicle()!=null);
		getActivate().setEnabled(getCurrentVehicle()!=null);
		getRemove().setEnabled(getCurrentVehicle()!=null && (selectedPositionWaypoint!=null || selectedRoute!=null));
		getValidate().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
		getUpload().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
		getSave().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
		getSaveAs().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
		getProperties().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
		getAdd().setEnabled(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null);
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.fill = GridBagConstraints.BOTH;
			gridBagConstraints15.gridy = 0;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 1.0;
			gridBagConstraints15.gridx = 4;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 9;
			gridBagConstraints6.weighty = 0.0;
			gridBagConstraints6.anchor = GridBagConstraints.CENTER;
			gridBagConstraints6.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 8;
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.anchor = GridBagConstraints.CENTER;
			gridBagConstraints5.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.gridwidth = 5;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.weighty = 0.0;
			gridBagConstraints3.gridy = 3;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(394, 135));
			contents.add(getJPanel(), gridBagConstraints3);
			contents.add(getScroll(), gridBagConstraints15);
		}
		return contents;
	}

	@Override
	protected String getBaseTitle() {
		return "Mission Plan";
	}

	private RoundButton getActivate() {
		if (activate == null) {
			activate = new RoundButton();
			activate.setRoundness(10);
			activate.setText("Activate");
			activate.setColorUnselected(Color.LIGHT_GRAY);
			activate.setColorSelected(Color.ORANGE);
			activate.setToolTipText("Start waypoint mode");
			activate.setEnabled(false);
			activate.setMargin(ViewHelper.getDefaultButtonMargin());
			activate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(!activate.isSelected()) {
						//change operating mode
						VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
						m.setSelectFlightPathControlMode(FlightPathControlMode.WAYPOINT);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
					} else {
						VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
						m.setSelectFlightPathControlMode(FlightPathControlMode.NO_MODE);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
					}
				}
			});
		}
		return activate;
	}

	/**
	 * This method initializes add	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdd() {
		if (add == null) {
			add = new JButton();
			add.setMargin(ViewHelper.getDefaultButtonMargin());
			add.setToolTipText("Add a new route/waypoint to mission");
			add.setEnabled(false);
			add.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/add.gif")));
			add.addMouseListener(new JPopupMenuMouseListener(getAddMenu(), true));
		}
		return add;
	}

	/**
	 * This method initializes remove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemove() {
		if (remove == null) {
			remove = new JButton();
			remove.setMargin(ViewHelper.getDefaultButtonMargin());
			remove.setToolTipText("Remove selected route/waypoint");
			remove.setEnabled(false);
			remove.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/remove.gif")));
			remove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					deleteSelectedItem();
				}
			});
		}
		return remove;
	}

	private void deleteSelectedItem() {
		if(selectedRoute!=null) {
			if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "The selected route will be deleted. Confirm operation?")) {
				return;
			}
			boolean delete = false;
			int answer = JOptionPane.showConfirmDialog(null, "Delete all waypoints inside route?");
			if(answer==JOptionPane.OK_OPTION) {
				delete = true;
			} else if(answer==JOptionPane.NO_OPTION) {
				delete = false;
			} else {
				return;//canceled
			}
			//remove all waypoint inside this route
			getCurrentVehicle().getMission().deleteRoute(selectedRoute, delete);
			getCurrentVehicle().getMission().normalizeWaypointNumbers();
		} else if(selectedPositionWaypoint!=null) {
			if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "The selected waypoint will be deleted. Confirm operation?")) {
				return;
			}
			getCurrentVehicle().getMission().removeWaypointAt(selectedPositionWaypoint.getWaypointNumber(), true);
			getCurrentVehicle().getMission().normalizeWaypointNumbers();
		}
		selectedRoute = null;
		selectedPositionWaypoint = null;
		updateGUI();
	}

	/**
	 * This method initializes properties	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getProperties() {
		if (properties == null) {
			properties = new JButton();
			properties.setMargin(ViewHelper.getDefaultButtonMargin());
			properties.setToolTipText("Set advanced mission parameters");
			properties.setEnabled(false);
			properties.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/attributes.gif")));
			properties.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MissionPropertiesDialog d = new MissionPropertiesDialog(null);
					pluginManager.manageObject(d);
					d.showDialog(getCurrentVehicle());
					subscriberService.notifyMissionEvent(getCurrentVehicle().getMission(), EventType.UPDATED, getThis());
				}
			});
		}
		return properties;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 3;
			gridBagConstraints20.insets = new Insets(0, 0, 0, 3);
			gridBagConstraints20.gridy = 1;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.gridy = 1;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.insets = new Insets(2, 5, 5, 2);
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new Dimension(376, 30));
			jPanel.add(getActivate(), gridBagConstraints2);
			jPanel.add(getJPanel1(), gridBagConstraints14);
			jPanel.add(getJPanel2(), gridBagConstraints21);
			jPanel.add(getTransferPanel(), gridBagConstraints20);
		}
		return jPanel;
	}

	/**
	 * This method initializes addMenu	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getAddMenu() {
		if (addMenu == null) {
			addMenu = new JPopupMenu();
			addMenu.add(getAddRoute());
			addMenu.add(getAddWaypoint());
		}
		return addMenu;
	}

	/**
	 * This method initializes addRoute	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAddRoute() {
		if (addRoute == null) {
			addRoute = new JMenuItem();
			addRoute.setText("Add Route");
			addRoute.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int insertAfter = resolveInsertAfter();
					AVRoute ar = new AVRoute();
					ar.setRouteID("Route at " + insertAfter);
					ar.setInitialWaypointNumber(insertAfter);
					getCurrentVehicle().getMission().getRoutes().add(ar);
					updateGUI();
				}
			});
		}
		return addRoute;
	}

	/**
	 * This method initializes addWaypoint	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAddWaypoint() {
		if (addWaypoint == null) {
			addWaypoint = new JMenuItem();
			addWaypoint.setText("Add Waypoint");
			addWaypoint.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertNewWaypoint();
				}
			});
		}
		return addWaypoint;
	}

	//determine where to put the new waypoint
	private int resolveInsertAfter() {
		//look for the last element in selected route. put new element as the last one
		if(selectedRoute!=null) {
			int lastWaypoint = selectedRoute.getInitialWaypointNumber()-1;
			MissionPlanTreeModel model = (MissionPlanTreeModel)getMissionTreeTable().getTreeTableModel();
			TypedTreeTableNode<AVRoute> nr = model.resolveNodeForRoute(selectedRoute);
			for(int i=0; i<nr.getChildCount(); i++) {
				TypedTreeTableNode<AVPositionWaypoint> pw = (TypedTreeTableNode<AVPositionWaypoint>)nr.getChildAt(i);
				if(pw.getUserObject().getWaypointNumber()>lastWaypoint) {
					lastWaypoint = pw.getUserObject().getWaypointNumber();
				}
			}
			return lastWaypoint;
			
		//put the element after the selected waypoint
		} else if(selectedPositionWaypoint!=null) {
			return selectedPositionWaypoint.getWaypointNumber();
			
		//nothing is selected. put the new waypoint in last position
		} else {
			return getCurrentVehicle().getMission().getHighestWaypointNumber();
		}
	}
	
	/**
	 * This method initializes scroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane();
			scroll.setViewportView(getMissionTreeTable());
		}
		return scroll;
	}

	/**
	 * This method initializes missionTreeTable	
	 * 	
	 * @return br.skylight.cucs.widgets.tables.missionTreeTable	
	 */
	private JXTreeTable2 getMissionTreeTable() {
		if (missionTreeTable == null) {
			MissionPlanTreeModel treeTableModel = new MissionPlanTreeModel();
			treeTableModel.addMissionPlanTreeModelListener(new MissionPlanTreeModelListener() {
				@Override
				public void onRouteUpdated(AVRoute route, Object newValue, int column) {
					subscriberService.notifyMissionEvent(getCurrentVehicle().getMission(), EventType.UPDATED, getThis());
				}
				@Override
				public void onPositionWaypointUpdated(AVPositionWaypoint pw, Object newValue, int column) {
					subscriberService.notifyMissionWaypointEvent(getCurrentVehicle().getMission(), pw, EventType.UPDATED, getThis());
				}
				@Override
				public void onMissionUpdated(Mission mission) {
					subscriberService.notifyMissionEvent(getCurrentVehicle().getMission(), EventType.UPDATED, getThis());
				}
			});
			missionTreeTable = new JXTreeTable2(treeTableModel);
			missionTreeTable.setColumnSelectionAllowed(false);
			missionTreeTable.setColumnControlVisible(true);
			missionTreeTable.setExpandsSelectedPaths(true);
			missionTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			missionTreeTable.setScrollsOnExpand(true);
			missionTreeTable.setEditable(true);
			missionTreeTable.setShowsRootHandles(true);
			missionTreeTable.add(getTreePopupMenu());
			missionTreeTable.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
			missionTreeTable.addMouseListener(new JPopupMenuMouseListener(getTreePopupMenu()));
			missionTreeTable.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(getCurrentVehicle().getMission()!=null) {
						if(e.getKeyCode()==KeyEvent.VK_DELETE) {
							deleteSelectedItem();
						} else if(e.getKeyCode()==KeyEvent.VK_INSERT) {
							insertNewWaypoint();
						}
					}
				}
			});
			missionTreeTable.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					selectedPositionWaypoint = null;
					selectedRoute = null;
					if(e.getPath().getLastPathComponent() instanceof TreeTableNode) {
						Object o = ((TreeTableNode)e.getPath().getLastPathComponent()).getUserObject();
						if(o instanceof AVRoute) {
							selectedRoute = (AVRoute)o;
							updateButtons();
						} else if (o instanceof AVPositionWaypoint) {
							selectedPositionWaypoint = (AVPositionWaypoint)o;
							subscriberService.notifyMissionWaypointEvent(getCurrentVehicle().getMission(), selectedPositionWaypoint, EventType.SELECTED, getThis());
							updateButtons();
						}
					}
				}
			});
			missionTreeTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount()>=2) {
						if(selectedPositionWaypoint!=null) {
							showWaypointDialog();
						}
					}
				}
			});
			missionTreeTable.setCellEditor(RouteType.class, new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(RouteType.class))));
			missionTreeTable.setCellEditor(LocationType.class, new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(LocationType.class))));
			missionTreeTable.setCellEditor(AltitudeType.class, new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(AltitudeType.class))));
			missionTreeTable.setCellEditor(WaypointSpeedType.class, new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(WaypointSpeedType.class))));
			missionTreeTable.setCellEditor(TurnType.class, new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(TurnType.class))));
			missionTreeTable.setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			missionTreeTable.getColumn(0).setMinWidth(80);
			//important for column sizing control to be effective
			missionTreeTable.setAutoCreateColumnsFromModel(false);
		}
		return missionTreeTable;
	}

	protected void showWaypointDialog() {
		MissionPlanWaypointDialog d = new MissionPlanWaypointDialog(null);
		d.showDialog(getCurrentVehicle(), selectedPositionWaypoint.getWaypointNumber());
		updateGUI();
	}

	protected void insertNewWaypoint() {
		int insertAfter = resolveInsertAfter();
		AVPositionWaypoint pw = new AVPositionWaypoint();
		if(selectedPositionWaypoint!=null) {
			pw = (AVPositionWaypoint)selectedPositionWaypoint.createCopy();
			pw.setWaypointToLongitudeOrRelativeX(pw.getWaypointToLongitudeOrRelativeX()+CoordinatesHelper.metersToLongitudeLength(300, pw.getWaypointToLatitudeOrRelativeY()));
		} else {
			//find last waypoint and put this waypoint near it
			if(getCurrentVehicle().getMission().getPositionWaypoints().size()>0) {
				AVPositionWaypoint wd = getCurrentVehicle().getMission().getPositionWaypoints().get(getCurrentVehicle().getMission().getPositionWaypoints().size()-1);
				pw = (AVPositionWaypoint)wd.createCopy();
				pw.setWaypointToLongitudeOrRelativeX(pw.getWaypointToLongitudeOrRelativeX()+CoordinatesHelper.metersToLongitudeLength(300, pw.getWaypointToLatitudeOrRelativeY()));
			} else {
				//TODO put in current map position
				pw.setWaypointToLatitudeOrRelativeY(-Math.toRadians(15));
				pw.setWaypointToLongitudeOrRelativeX(-Math.toRadians(45));
			}
			VehicleConfigurationMessage vc = getCurrentVehicle().getVehicleConfiguration();
			if(vc!=null) {
				pw.setWaypointToAltitude(vc.getOptimumCruiseIndicatedAirspeed());
				pw.setWaypointSpeedType(WaypointSpeedType.GROUND_SPEED);
			}
		}
		getCurrentVehicle().getMission().insertWaypoint(pw, insertAfter);
		getCurrentVehicle().getMission().normalizeWaypointNumbers();
		selectedPositionWaypoint = pw;
		updateGUI();
		subscriberService.notifyMissionWaypointEvent(getCurrentVehicle().getMission(), pw, EventType.CREATED, getThis());
	}

	/**
	 * This method initializes open	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOpen() {
		if (open == null) {
			open = new JButton();
			open.setToolTipText("Load mission");
			open.setMargin(ViewHelper.getDefaultButtonMargin());
			open.setText("");
			open.setEnabled(false);
			open.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif")));
			open.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getCurrentVehicle().getMission()!=null) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Current mission will be replaced. Confirm that?")) {
							return;
						}
					}
					File f = ViewHelper.showFileSelectionDialog(null, storageService.getBaseDir(), ".smf", "Skylight mission file (*.smf)", false);
					if(f!=null) {
						lastFile = f;
						try {
							vehicleControlService.loadMission(getCurrentVehicle().getVehicleID().getVehicleID(), f);
						} catch (IOException e1) {
							ViewHelper.showException(e1);
						}
						selectedRoute = null;
						selectedPositionWaypoint = null;
						updateGUI();
					}
				}
			});
		}
		return open;
	}

	/**
	 * This method initializes save	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSave() {
		if (save == null) {
			save = new JButton();
			save.setToolTipText("Save mission");
			save.setMargin(ViewHelper.getDefaultButtonMargin());
			save.setText("");
			save.setEnabled(false);
			save.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/save.gif")));
			save.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(lastFile==null) {
						File f = ViewHelper.showFileSelectionDialog(null, storageService.getBaseDir(), ".smf", "Skylight mission file (*.smf)", true);
						if(f!=null) {
							lastFile = f;
						} else {
							return;
						}
					}
					try {
						vehicleControlService.saveMission(getCurrentVehicle().getVehicleID().getVehicleID(), lastFile);
					} catch (IOException e1) {
						ViewHelper.showException(e1);
					}
				}
			});
		}
		return save;
	}

	/**
	 * This method initializes newMission	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getNewMission() {
		if (newMission == null) {
			newMission = new JButton();
			newMission.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/new.gif")));
			newMission.setToolTipText("New mission");
			newMission.setEnabled(false);
			newMission.setMargin(ViewHelper.getDefaultButtonMargin());
			newMission.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getCurrentVehicle().getMission()!=null) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Current mission will be replaced. Continue?")) {
							return;
						}
					}
					vehicleControlService.createNewMission(getCurrentVehicle().getVehicleID().getVehicleID());
					lastFile = null;
					selectedRoute = null;
					selectedPositionWaypoint = null;
					updateGUI();
				}
			});
		}
		return newMission;
	}

	private MissionPlanView getThis() {
		return this;
	}
	
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 3;
			gridBagConstraints16.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(0, 7, 0, 0);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getAdd(), gridBagConstraints);
			jPanel1.add(getRemove(), gridBagConstraints1);
			jPanel1.add(getProperties(), gridBagConstraints4);
			jPanel1.add(getValidate(), gridBagConstraints16);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 3;
			gridBagConstraints17.gridy = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.insets = new Insets(0, 7, 0, 0);
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.gridx = 0;
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.add(getNewMission(), gridBagConstraints7);
			jPanel2.add(getOpen(), gridBagConstraints9);
			jPanel2.add(getSave(), gridBagConstraints10);
			jPanel2.add(getSaveAs(), gridBagConstraints17);
		}
		return jPanel2;
	}

	/**
	 * This method initializes validate	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getValidate() {
		if (validate == null) {
			validate = new JButton();
			validate.setToolTipText("Validate mission");
			validate.setMargin(ViewHelper.getDefaultButtonMargin());
			validate.setEnabled(false);
			validate.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/checkbox-warning.gif")));
			validate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					VerificationResult vr = vehicleControlService.validateMission(getCurrentVehicle().getVehicleID().getVehicleID());
					JOptionPane.showMessageDialog(null, vr.toString(), "Mission validation results", vr.getOptionPaneResultLevel());
				}
			});
		}
		return validate;
	}

	private JPopupMenu getTreePopupMenu() {
		if(treePopupMenu==null) {
			treePopupMenu = new JPopupMenu();
			treePopupMenu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					treePopupMenu.removeAll();
					if(selectedRoute!=null) {
						JMenuItem mi2 = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedRoute!=null) {
									//find next waypoint number
									List<WaypointDef> rw = new ArrayList<WaypointDef>();
									rw.addAll(getCurrentVehicle().getMission().getOrderedWaypoints());
									Collections.reverse(rw);
									for (WaypointDef wd : rw) {
										if(wd.getWaypointNumber()<selectedRoute.getInitialWaypointNumber()) {
											selectedRoute.setInitialWaypointNumber(wd.getWaypointNumber());
											break;
										}
									}
									//if previous waypoint is not found (mission begining), do nothing.
								}
								updateGUI();
							}
						});
						mi2.setText("Move up");
						treePopupMenu.add(mi2);

						JMenuItem mi3 = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedRoute!=null) {
									//find next waypoint number
									for (WaypointDef wd : getCurrentVehicle().getMission().getOrderedWaypoints()) {
										if(wd.getWaypointNumber()>selectedRoute.getInitialWaypointNumber()) {
											selectedRoute.setInitialWaypointNumber(wd.getWaypointNumber());
											break;
										}
									}
									//if next waypoint is not found (mission ending), do nothing.
								}
								updateGUI();
							}
						});
						mi3.setText("Move down");
						treePopupMenu.add(mi3);

						JMenuItem miu = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedRoute!=null) {
									//cancel any transfer in progress
									vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD);
									
									//upload ROUTE
									selectedRoute.setTimeStamp(System.currentTimeMillis()/1000.0);
									selectedRoute.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
									messagingService.sendMessage(selectedRoute);
									
									//upload all WAYPOINTS from selected ROUTE
									Mission m = getCurrentVehicle().getMission();
									for(WaypointDef wd : m.computeWaypointsMap().values()) {
										if(m.isWaypointInsideRoute(wd.getWaypointNumber(), selectedRoute.getInitialWaypointNumber())) {
											vehicleControlService.sendWaypointDefToVehicle(wd, getCurrentVehicle().getVehicleID().getVehicleID());
										}
									}

									//load waypoints to vehicle
									vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.LOAD_MISSION);
								}
							}
						});
						miu.setText("Upload route");
						treePopupMenu.add(miu);
						
						JMenuItem mi = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedRoute!=null) {
									//set next waypoint number
									VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
									vs.setCommandedWaypointNumber(selectedRoute.getInitialWaypointNumber());
									vehicleControlService.sendVehicleSteeringCommand(vs);
									
									//activate WAYPOINT mode
									VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
									m.setSelectFlightPathControlMode(FlightPathControlMode.WAYPOINT);
									m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
									messagingService.sendMessage(m);
								}
							}
						});
						mi.setText("Activate route");
						treePopupMenu.add(mi);
						
					} else if(selectedPositionWaypoint!=null) {
						JMenuItem miu = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedPositionWaypoint!=null) {
									//cancel any transfer in progress
									vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD);
									
									//upload SINGLE WAYPOINT
									WaypointDef wd = getCurrentVehicle().getMission().getComputedWaypointsMap().get(selectedPositionWaypoint.getWaypointNumber());
									vehicleControlService.sendWaypointDefToVehicle(wd, getCurrentVehicle().getVehicleID().getVehicleID());

									//load waypoints to vehicle
									vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.LOAD_MISSION);
								}
							}
						});
						miu.setText("Upload waypoint");
						treePopupMenu.add(miu);

						JMenuItem mi = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								if(selectedPositionWaypoint!=null) {
									//set next waypoint number
									VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
									vs.setCommandedWaypointNumber(selectedPositionWaypoint.getWaypointNumber());
									vehicleControlService.sendVehicleSteeringCommand(vs);

									//activate WAYPOINT mode
									VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
									m.setSelectFlightPathControlMode(FlightPathControlMode.WAYPOINT);
									m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
									messagingService.sendMessage(m);
								}
							}
						});
						mi.setText("Activate waypoint");
						treePopupMenu.add(mi);
					}
				}
				public void popupMenuCanceled(PopupMenuEvent arg0) {}
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
			});
		}
		return treePopupMenu;
	}

	@Override
	public void onMissionEvent(Mission mission, EventType type) {
//		getMissionTreeTable().updateUI();
		updateGUI();
	}

	/**
	 * This method initializes saveAs	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveAs() {
		if (saveAs == null) {
			saveAs = new JButton();
			saveAs.setToolTipText("Save mission as...");
			saveAs.setMargin(ViewHelper.getDefaultButtonMargin());
			saveAs.setText("");
			saveAs.setEnabled(false);
			saveAs.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/saveas.gif")));
			saveAs.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					File f = ViewHelper.showFileSelectionDialog(null, storageService.getBaseDir(), "*.smf", "Skylight mission file (*.smf)", true);
					if(f!=null) {
						lastFile = f;
						try {
							vehicleControlService.saveMission(getCurrentVehicle().getVehicleID().getVehicleID(), lastFile);
						} catch (IOException e1) {
							ViewHelper.showException(e1);
						}
					}
				}
			});
		}
		return saveAs;
	}

	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type) {
		//select waypoint
		selectedRoute = null;
		if(type.equals(EventType.DELETED)) {
			selectedPositionWaypoint = null;
		} else {
			selectedPositionWaypoint = pw;
		}
		CUCSViewHelper.selectTypedTreeTableRow(getMissionTreeTable(), pw);
		
		//update UI
		if(type.equals(EventType.DELETED) || type.equals(EventType.CREATED)) {
			updateGUI();
		} else {
//			getMissionTreeTable().updateUI();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		if(message instanceof MissionUploadDownloadStatus) {
			//switch to active panel
			((CardLayout)getTransferPanel().getLayout()).show(getTransferPanel(), getActiveTransfer().getName());
			
			//show progress
			MissionUploadDownloadStatus ms = ((MissionUploadDownloadStatus)message);
			if(ms.getStatus().equals(TransferStatus.IN_PROGRESS)) {
				getTransferProgress().setString(null);
				getEndTransfer().setIcon(ICON_CANCEL);
				getEndTransfer().setToolTipText("Cancel current transfer");
				transferCancel = true;
			} else if(ms.getStatus().equals(TransferStatus.ABORTED_REJECTED)) {
				getTransferProgress().setString("Aborted");
				getTransferProgress().setForeground(Color.RED);
				getEndTransfer().setIcon(ICON_OK);
				getEndTransfer().setToolTipText("Transfer aborted. Click to return");
				transferCancel = false;
			} else if(ms.getStatus().equals(TransferStatus.COMPLETE)) {
				getTransferProgress().setString("Complete");
				getTransferProgress().setForeground(Color.GREEN);
				getEndTransfer().setIcon(ICON_OK);
				getEndTransfer().setToolTipText("Transfer complete. Click to return");
				transferCancel = false;
			}
			getTransferProgress().setValue(ms.getPercentComplete());
			
			updateGUI();
			
		//M106
		} else if(message instanceof VehicleOperatingModeReport) {
			VehicleOperatingModeReport m = (VehicleOperatingModeReport)message;
			getActivate().setSelected(m.getSelectFlightPathControlMode().equals(FlightPathControlMode.WAYPOINT));
			getActivate().notifyFeedback();
		}
	}

	/**
	 * This method initializes transferPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTransferPanel() {
		if (transferPanel == null) {
			transferPanel = new JPanel();
			transferPanel.setLayout(new CardLayout());
			transferPanel.add(getIdleTransfer(), getIdleTransfer().getName());
			transferPanel.add(getActiveTransfer(), getActiveTransfer().getName());
		}
		return transferPanel;
	}

	/**
	 * This method initializes idleTransfer	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getIdleTransfer() {
		if (idleTransfer == null) {
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 0;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 0;
			idleTransfer = new JPanel();
			idleTransfer.setLayout(new GridBagLayout());
			idleTransfer.setName("jPanel3");
			idleTransfer.add(getUpload(), gridBagConstraints12);
			idleTransfer.add(getDownload(), gridBagConstraints13);
		}
		return idleTransfer;
	}

	/**
	 * This method initializes upload	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpload() {
		if (upload == null) {
			upload = new JButton();
			upload.setEnabled(false);
			upload.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			upload.setMargin(ViewHelper.getDefaultButtonMargin());
			upload.setToolTipText("Upload mission to vehicle");
			upload.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//cancel any transfer in progress
					vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD);
					
					VerificationResult vr = vehicleControlService.validateMission(getCurrentVehicle().getVehicleID().getVehicleID());
					if(vr.getErrors().size()==0) {
						if(vr.getWarnings().size()>0) {
							if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "There were found warnings in mission validation\n" + vr.toString() + "\n\nConfirm mission upload?")) {
								return;
							}
						} else {
							if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Mission validation PASSED without warnings.\nConfirm mission upload?")) {
								return;
							}
						}

						//confirm mission replace
						if(JOptionPane.OK_OPTION==JOptionPane.showConfirmDialog(null, "Clear existing mission in vehicle before uploading the new mission elements?")) {
							vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.CLEAR_MISSION);
						}
						
						vehicleControlService.sendMissionToVehicle(getCurrentVehicle().getVehicleID().getVehicleID());
					} else {
						JOptionPane.showMessageDialog(null, "Cannot upload mission because there were found errors in mission validation\n" + vr.toString());
					}
				}
			});
		}
		return upload;
	}

	/**
	 * This method initializes download	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDownload() {
		if (download == null) {
			download = new JButton();
			download.setToolTipText("Download vehicle from vehicle");
			download.setMargin(ViewHelper.getDefaultButtonMargin());
			download.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/download.gif")));
			download.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//request operation confirmation
					if(getCurrentVehicle().getMission()!=null) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Current mission will be replaced by downloaded mission. Continue?")) {
							return;
						}
					}
					
					//request download
					vehicleControlService.requestMissionDownload(getCurrentVehicle().getVehicleID().getVehicleID());

					selectedRoute = null;
					selectedPositionWaypoint = null;
					updateGUI();
				}
			});
		}
		return download;
	}

	/**
	 * This method initializes activeTransfer	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getActiveTransfer() {
		if (activeTransfer == null) {
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 1;
			gridBagConstraints19.gridy = 0;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints18.gridy = 0;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.weighty = 0.0;
			gridBagConstraints18.gridx = 0;
			activeTransfer = new JPanel();
			activeTransfer.setLayout(new GridBagLayout());
			activeTransfer.setSize(new Dimension(100, 27));
			activeTransfer.setName("jPanel4");
			activeTransfer.add(getTransferProgress(), gridBagConstraints18);
			activeTransfer.add(getEndTransfer(), gridBagConstraints19);
		}
		return activeTransfer;
	}

	/**
	 * This method initializes transferProgress	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getTransferProgress() {
		if (transferProgress == null) {
			transferProgress = new JProgressBar();
			transferProgress.setPreferredSize(new Dimension(146, 16));
			transferProgress.setStringPainted(true);
		}
		return transferProgress;
	}

	/**
	 * This method initializes endTransfer	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEndTransfer() {
		if (endTransfer == null) {
			endTransfer = new JButton();
			endTransfer.setToolTipText("Cancel current transfer");
			endTransfer.setMargin(ViewHelper.getMinimalButtonMargin());
			endTransfer.setIcon(ICON_CANCEL);
			endTransfer.addActionListener(new ActionListener() {
				//this is used to avoid being stuck in progress screen when vsm is no longer responding (either COMPLETE nor ABORTED)
				private long lastActionTime;
				public void actionPerformed(ActionEvent e) {
					//back to idle panel
					if(!transferCancel || (System.currentTimeMillis()-lastActionTime)<500) {
						((CardLayout)getTransferPanel().getLayout()).show(getTransferPanel(), getIdleTransfer().getName());
						
					//send a cancel transfer message
					} else {
						MissionUploadCommand m = messagingService.resolveMessageForSending(MissionUploadCommand.class);
						m.setMissionPlanMode(MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						m.setMissionID(getCurrentVehicle().getMission().getMissionID());
						messagingService.sendMessage(m);
					}
					lastActionTime = System.currentTimeMillis();
				}
			});
		}
		return endTransfer;
	}
	
}
