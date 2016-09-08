package br.skylight.cucs.plugins.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.EngineStatus;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.EngineCommand;
import br.skylight.commons.dli.vehicle.EngineOperatingStates;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.RoundButton;
import java.awt.Dimension;

public class EngineControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel speedLabel = null;
	private JLabel jLabel1 = null;
	private JLabel powerLabel = null;
	private JLabel jLabel3 = null;
	private RoundButton speed = null;
	private RoundButton body = null;
	private RoundButton shaft = null;
	private RoundButton exhaust = null;
	private RoundButton coolant = null;
	private RoundButton lubricantPres = null;
	private RoundButton lubricantTemp = null;
	private RoundButton fire = null;

	private EngineOperatingStates engineOperatingStates;  //  @jve:decl-index=0:
	private MessagingService messagingService;  //  @jve:decl-index=0:
	private RoundButton enableEngine = null;
	private RoundButton startEngine = null;
	private RoundButton stopEngine = null;
	private JPanel jPanel = null;
	
	/**
	 * This is the default constructor
	 */
	public EngineControlPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
		gridBagConstraints17.gridx = 0;
		gridBagConstraints17.gridwidth = 2;
		gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints17.gridy = 6;
		GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
		gridBagConstraints16.gridx = 1;
		gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints16.weightx = 1.0;
		gridBagConstraints16.insets = new Insets(0, 0, 0, 1);
		gridBagConstraints16.gridy = 3;
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		gridBagConstraints15.gridx = 0;
		gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints15.weightx = 1.0;
		gridBagConstraints15.insets = new Insets(0, 1, 1, 0);
		gridBagConstraints15.gridy = 5;
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		gridBagConstraints14.gridx = 1;
		gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints14.weightx = 1.0;
		gridBagConstraints14.insets = new Insets(0, 0, 1, 1);
		gridBagConstraints14.gridy = 5;
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		gridBagConstraints13.gridx = 0;
		gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints13.weightx = 1.0;
		gridBagConstraints13.insets = new Insets(0, 1, 0, 0);
		gridBagConstraints13.gridy = 4;
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 1;
		gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.weightx = 1.0;
		gridBagConstraints12.insets = new Insets(0, 0, 0, 1);
		gridBagConstraints12.gridy = 4;
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints11.weightx = 1.0;
		gridBagConstraints11.insets = new Insets(0, 1, 0, 0);
		gridBagConstraints11.gridy = 3;
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.gridx = 1;
		gridBagConstraints10.weightx = 1.0;
		gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints10.insets = new Insets(0, 0, 0, 1);
		gridBagConstraints10.gridy = 2;
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.weightx = 1.0;
		gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints9.insets = new Insets(0, 1, 0, 0);
		gridBagConstraints9.gridy = 2;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 1;
		gridBagConstraints3.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints3.gridy = 1;
		jLabel3 = new JLabel();
		jLabel3.setText("Power setting");
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.insets = new Insets(5, 0, 0, 0);
		gridBagConstraints2.gridy = 0;
		powerLabel = new JLabel();
		powerLabel.setText("0 %");
		powerLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.insets = new Insets(0, 5, 0, 0);
		gridBagConstraints1.gridy = 1;
		jLabel1 = new JLabel();
		jLabel1.setText("Speed (rpm)");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.insets = new Insets(5, 0, 0, 0);
		gridBagConstraints.gridy = 0;
		speedLabel = new JLabel();
		speedLabel.setText("0");
		speedLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		this.setSize(160, 149);
		this.setLayout(new GridBagLayout());
		this.add(speedLabel, gridBagConstraints);
		this.add(jLabel1, gridBagConstraints1);
		this.add(powerLabel, gridBagConstraints2);
		this.add(jLabel3, gridBagConstraints3);
		this.add(getSpeed(), gridBagConstraints9);
		this.add(getBody(), gridBagConstraints10);
		this.add(getShaft(), gridBagConstraints11);
		this.add(getExhaust(), gridBagConstraints12);
		this.add(getCoolant(), gridBagConstraints13);
		this.add(getLubricantPres(), gridBagConstraints14);
		this.add(getLubricantTemp(), gridBagConstraints15);
		this.add(getFire(), gridBagConstraints16);
		this.add(getJPanel(), gridBagConstraints17);
	}

	/**
	 * This method initializes speed	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getSpeed() {
		if (speed == null) {
			speed = new RoundButton();
			speed.setText("Speed");
			speed.setRoundness(10);
			speed.setRaiseLevel(1);
			speed.setMargin(new Insets(0,0,0,0));
		}
		return speed;
	}

	/**
	 * This method initializes body	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getBody() {
		if (body == null) {
			body = new RoundButton();
			body.setText("Body temp");
			body.setRoundness(10);
			body.setRaiseLevel(1);
			body.setMargin(new Insets(0,0,0,0));
		}
		return body;
	}

	/**
	 * This method initializes shaft	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getShaft() {
		if (shaft == null) {
			shaft = new RoundButton();
			shaft.setMargin(new Insets(0, 0, 0, 0));
			shaft.setRaiseLevel(1);
			shaft.setRoundness(10);
			shaft.setText("Shaft torque");
		}
		return shaft;
	}

	/**
	 * This method initializes exhaust	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getExhaust() {
		if (exhaust == null) {
			exhaust = new RoundButton();
			exhaust.setMargin(new Insets(0, 0, 0, 0));
			exhaust.setRaiseLevel(1);
			exhaust.setRoundness(10);
			exhaust.setText("Exhaust temp");
		}
		return exhaust;
	}

	/**
	 * This method initializes coolant	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getCoolant() {
		if (coolant == null) {
			coolant = new RoundButton();
			coolant.setMargin(new Insets(0, 0, 0, 0));
			coolant.setRaiseLevel(1);
			coolant.setRoundness(10);
			coolant.setText("Coolant temp");
		}
		return coolant;
	}

	/**
	 * This method initializes lubricantPres	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getLubricantPres() {
		if (lubricantPres == null) {
			lubricantPres = new RoundButton();
			lubricantPres.setMargin(new Insets(0, 0, 0, 0));
			lubricantPres.setRaiseLevel(1);
			lubricantPres.setRoundness(10);
			lubricantPres.setText("Lubricant pres");
		}
		return lubricantPres;
	}

	/**
	 * This method initializes lubricantTemp	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getLubricantTemp() {
		if (lubricantTemp == null) {
			lubricantTemp = new RoundButton();
			lubricantTemp.setMargin(new Insets(0, 0, 0, 0));
			lubricantTemp.setRaiseLevel(1);
			lubricantTemp.setRoundness(10);
			lubricantTemp.setText("Lubricant temp");
		}
		return lubricantTemp;
	}

	/**
	 * This method initializes fire	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getFire() {
		if (fire == null) {
			fire = new RoundButton();
			fire.setMargin(new Insets(0, 0, 0, 0));
			fire.setRaiseLevel(1);
			fire.setRoundness(10);
			fire.setText("Fire");
		}
		return fire;
	}
	
	public void setEngineOperatingStates(EngineOperatingStates m) {
		this.engineOperatingStates = m;
		if(m.equals(EngineStatus.ENABLED_RUNNING)) {
			getEnableEngine().setSelected(true);
			getStartEngine().setSelected(false);
			getStopEngine().setSelected(false);
		} else if(m.equals(EngineStatus.STARTED)) {
			getEnableEngine().setSelected(false);
			getStartEngine().setSelected(true);
			getStopEngine().setSelected(false);
		} else if(m.equals(EngineStatus.STOPPED)) {
			getEnableEngine().setSelected(false);
			getStartEngine().setSelected(false);
			getStopEngine().setSelected(true);
		}
		speedLabel.setText(""+(int)(60F*(m.getEngineSpeed()/MathHelper.TWO_PI)));
		powerLabel.setText((int)m.getEnginePowerSetting()+" %");
		CUCSViewHelper.setupButtonForEngineStatus(getSpeed(), m.getEngineSpeedStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getBody(), m.getEngineBodyTemperatureStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getShaft(), m.getOutputPowerStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getCoolant(), m.getCoolantTemperatureStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getLubricantTemp(), m.getLubricantTemperatureStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getFire(), m.getFireDetectionSensorStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getExhaust(), m.getExhaustGasTemperatureStatus());
		CUCSViewHelper.setupButtonForEngineStatus(getLubricantPres(), m.getLubricantPressureStatus());
	}

	/**
	 * This method initializes enableEngine	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getEnableEngine() {
		if (enableEngine == null) {
			enableEngine = new RoundButton();
			enableEngine.setRoundness(10);
			enableEngine.setText("Enable");
			enableEngine.setColorUnselected(Color.LIGHT_GRAY);
			enableEngine.setColorSelected(Color.ORANGE);
			enableEngine.setToolTipText("Enable engine for being started");
			enableEngine.setMargin(ViewHelper.getDefaultButtonMargin());
			enableEngine.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EngineCommand m = messagingService.resolveMessageForSending(EngineCommand.class);
					m.setEngineNumber(engineOperatingStates.getEngineNumber());
					m.setVehicleID(engineOperatingStates.getVehicleID());
					m.setEngineCommand(EngineStatus.ENABLED_RUNNING);
					messagingService.sendMessage(m);
				}
			});
		}
		return enableEngine;
	}

	/**
	 * This method initializes startEngine	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getStartEngine() {
		if (startEngine == null) {
			startEngine = new RoundButton();
			startEngine.setToolTipText("Start engine");
			startEngine.setText("Start");
			startEngine.setColorUnselected(Color.LIGHT_GRAY);
			startEngine.setColorSelected(Color.ORANGE);
			startEngine.setRoundness(10);
			startEngine.setMargin(ViewHelper.getDefaultButtonMargin());
			startEngine.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EngineCommand m = messagingService.resolveMessageForSending(EngineCommand.class);
					m.setEngineNumber(engineOperatingStates.getEngineNumber());
					m.setVehicleID(engineOperatingStates.getVehicleID());
					m.setEngineCommand(EngineStatus.STARTED);
					messagingService.sendMessage(m);
				}
			});
		}
		return startEngine;
	}

	/**
	 * This method initializes stopEngine	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getStopEngine() {
		if (stopEngine == null) {
			stopEngine = new RoundButton();
			stopEngine.setToolTipText("Stop engine");
			stopEngine.setText("Stop");
			stopEngine.setColorUnselected(Color.LIGHT_GRAY);
			stopEngine.setColorSelected(Color.ORANGE);
			stopEngine.setRoundness(10);
			stopEngine.setMargin(ViewHelper.getDefaultButtonMargin());
			stopEngine.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EngineCommand m = messagingService.resolveMessageForSending(EngineCommand.class);
					m.setEngineNumber(engineOperatingStates.getEngineNumber());
					m.setVehicleID(engineOperatingStates.getVehicleID());
					m.setEngineCommand(EngineStatus.STOPPED);
					messagingService.sendMessage(m);
				}
			});
		}
		return stopEngine;
	}
	
	public void setMessagingService(MessagingService messagingService) {
		this.messagingService = messagingService;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.insets = new Insets(3, 0, 3, 3);
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.insets = new Insets(3, 0, 3, 3);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints4.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getEnableEngine(), gridBagConstraints4);
			jPanel.add(getStartEngine(), gridBagConstraints5);
			jPanel.add(getStopEngine(), gridBagConstraints6);
		}
		return jPanel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
