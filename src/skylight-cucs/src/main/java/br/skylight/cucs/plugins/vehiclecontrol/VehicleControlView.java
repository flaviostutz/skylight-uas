package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.ControllableElement;
import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.MissionListener;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;

public class VehicleControlView extends View<VehicleControlState> implements VehicleControlListener, MissionListener {

	private JPanel contents = null;  //  @jve:decl-index=0:visual-constraint="20,20"
	private JXTreeTable treeTable;
	private JPopupMenu treePopupMenu;
	
	private ControllableElement selectedControllableElement;
	private CUCS selectedCUCS;
	
	@ServiceInjection
	public UserService userService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;

	@ServiceInjection
	public SubscriberService subscriberService;
	
	@ServiceInjection
	public PluginManager pluginManager;

	@ServiceInjection
	public MessagingService messagingService;  //  @jve:decl-index=0:
	private FeedbackButton refresh = null;
	
	public VehicleControlView(ViewExtensionPoint ep) {
		super(ep);
		setTitleText("Vehicle Control");
	}

	@Override
	protected void onActivate() throws Exception {
		vehicleControlService.addListener(this);
	}

	@Override
	protected void onDeactivate() throws Exception {
		vehicleControlService.removeListener(this);
	}
	
	@Override
	protected VehicleControlState instantiateState() {
		return new VehicleControlState();
	}

	@Override
	protected void onStateUpdated() {
		//TODO select last vehicle
	}

	@Override
	protected void prepareState() {
		if(subscriberService.getLastSelectedVehicle()!=null) {
			getState().setLastSelectedVehicleId(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
		}
	}

	/**
	 * This method initializes contents
	 * 
	 * @return javax.swing.JPanel
	 */
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints14.anchor = GridBagConstraints.EAST;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.fill = GridBagConstraints.NONE;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weighty = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.CENTER;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(287, 173));
			JScrollPane scrollpane = new JScrollPane(getTreeTable());
			contents.add(scrollpane, gridBagConstraints1);
			contents.add(getRefresh(), gridBagConstraints14);
		}
		return contents;
	}

	private JXTreeTable getTreeTable() {
		if(treeTable==null) {
			TreeTableModel treeTableModel = new VehicleControlTreeModel(userService);
			treeTable = new JXTreeTable(treeTableModel);
			treeTable.setColumnSelectionAllowed(false);
			treeTable.setColumnControlVisible(true);
			treeTable.setExpandsSelectedPaths(true);
	        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			treeTable.setScrollsOnExpand(true);
			treeTable.setEditable(false);
			treeTable.add(getTreePopupMenu());
			treeTable.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
			treeTable.addMouseListener(new JPopupMenuMouseListener(getTreePopupMenu()));
			treeTable.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					selectedControllableElement = null;
					selectedCUCS = null;
					if(e.getPath().getLastPathComponent() instanceof TreeTableNode) {
						Object o = ((TreeTableNode)e.getPath().getLastPathComponent()).getUserObject();
						if(o instanceof ControllableElement) {
							selectedControllableElement = (ControllableElement)o;
							if(selectedControllableElement instanceof Vehicle) {
								Vehicle v = (Vehicle)selectedControllableElement;
								subscriberService.notifyVehicleEvent(v, EventType.SELECTED, null);
							} else if(selectedControllableElement instanceof Payload) {
								subscriberService.notifyPayloadEvent((Payload)selectedControllableElement, EventType.SELECTED, null);
							}
						} else if (o instanceof CUCS) {
							selectedCUCS = (CUCS)o;
						}
					}
				}
			});
			treeTable.setAutoCreateColumnsFromModel(false);
			treeTable.setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			treeTable.getColumn(0).setMinWidth(180);
			
			onVehiclesUpdated(vehicleControlService.getKnownVehicles(), vehicleControlService.getKnownCUCS());
		}
		return treeTable;
	}
	
	private JPopupMenu getTreePopupMenu() {
		if(treePopupMenu==null) {
			treePopupMenu = new JPopupMenu();
			treePopupMenu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					treePopupMenu.removeAll();
					if(selectedControllableElement!=null) {
						CUCSControl cc = selectedControllableElement.resolveCUCSControl(userService.getCurrentCucsId());
						
						//REQUEST CONTROL
						JMenuItem mi = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								ControlRequestDialog d = new ControlRequestDialog(null, selectedControllableElement, messagingService, userService);
								d.setVisible(true);
							}
						});
						if(cc.getGrantedLOIs().getLOIs().size()==0) {
							mi.setText("Request control...");
						} else {
							mi.setText("Change control...");
						}
						treePopupMenu.add(mi);
						
						//RELINQUISH CONTROL
						if(cc.getGrantedLOIs().getLOIs().size()>0) {
							mi = new JMenuItem(new AbstractAction() {
								public void actionPerformed(ActionEvent ae) {
									ControlRelinquishDialog d = new ControlRelinquishDialog(null, selectedControllableElement, messagingService, userService);
									d.setVisible(true);
								}
							});
							mi.setText("Relinquish control...");
							treePopupMenu.add(mi);
						}
						
						treePopupMenu.add(new JSeparator());

						//EDIT LABEL
						mi = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								String name = JOptionPane.showInputDialog("Enter name:");
								if(name!=null) {
									selectedControllableElement.setName(name);
									getTreeTable().updateUI();
								}
							}
						});
						mi.setText("Set name...");
						treePopupMenu.add(mi);
						
					} else if(selectedCUCS!=null) {
						JMenuItem mi = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								JOptionPane.showMessageDialog(getThis().getComponent(), "SHOW INFO " + selectedControllableElement);
							}
						});
						mi.setText("Show info");
						treePopupMenu.add(mi);
					}
				}
				public void popupMenuCanceled(PopupMenuEvent arg0) {}
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
			});
		}
		return treePopupMenu;
	}

	private VehicleControlView getThis() {
		return this;
	}
	
	@Override
	public void onVehiclesUpdated(Map<Integer, Vehicle> knownVehicles, Map<Integer,CUCS> knownCUCS) {
		VehicleControlTreeModel tm = (VehicleControlTreeModel)getTreeTable().getTreeTableModel();
		tm.updateVehiclesAndOperators(knownVehicles, knownCUCS);
		getTreeTable().updateUI();
		getTreeTable().expandAll();
//		getTreeTable().repaint();
		getRefresh().notifyFeedback();
	}

	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getRefresh() {
		if (refresh == null) {
			refresh = new FeedbackButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					vehicleControlService.requestVehicleInfos();
				}
			});
		}
		return refresh;
	}

	@Override
	public void onMissionEvent(Mission mission, EventType type) {
		CUCSViewHelper.selectTypedTreeTableRow(getTreeTable(), mission.getVehicle());
	}

	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type) {
		CUCSViewHelper.selectTypedTreeTableRow(getTreeTable(), mission.getVehicle());
	}
	
}
