package br.skylight.cucs.plugins.gamecontroller;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import br.skylight.commons.ViewHelper;
import br.skylight.cucs.widgets.JMeasureSpinner;

public class GameControllerComponentSelector extends JDialog implements GameControllerComponentListener {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	
	private JLabel jLabel = null;
	private long showDialogTime;

	private JLabel jLabel1 = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JLabel jLabel6 = null;

	private JLabel controllerName = null;

	private JLabel componentName = null;

	private JLabel currentValue = null;

	private JLabel userControlName = null;

	private JButton okButton = null;

	private JButton cancelButton = null;

	private JPanel jPanel = null;
	private JButton clearSelectionButton = null;
	
	private boolean wasCancelled;
	private JCheckBox inverseCheckbox = null;

	private GameControllerService gameControllerService;
	private ControllerBinding binding;  //  @jve:decl-index=0:
	private ControllerBinding tempBinding = new ControllerBinding();  //  @jve:decl-index=0:
	private ControllerBindingDefinition bindingDefinition;  //  @jve:decl-index=0:

	private JCheckBox incrementalCheckbox = null;

	private JCheckBox exponentialCheckbox = null;

	private JLabel jLabel7 = null;

	private JMeasureSpinner<Integer> autoUpdateTime = null;
	
	/**
	 * @param owner
	 */
	public GameControllerComponentSelector(Window owner, GameControllerService gameControllerService) {
		super(owner);
		this.gameControllerService = gameControllerService;
		initialize();
		ViewHelper.centerWindow(this);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(270, 347);
		this.setTitle("Controller component selection");
		this.setContentPane(getJContentPane());
		this.setModal(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent arg0) {
				try {
					gameControllerService.removeListener(getThis());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private GameControllerComponentSelector getThis() {
		return this;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.insets = new Insets(0, 4, 0, 0);
			gridBagConstraints21.gridy = 11;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints15.gridy = 11;
			jLabel7 = new JLabel();
			jLabel7.setText("Auto update time (ms):");
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 1;
			gridBagConstraints61.anchor = GridBagConstraints.WEST;
			gridBagConstraints61.gridy = 10;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.gridy = 9;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 8;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridwidth = 1;
			gridBagConstraints13.insets = new Insets(4, 4, 0, 0);
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 12;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridwidth = 2;
			gridBagConstraints31.insets = new Insets(7, 0, 6, 0);
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.gridy = 13;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.insets = new Insets(3, 0, 0, 6);
			gridBagConstraints9.gridy = 4;
			userControlName = new JLabel();
			userControlName.setText("-");
			userControlName.setFont(new Font("Dialog", Font.ITALIC, 12));
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.insets = new Insets(3, 0, 0, 6);
			gridBagConstraints8.weighty = 0.0;
			gridBagConstraints8.anchor = GridBagConstraints.NORTH;
			gridBagConstraints8.gridy = 7;
			currentValue = new JLabel();
			currentValue.setText("-");
			currentValue.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.insets = new Insets(3, 0, 0, 6);
			gridBagConstraints7.gridy = 6;
			componentName = new JLabel();
			componentName.setText("NONE");
			componentName.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.insets = new Insets(3, 0, 0, 6);
			gridBagConstraints6.gridy = 5;
			controllerName = new JLabel();
			controllerName.setText("NONE");
			controllerName.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints5.insets = new Insets(3, 6, 0, 4);
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.gridy = 7;
			jLabel6 = new JLabel();
			jLabel6.setText("Current value:");
			jLabel6.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(3, 6, 0, 4);
			gridBagConstraints4.gridy = 6;
			jLabel5 = new JLabel();
			jLabel5.setText("Component:");
			jLabel5.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(3, 6, 0, 4);
			gridBagConstraints3.gridy = 5;
			jLabel4 = new JLabel();
			jLabel4.setText("Game controller:");
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(3, 6, 0, 4);
			gridBagConstraints2.gridy = 4;
			jLabel3 = new JLabel();
			jLabel3.setText("Control:");
			jLabel3.setFont(new Font("Dialog", Font.ITALIC, 12));
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridwidth = 2;
			gridBagConstraints11.insets = new Insets(3, 5, 8, 5);
			gridBagConstraints11.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("gesture to a control");
			jLabel2.setFont(new Font("Dialog", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints1.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("in a desired direction to associate this");
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.insets = new Insets(9, 5, 0, 5);
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Press a controller button or move a stick");
			jLabel.setFont(new Font("Dialog", Font.BOLD, 12));
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabel, gridBagConstraints);
			jContentPane.add(jLabel1, gridBagConstraints1);
			jContentPane.add(jLabel2, gridBagConstraints11);
			jContentPane.add(jLabel3, gridBagConstraints2);
			jContentPane.add(jLabel4, gridBagConstraints3);
			jContentPane.add(jLabel5, gridBagConstraints4);
			jContentPane.add(jLabel6, gridBagConstraints5);
			jContentPane.add(controllerName, gridBagConstraints6);
			jContentPane.add(componentName, gridBagConstraints7);
			jContentPane.add(currentValue, gridBagConstraints8);
			jContentPane.add(userControlName, gridBagConstraints9);
			jContentPane.add(getJPanel(), gridBagConstraints31);
			jContentPane.add(getClearSelectionButton(), gridBagConstraints13);
			jContentPane.add(getInverseCheckbox(), gridBagConstraints14);
			jContentPane.add(getIncrementalCheckbox(), gridBagConstraints41);
			jContentPane.add(getExponentialCheckbox(), gridBagConstraints61);
			jContentPane.add(jLabel7, gridBagConstraints15);
			jContentPane.add(getAutoUpdateTime(), gridBagConstraints21);
		}
		return jContentPane;
	}
	
	public void showDialog(ControllerBindingDefinition bindingDefinition, ControllerBinding binding) {
		this.bindingDefinition = bindingDefinition;
		this.binding = binding;
		tempBinding.copyFrom(binding);
		userControlName.setText(bindingDefinition.getName());
		showDialogTime = System.currentTimeMillis();
		gameControllerService.addListener(this);
		wasCancelled = true;
		updateGUI();
		setVisible(true);
	}
	
	public void updateGUI() {
		if(tempBinding.isActive() && tempBinding.getControllerName()!=null && tempBinding.getControllerName().trim().length()>0) {
			controllerName.setText(tempBinding.getControllerName());
		} else {
			controllerName.setText("NONE");
		}
		if(tempBinding.isActive() && tempBinding.getComponentName()!=null && tempBinding.getComponentName().trim().length()>0) {
			componentName.setText(tempBinding.getComponentName());
		} else {
			componentName.setText("NONE");
		}
		getInverseCheckbox().setSelected(tempBinding.isInverse());
		getIncrementalCheckbox().setSelected(tempBinding.isIncremental());
		getExponentialCheckbox().setSelected(tempBinding.isExponential());
		getAutoUpdateTime().setValue(tempBinding.getTimeAutoTriggerWhileTraveling());
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					tempBinding.setTimeAutoTriggerWhileTraveling(getAutoUpdateTime().getValue());
					//look for other controllers that already are bound to the selected controller component
					for (ControllerBinding cb : gameControllerService.getCurrentControllerProfile().getBindings()) {
//						System.out.println(cb.getDefinitionId() + " " + cb.getControllerName() + " " + cb.getComponentName() + " " + cb.isActive() + " " + binding.isActive());
						if(cb.getDefinitionId()!=binding.getDefinitionId() && cb.isActive() && tempBinding.isActive()) {
							if(cb.getControllerName().equals(tempBinding.getControllerName()) && cb.getComponentName().equals(tempBinding.getComponentName())) {
								int r = JOptionPane.showConfirmDialog(null, "Control '" + cb.getControllerBindingDefinition().getName() + "' is already bound to the same controller component you've selected.\nDo you want to remove that binding?");
								if(r==JOptionPane.YES_OPTION) {
									cb.setActive(false);
								} else if(r==JOptionPane.CANCEL_OPTION) {
									return;
								}
							}
						}
					}
					binding.copyFrom(tempBinding);//commit
					gameControllerService.reloadSteerings();
					wasCancelled = false;
					setVisible(false);
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					wasCancelled = true;
					setVisible(false);
				}
			});
			
		}
		return cancelButton;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.insets = new Insets(0, 6, 0, 0);
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.insets = new Insets(0, 0, 0, 6);
			gridBagConstraints10.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getOkButton(), gridBagConstraints10);
			jPanel.add(getCancelButton(), gridBagConstraints12);
		}
		return jPanel;
	}

	@Override
	public void onComponentValueChanged(Controller controller, Component component, float value, double resolvedValue) {
		//avoid the first update values when this dialog is shown
		if((System.currentTimeMillis()-showDialogTime)>500) {
			//filter readings
			if(Math.abs(value)>0.2) {
				if(tempBinding.isInverse()) {
					value = -value;
				}
				currentValue.setText(value + "");
				tempBinding.setActive(true);
				tempBinding.setControllerName(controller.getName());
				tempBinding.setComponentName(component.getIdentifier().getName());
				updateGUI();
			}
		}
	}

	/**
	 * This method initializes clearSelectionButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getClearSelectionButton() {
		if (clearSelectionButton == null) {
			clearSelectionButton = new JButton();
			clearSelectionButton.setText("Clear selection");
			clearSelectionButton.setFont(new Font("Dialog", Font.PLAIN, 10));
			clearSelectionButton.setMargin(ViewHelper.getMinimalButtonMargin());
			clearSelectionButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					controllerName.setText("NONE");
					componentName.setText("NONE");
					currentValue.setText("-");
					tempBinding.setActive(false);
				}
			});
		}
		return clearSelectionButton;
	}

	/**
	 * This method initializes inverseCheckbox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInverseCheckbox() {
		if (inverseCheckbox == null) {
			inverseCheckbox = new JCheckBox();
			inverseCheckbox.setText("inverse");
			inverseCheckbox.setFont(new Font("Dialog", Font.PLAIN, 12));
			inverseCheckbox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					tempBinding.setInverse(inverseCheckbox.isSelected());
				}
			});
		}
		return inverseCheckbox;
	}

	public boolean wasCancelled() {
		return wasCancelled;
	}

	/**
	 * This method initializes incrementalCheckbox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getIncrementalCheckbox() {
		if (incrementalCheckbox == null) {
			incrementalCheckbox = new JCheckBox();
			incrementalCheckbox.setFont(new Font("Dialog", Font.PLAIN, 12));
			incrementalCheckbox.setText("incremental mode");
			incrementalCheckbox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					tempBinding.setIncremental(incrementalCheckbox.isSelected());
				}
			});
		}
		return incrementalCheckbox;
	}

	/**
	 * This method initializes exponentialCheckbox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getExponentialCheckbox() {
		if (exponentialCheckbox == null) {
			exponentialCheckbox = new JCheckBox();
			exponentialCheckbox.setFont(new Font("Dialog", Font.PLAIN, 12));
			exponentialCheckbox.setText("use exponential curve");
			exponentialCheckbox.setToolTipText("This controller will use a exponential response curve");
			exponentialCheckbox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					tempBinding.setExponential(exponentialCheckbox.isSelected());
				}
			});
		}
		return exponentialCheckbox;
	}

	/**
	 * This method initializes autoUpdateTime	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getAutoUpdateTime() {
		if (autoUpdateTime == null) {
			autoUpdateTime = new JMeasureSpinner<Integer>();
			autoUpdateTime.setToolTipText("This is the max time without sending messages to vehicle with current controller value. Use high values for slow connections.");
			autoUpdateTime.setup(null, 500, 50, 99999, 100, 0, 0);
		}
		return autoUpdateTime;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
