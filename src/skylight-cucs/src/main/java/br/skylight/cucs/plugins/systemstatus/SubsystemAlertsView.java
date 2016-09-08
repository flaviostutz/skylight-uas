package br.skylight.cucs.plugins.systemstatus;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import br.skylight.commons.AlertWrapper;
import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AlertType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.tables.ButtonCellEditorRenderer;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTableModel;

public class SubsystemAlertsView extends VehicleView implements MessageListener {

	private static final Logger logger = Logger.getLogger(VehicleView.class.getName());
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	
	private JXTable alertsTable = null;

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	private JScrollPane jScrollPane = null;
	private ThreadWorker refresher;
	
	public SubsystemAlertsView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M1100, this);
		
		//periodic refresh thread
		final Runnable runner = new Runnable() {
			public void run() {
				updateGUI();
			}
		};
		refresher = new ThreadWorker(1) {
			@Override
			public void step() throws Exception {
				//use swing thread to avoid thread 
				//concurrency problems with non thread safe components
				SwingUtilities.invokeLater(runner);
			}
		};
		
		refresher.activate();
	}
	
	@Override
	protected void onDeactivate() throws Exception {
		refresher.deactivate();
	}

	@Override
	public void onMessageReceived(Message message) {
		//M1100
		if(message instanceof SubsystemStatusAlert) {
			updateGUI();
			getRefresh().notifyFeedback();
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridx = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(210, 149));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getJScrollPane(), gridBagConstraints7);
		}
		return contents;
	}

	@Override
	protected String getBaseTitle() {
		return "Alerts";
	}

	@Override
	protected synchronized void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		//update table values
		if(getCurrentVehicle()!=null) {
			getCurrentVehicle().refreshAlerts();
			List<AlertWrapper> a = new ArrayList<AlertWrapper>();
			for (AlertWrapper alertWrapper : getCurrentVehicle().getSubsystemStatusAlerts().values()) {
				a.add(alertWrapper);
			}
			((TypedTableModel<AlertWrapper>)getAlertsTable().getModel()).setUserObjects(a);
		} else {
			((TypedTableModel<SubsystemStatusAlert>)getAlertsTable().getModel()).setUserObjects(new ArrayList<SubsystemStatusAlert>());
		}
		getAlertsTable().updateUI();
	}
	
	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private VehicleMessageRefreshButton getRefresh() {
		if (refresh == null) {
			refresh = new VehicleMessageRefreshButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.setup(subscriberService, messagingService, MessageType.M1100);
		}
		return refresh;
	}

	/**
	 * This method initializes alertsTable	
	 * 	
	 * @return org.jdesktop.swingx.JXTable	
	 */
	private JXTable getAlertsTable() {
		if (alertsTable == null) {
			alertsTable = new JXTable();
			ObjectToColumnAdapter<AlertWrapper> o = new ObjectToColumnAdapter<AlertWrapper>() {
				@Override
				public Object getValueAt(AlertWrapper object, int columnIndex) {
					if(columnIndex==0) {
						return object.getSubsystemStatusAlert().getPriority().toString();
					} else if(columnIndex==1) {
						return object.getSubsystemStatusAlert().getText();
					} else if(columnIndex==2) {
						return object.getSubsystemStatusAlert().getSubsystemID().getName();
					} else if(columnIndex==3) {
						return StringHelper.formatElapsedTime(object.getPriorityTime());
					}
					return null;
				}
				@Override
				public void setValueAt(AlertWrapper object, Object value, int columnIndex) {
				}
			};
			final TypedTableModel<AlertWrapper> t = new TypedTableModel<AlertWrapper>(o, "Priority", "Message", "Subsystem", "Time", "");
			alertsTable.setModel(t);
			t.setColumnEditables(false,false,false,false,true);
			
			JButton b = new JButton("Clear");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							int wid = Math.abs(t.getUserObjects().get(alertsTable.getSelectedRow()).getSubsystemStatusAlert().getWarningID());
							AlertWrapper sa = getCurrentVehicle().getSubsystemStatusAlerts().get(wid);
							if(!sa.getSubsystemStatusAlert().getType().equals(AlertType.NOT_CLEARABLE_BY_OPERATOR)) {
								getCurrentVehicle().getSubsystemStatusAlerts().remove(wid);
								updateGUI();
							} else {
								JOptionPane.showMessageDialog(null, "This alert is not cleareable by user");
							}
						}
					});
				}
			});
			b.setMargin(ViewHelper.getMinimalButtonMargin());

			//important for column sizing control to be effective
			alertsTable.setAutoCreateColumnsFromModel(false);
			alertsTable.setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			
			alertsTable.getColumn(0).setMaxWidth(50);
//			alertsTable.getColumn(1).setMinWidth(300);
			alertsTable.getColumn(2).setMaxWidth(65);
			alertsTable.getColumn(3).setMaxWidth(50);
			alertsTable.getColumn(4).setMaxWidth(35);

			ButtonCellEditorRenderer br = new ButtonCellEditorRenderer(b);
			alertsTable.getColumn(4).setCellEditor(br);
			alertsTable.getColumn(4).setCellRenderer(br);
			
			alertsTable.setHighlighters(HighlighterFactory.createAlternateStriping());

			
			alertsTable.setSortable(false);//sorting causes problem with underlaying model
//			alertsTable.setSortOrder(0, SortOrder.DESCENDING);
//			alertsTable.setSortsOnUpdates(true);
		}
		return alertsTable;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getAlertsTable());
		}
		return jScrollPane;
	}

}
