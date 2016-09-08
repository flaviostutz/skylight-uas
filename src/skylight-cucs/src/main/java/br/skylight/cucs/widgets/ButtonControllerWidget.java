package br.skylight.cucs.widgets;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.cucs.plugins.gamecontroller.BinaryValueResolver;
import br.skylight.cucs.plugins.gamecontroller.ControllerBinding;
import br.skylight.cucs.plugins.gamecontroller.ControllerBindingDefinition;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;

public class ButtonControllerWidget extends JPanel {

	private static final long serialVersionUID = -5415965421819680798L;

	private JButton button = null;
	private JButton joystickButton = null;
	private boolean doClick = true;
	private GameControllerService gameControllerService;  //  @jve:decl-index=0:
	private ControllerBinding controllerBinding;  //  @jve:decl-index=0:
	
	public ButtonControllerWidget() {
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 7;
		gridBagConstraints4.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints4.weighty = 1.0;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.weightx = 1.0;
		gridBagConstraints4.anchor = GridBagConstraints.WEST;
		gridBagConstraints4.gridy = 0;
		setLayout(new GridBagLayout());
		this.setSize(new Dimension(101, 25));
		this.add(getButton(), gridBagConstraints4);
		this.add(getJoystickButton(), gridBagConstraints);
	}
	
	public void setupGameController(GameControllerService gameControllerService, int bindingDefinitionId) {
		this.gameControllerService = gameControllerService;
		this.controllerBinding = new ControllerBinding();
		getJoystickButton().setVisible(gameControllerService!=null);
		if(gameControllerService!=null) {
			controllerBinding.setDefinitionId(bindingDefinitionId);
			controllerBinding.setControllerBindingDefinition(new ControllerBindingDefinition(bindingDefinitionId, "Button " + bindingDefinitionId, new BinaryValueResolver()) {
				@Override
				public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
					getButton().doClick();
				}
			});
			gameControllerService.addCustomBinding(controllerBinding);
		}
	}
	
	/**
	 * This method initializes holdButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	public JButton getButton() {
		if (button == null) {
			button = new JButton();
			button.setText("Action");
			button.setMargin(ViewHelper.getMinimalButtonMargin());
			button.setFont(new Font("Dialog", Font.PLAIN, 12));
			button.setPreferredSize(new Dimension(20, 20));
		}
		return button;
	}

	/**
	 * This method initializes joystickButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJoystickButton() {
		if (joystickButton == null) {
			joystickButton = new JButton();
			joystickButton.setMargin(ViewHelper.getMinimalButtonMargin());
			joystickButton.setPreferredSize(new Dimension(20, 20));
			joystickButton.setToolTipText("Set game controller");
			joystickButton.setVisible(false);
			joystickButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/groundstation/images/joystick.gif")));
			joystickButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					gameControllerService.showControllerBindingDialog(controllerBinding.getDefinitionId());
				}
			});
		}
		return joystickButton;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
