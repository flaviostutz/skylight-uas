package br.skylight.cucs.plugins.vehicleconfiguration;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.NumberEditorExt;

import br.skylight.commons.EventType;
import br.skylight.commons.MessageConfiguration;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.MessageSource;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.tables.ButtonCellEditorRenderer;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTableModel;

public class MessagesConfigView extends VehicleView {

	@ServiceInjection
	public MessagingService messagingService;
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="18,12"
	private JXTable messageConfigTable = null;

	private JScrollPane scroll = null;

	private JButton addButton = null;

	private JButton removeButton = null;
	private JButton upload = null;

	private JCheckBox sendMessageConfigurations = null;

	private JButton unscheduleAllConfigurations = null;

	public MessagesConfigView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints3.gridy = 3;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.gridx = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints5.gridy = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.insets = new Insets(3, 2, 0, 0);
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridy = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.gridwidth = 1;
			gridBagConstraints21.anchor = GridBagConstraints.EAST;
			gridBagConstraints21.fill = GridBagConstraints.NONE;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints21.gridy = 3;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(262, 192));
			contents.add(getUpload(), gridBagConstraints21);
			contents.add(getAddButton(), gridBagConstraints);
			contents.add(getRemoveButton(), gridBagConstraints4);
			contents.add(getSendMessageConfigurations(), gridBagConstraints5);
			contents.add(getScroll(), gridBagConstraints6);
			contents.add(getUnscheduleAllConfigurations(), gridBagConstraints3);
			updateGUI();
		}
		return contents;
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		super.onVehicleEvent(av, type);
		if(type.equals(EventType.SELECTED) || (type.equals(EventType.UPDATED) && getCurrentVehicle().equals(av))) {
			((TypedTableModel<MessageConfiguration>)getMessageConfigTable().getModel()).setUserObjects(getCurrentVehicle().getMessageConfigurations());
			getSendMessageConfigurations().setSelected(av.isSendMessageConfigurationsOnConnect());
		}
	}

	protected void updateGUI() {
		enableControls(getCurrentVehicle()!=null);
		getMessageConfigTable().updateUI();	
	}
	
	private void enableControls(boolean enable) {
		if(!enable) {
			getRemoveButton().setEnabled(enable);
		} else {
			getRemoveButton().setEnabled(messageConfigTable.getRowCount()>0 && messageConfigTable.getSelectedRow()!=-1);
		}
		getAddButton().setEnabled(enable);
		getUpload().setEnabled(enable);
		getUnscheduleAllConfigurations().setEnabled(enable);
		getMessageConfigTable().setEnabled(enable);
	}

	/**
	 * This method initializes messageConfigTable	
	 * 	
	 * @return org.jdesktop.swingx.JXTable	
	 */
	private JXTable getMessageConfigTable() {
		if (messageConfigTable == null) {
			messageConfigTable = new JXTable();
			messageConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			messageConfigTable.setShowGrid(true);
			messageConfigTable.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
			messageConfigTable.setEditable(true);
			messageConfigTable.setSortable(false);
			messageConfigTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					getRemoveButton().setEnabled(messageConfigTable.getRowCount()>0 && messageConfigTable.getSelectedRow()!=-1);
				}
			});
			TypedTableModel<MessageConfiguration> model = new TypedTableModel<MessageConfiguration>(new ObjectToColumnAdapter<MessageConfiguration>() {
				@Override
				public Object getValueAt(MessageConfiguration mc, int columnIndex) {
					if(columnIndex==0) {
						return mc.getMessageType();
					} else if(columnIndex==1) {
						return mc.getScheduledFrequency();
					} else if(columnIndex==2) {
						return mc.isRequestOnConnect();
					} else if(columnIndex==3) {
						return mc.isAcknowledgeReceipt();
					} else {
						return null;
					}
				}
				@Override
				public void setValueAt(MessageConfiguration mc, Object value, int columnIndex) {
					if(columnIndex==0) {
						mc.setMessageType((MessageType)value);
					} else if(columnIndex==1) {
						mc.setScheduledFrequency((Float)value);
					} else if(columnIndex==2) {
						mc.setRequestOnConnect((Boolean)value);
					} else if(columnIndex==3) {
						mc.setAcknowledgeReceipt((Boolean)value);
					}
					subscriberService.notifyVehicleEvent(getCurrentVehicle(), EventType.UPDATED, getThis());
				}
			}, "Message type", "Update frequency (Hz)", "Req. on connect", "Acknowledge?", "Action");
			getMessageConfigTable().setModel(model);
			model.setColumnEditables(true,true,true,true,true);
			
			//MESSAGE TYPES COLUMN
			JComboBox cb = new JComboBox();
			DefaultComboBoxModel cm = new DefaultComboBoxModel();
			for (MessageType mt : MessageType.values()) {
				cm.addElement(mt);
			}
			cb.setModel(cm);
			getMessageConfigTable().getColumn(0).setCellEditor(new ComboBoxCellEditor(cb));

			//FREQUENCY COLUMN
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMinimumIntegerDigits(1);
			nf.setMaximumIntegerDigits(3);
			nf.setMinimumFractionDigits(0);
			nf.setMaximumFractionDigits(2);
			getMessageConfigTable().getColumn(1).setCellEditor(new NumberEditorExt(nf));
			
			//ACTION BUTTON
			JButton b = new JButton();
			b.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			b.setToolTipText("Upload to vehicle");
			b.setMargin(ViewHelper.getMinimalButtonMargin());
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							TypedTableModel<MessageConfiguration> model = ((TypedTableModel<MessageConfiguration>)messageConfigTable.getModel());
							MessageConfiguration mc = model.getUserObjects().get(messageConfigTable.getSelectedRow());
							mc.sendConfigurationToVehicle(getCurrentVehicle().getVehicleID().getVehicleID(), messagingService);
						}
					});
				}
			});

			//needed for proper column width control
			messageConfigTable.setAutoCreateColumnsFromModel(false);
			messageConfigTable.setAutoResizeMode(JXTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			messageConfigTable.getColumn(0).setMinWidth(220);
			
			ButtonCellEditorRenderer br = new ButtonCellEditorRenderer(b);
			messageConfigTable.getColumn(4).setCellEditor(br);
			messageConfigTable.getColumn(4).setCellRenderer(br);
		}
		return messageConfigTable;
	}

	private MessagesConfigView getThis() {
		return this;
	}
	
	@Override
	protected String getBaseTitle() {
		return "Messages Configuration";
	}

	/**
	 * This method initializes scroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane();
			scroll.setViewportView(getMessageConfigTable());
		}
		return scroll;
	}

	/**
	 * This method initializes addButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton();
			addButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/add.gif")));
			addButton.setMargin(ViewHelper.getMinimalButtonMargin());
			addButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					MessageConfiguration mc = new MessageConfiguration();
					mc.setMessageType(MessageType.M101);
					getCurrentVehicle().getMessageConfigurations().add(mc);
					updateGUI();
				}
			});
		}
		return addButton;
	}

	/**
	 * This method initializes removeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new JButton();
			removeButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/remove.gif")));
			removeButton.setMargin(ViewHelper.getMinimalButtonMargin());
			removeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getMessageConfigTable().getSelectedRow()!=-1) {
						getCurrentVehicle().getMessageConfigurations().remove(getMessageConfigTable().getSelectedRow());
						updateGUI();
					}
				}
			});
		}
		return removeButton;
	}

	/**
	 * This method initializes sendMessageConfigurations	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSendMessageConfigurations() {
		if (sendMessageConfigurations == null) {
			sendMessageConfigurations = new JCheckBox();
			sendMessageConfigurations.setText("Send message configurations on connect");
			sendMessageConfigurations.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					getCurrentVehicle().setSendMessageConfigurationsOnConnect(sendMessageConfigurations.isSelected());
				}
			});
		}
		return sendMessageConfigurations;
	}
	
	private JButton getUpload() {
		if(upload==null) {
			upload = new JButton();
			upload.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			upload.setToolTipText("Upload all configurations to vehicle");
			upload.setMargin(ViewHelper.getMinimalButtonMargin());
			upload.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							//send configuration messages
							for (MessageConfiguration mc : getCurrentVehicle().getMessageConfigurations()) {
								mc.sendConfigurationToVehicle(getCurrentVehicle().getVehicleID().getVehicleID(), messagingService);
							}
						}
					});
				}
			});
		}
		return upload;
	}

	/**
	 * This method initializes unscheduleAllConfigurations	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUnscheduleAllConfigurations() {
		if (unscheduleAllConfigurations == null) {
			unscheduleAllConfigurations = new JButton();
			unscheduleAllConfigurations.setToolTipText("Unschedule all messages");
			unscheduleAllConfigurations.setMargin(ViewHelper.getMinimalButtonMargin());
			unscheduleAllConfigurations.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/clear.gif")));
			unscheduleAllConfigurations.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (MessageType mt : MessageType.values()) {
						if(mt.isPush()) {
							if(mt.getSource().equals(MessageSource.VSM) || mt.getSource().equals(MessageSource.CUCS_VSM)) {
								MessageConfiguration mc = new MessageConfiguration();
								mc.setMessageType(mt);
								mc.sendConfigurationToVehicle(getCurrentVehicle().getVehicleID().getVehicleID(), messagingService);
							}
						}
					}
				}
			});
		}
		return unscheduleAllConfigurations;
	}
	
}
