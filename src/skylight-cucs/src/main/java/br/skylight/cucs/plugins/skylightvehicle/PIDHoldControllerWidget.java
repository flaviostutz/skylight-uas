package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import net.java.games.input.Component;

import org.jfree.data.xy.XYSeriesCollection;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.PIDControllerCommand;
import br.skylight.commons.dli.skylight.PIDControllerState;
import br.skylight.commons.infra.Activable;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.HoldControllerWidget;
import br.skylight.cucs.widgets.HoldControllerWidgetListener;
import br.skylight.cucs.widgets.MessageToChartConverter;
import br.skylight.cucs.widgets.TelemetryChartFrame;

public class PIDHoldControllerWidget extends JPanel implements MessageListener, Activable {

	private static final long serialVersionUID = 1L;
	private static final int MAX_GRAPH_ITEMS = 130;
	
	private JToggleButton more = null;
	private JPanel jPanel = null;
	private JPanel pidPanel = null;
	private JLabel jLabel = null;
	private JSpinner pidP = null;
	private JLabel jLabel1 = null;
	private JSpinner pidI = null;
	private JLabel jLabel2 = null;
	private JSpinner pidD = null;
	private JButton setButton = null;
	private SkylightVehicle skylightVehicle;
	private PIDControl pidControl;  //  @jve:decl-index=0:
	private long graphStartTime;
	
	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public PluginManager pluginManager;
	private HoldControllerWidget holdControllerWidget = null;
	
	public PIDHoldControllerWidget() {
		super();
		initialize();
		getHoldControllerWidget().getGraphButton().setVisible(true);
	}
	
	public void setPidControl(final PIDControl pidControl) {
		this.pidControl = pidControl;
		getHoldControllerWidget().setLabel(pidControl.getName());
		getHoldControllerWidget().setHoldControllerListener(new HoldControllerWidgetListener() {
			@Override
			public void onHoldClicked(double value) {
				PIDControllerCommand m = messagingService.resolveMessageForSending(PIDControllerCommand.class);
				m.setCommandedSetpoint((float)value);
				m.setPIDControl(pidControl);
				m.setVehicleID(skylightVehicle.getVehicleID());
				messagingService.sendMessage(m);
			}
			@Override
			public void onUnholdClicked() {
				PIDControllerCommand m = messagingService.resolveMessageForSending(PIDControllerCommand.class);
				m.setCommandedSetpoint(Float.NaN);
				m.setPIDControl(pidControl);
				m.setVehicleID(skylightVehicle.getVehicleID());
				messagingService.sendMessage(m);
			}
			@Override
			public void onGraphClicked() {
				graphStartTime = System.currentTimeMillis();
				
				final TelemetryChartFrame c1 = CUCSViewHelper.showMultiChart(pidControl.getName(), "Tick", "Value", true, MAX_GRAPH_ITEMS, new MessageToChartConverter() {
					public void addMessageDataToDataset(Message message, XYSeriesCollection ds) {
						if(message instanceof PIDControllerState) {
							PIDControllerState m = (PIDControllerState)message;
							if(m.getPIDControl().equals(pidControl)) {
								long t = System.currentTimeMillis() - graphStartTime;
								ds.getSeries(0).add(t, pidControl.getSetpointMeasureType().convertToTargetUnit(m.getSetpointValue()));
								ds.getSeries(1).add(t, pidControl.getSetpointMeasureType().convertToTargetUnit(m.getFeedbackValue()));
								ds.getSeries(2).add(t, m.getOutputValue());
							}
						}
					}
				}, 
				pidControl.getObjectiveElementName() + " (setpoint)", 
				pidControl.getObjectiveElementName() + " (feedback)", 
				pidControl.getActuationElementName() + " (output)");
				subscriberService.addMessageListener(MessageType.M2011, c1);
				c1.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						subscriberService.removeMessageListener(MessageType.M2011, c1);
					}
				});

				final TelemetryChartFrame c2 = CUCSViewHelper.showMultiChart(pidControl.getName(), "Tick", "Value", true, MAX_GRAPH_ITEMS, new MessageToChartConverter() {
					public void addMessageDataToDataset(Message message, XYSeriesCollection ds) {
						if(message instanceof PIDControllerState) {
							PIDControllerState m = (PIDControllerState)message;
							if(m.getPIDControl().equals(pidControl)) {
								long t = System.currentTimeMillis() - graphStartTime;
								ds.getSeries(0).add(t, m.getProportionalValue());
								ds.getSeries(1).add(t, m.getIntegralValue());
								ds.getSeries(2).add(t, m.getDiferentialValue());
							}
						}
					}
				}, "P", "I", "D");
				subscriberService.addMessageListener(MessageType.M2011, c2);
				c2.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						subscriberService.removeMessageListener(MessageType.M2011, c2);
					}
				});
				c2.setLocation((int)(c2.getLocation().x)+30, (int)(c2.getLocation().y)+30);
			}
			@Override
			public float getControllerValueToHoldValue(float controllerComponentValue, Component component) {
				return 0;
			}
		});
	}
	
	public PIDControl getPidControl() {
		return pidControl;
	}

	@Override
	public void onMessageReceived(Message message) {
		if(message instanceof PIDControllerState) {
			PIDControllerState m = (PIDControllerState)message;
			if(m.getPIDControl().equals(pidControl)) {
				getHoldControllerWidget().setPidData(m);
			}
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.gridx = 1;
		gridBagConstraints10.weightx = 1.0;
		gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints10.gridy = 0;
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		gridBagConstraints9.gridx = 1;
		gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints9.gridy = 1;
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.gridheight = 2;
		gridBagConstraints8.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints8.gridy = 0;
		this.setSize(333, 46);
		this.setLayout(new GridBagLayout());
		this.add(getJPanel(), gridBagConstraints8);
		this.add(getPidPanel(), gridBagConstraints9);
		this.add(getHoldControllerWidget(), gridBagConstraints10);
	}

	/**
	 * This method initializes more	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getMore() {
		if (more == null) {
			more = new JToggleButton();
			more.setText("+");
			more.setMargin(ViewHelper.getMinimalButtonMargin());
			more.setPreferredSize(new Dimension(20, 20));
			more.setMinimumSize(new Dimension(20, 20));
			more.setMaximumSize(new Dimension(20, 20));
			more.setToolTipText("More options");
			more.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					getPidPanel().setVisible(more.isSelected());
				}
			});
		}
		return more;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.NORTH;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(1, 1, 1, 1);
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getMore(), gridBagConstraints);
		}
		return jPanel;
	}

	/**
	 * This method initializes pidPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPidPanel() {
		if (pidPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 6;
			gridBagConstraints7.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 5;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.insets = new Insets(0, 6, 0, 0);
			gridBagConstraints5.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText("D:");
			jLabel2.setFont(new Font("Tahoma", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.insets = new Insets(0, 6, 0, 0);
			gridBagConstraints3.gridy = 0;
			jLabel1 = new JLabel();
			jLabel1.setText("I:");
			jLabel1.setFont(new Font("Tahoma", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.insets = new Insets(0, 26, 0, 0);
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("P:");
			jLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
			pidPanel = new JPanel();
			pidPanel.setLayout(new GridBagLayout());
			pidPanel.add(jLabel, gridBagConstraints1);
			pidPanel.add(getPidP(), gridBagConstraints2);
			pidPanel.add(jLabel1, gridBagConstraints3);
			pidPanel.add(getPidI(), gridBagConstraints4);
			pidPanel.add(jLabel2, gridBagConstraints5);
			pidPanel.add(getPidD(), gridBagConstraints6);
			pidPanel.add(getSetButton(), gridBagConstraints7);
			pidPanel.setVisible(false);
		}
		return pidPanel;
	}

	/**
	 * This method initializes pidP	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getPidP() {
		if (pidP == null) {
			pidP = new JSpinner(new SpinnerNumberModel(0.0, -999.9, 999.9, 0.1));
			pidP.setPreferredSize(new Dimension(40, 20));
			pidP.setToolTipText("Proportional part");
			CUCSViewHelper.setDefaultActionClick(pidP, getSetButton());
		}
		return pidP;
	}

	/**
	 * This method initializes pidI	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getPidI() {
		if (pidI == null) {
			pidI = new JSpinner(new SpinnerNumberModel(0.0, -999.9, 999.9, 0.1));
			pidI.setPreferredSize(new Dimension(40, 20));
			pidI.setToolTipText("Integral part");
			CUCSViewHelper.setDefaultActionClick(pidI, getSetButton());
		}
		return pidI;
	}

	/**
	 * This method initializes pidD	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getPidD() {
		if (pidD == null) {
			pidD = new JSpinner(new SpinnerNumberModel(0.0, -999.9, 999.9, 0.1));
			pidD.setPreferredSize(new Dimension(40, 20));
			pidD.setToolTipText("Differential part");
			CUCSViewHelper.setDefaultActionClick(pidD, getSetButton());
		}
		return pidD;
	}

	/**
	 * This method initializes setButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	public JButton getSetButton() {
		if (setButton == null) {
			setButton = new JButton();
			setButton.setToolTipText("Send PID configuration to vehicle");
			setButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			setButton.setMargin(ViewHelper.getMinimalButtonMargin());
			setButton.setPreferredSize(new Dimension(20, 20));
			setButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PIDConfiguration m = messagingService.resolveMessageForSending(PIDConfiguration.class);
					m.setPIDControl(pidControl);
					m.setKp(((Double)getPidP().getValue()).floatValue());
					m.setKi(((Double)getPidI().getValue()).floatValue());
					m.setKd(((Double)getPidD().getValue()).floatValue());
					m.setVehicleID(skylightVehicle.getVehicleID());
					messagingService.sendMessage(m);
				}
			});
		}
		return setButton;
	}

	@Override
	public void activate() throws Exception {
		pluginManager.manageObject(getHoldControllerWidget());
	}

	@Override
	public void deactivate() throws Exception {
		pluginManager.unmanageObject(getHoldControllerWidget());
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	public void updateGUI() {
		if(skylightVehicle!=null && skylightVehicle.getSkylightVehicleConfiguration()!=null) {
			PIDConfiguration pc = (skylightVehicle.getSkylightVehicleConfiguration()).getPIDConfiguration(getPidControl());
			getPidP().setValue((double)pc.getKp());
			getPidI().setValue((double)pc.getKi());
			getPidD().setValue((double)pc.getKd());
		}
	}
	
	public void setSkylightVehicle(SkylightVehicle skylightVehicle) {
		this.skylightVehicle = skylightVehicle;
	}
	public SkylightVehicle getSkylightVehicle() {
		return skylightVehicle;
	}
	
	/**
	 * This method initializes holdControllerWidget	
	 * 	
	 * @return br.skylight.cucs.widgets.HoldControllerWidget	
	 */
	public HoldControllerWidget getHoldControllerWidget() {
		if (holdControllerWidget == null) {
			holdControllerWidget = new HoldControllerWidget();
		}
		return holdControllerWidget;
	}

	public static void main(String[] args) {
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(new PIDHoldControllerWidget());
		JFrame f = new JFrame();
		f.add(scroll, BorderLayout.CENTER);
		f.setVisible(true);
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public void init() throws Exception {
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
