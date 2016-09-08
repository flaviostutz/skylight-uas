package br.skylight.cucs.plugins.gamecontroller;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

public class GameControllerView extends View implements GameControllerServiceListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="37,13"
	private JButton add = null;
	private JButton remove = null;
	private JComboBox controllerBindingProfiles = null;
	private ControllerBindingProfile selectedProfile;
	
	@ServiceInjection
	public GameControllerService gameControllerService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	public GameControllerView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
		setTitleText("Game controller bindings");
	}

	@Override
	protected void onActivate() throws Exception {
		gameControllerService.addGameControllerServiceListener(this);
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.insets = new Insets(1, 3, 5, 3);
			gridBagConstraints2.weighty = 0.0;
			gridBagConstraints2.anchor = GridBagConstraints.NORTH;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.insets = new Insets(5, 3, 0, 0);
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints.gridy = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(182, 63));
			contents.add(getAdd(), gridBagConstraints);
			contents.add(getRemove(), gridBagConstraints1);
			contents.add(getControllerBindingProfiles(), gridBagConstraints2);
		}
		return contents;
	}

	/**
	 * This method initializes add	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAdd() {
		if (add == null) {
			add = new JButton();
			add.setEnabled(true);
			add.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/add.gif")));
			add.setMargin(ViewHelper.getDefaultButtonMargin());
			add.setToolTipText("Add a controller binding profile");
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = JOptionPane.showInputDialog("Enter profile name:");
					if(name!=null) {
						ControllerBindingProfile p = gameControllerService.createControllerBindingProfile(name);
						updateComboBoxModel();
						selectProfile(p);
					}
				}
			});
		}
		return add;
	}

	protected void selectProfile(ControllerBindingProfile p) {
		gameControllerService.selectControllerBindingProfile(p);
		getControllerBindingProfiles().setSelectedItem(p);
		selectedProfile = p;
	}

	/**
	 * This method initializes remove	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemove() {
		if (remove == null) {
			remove = new JButton();
			remove.setEnabled(true);
			remove.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/remove.gif")));
			remove.setMargin(ViewHelper.getDefaultButtonMargin());
			remove.setToolTipText("Remove selected controller binding profile");
			remove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(selectedProfile!=null) {
						gameControllerService.getControllerProfiles().remove(selectedProfile);
						updateComboBoxModel();
						if(gameControllerService.getControllerProfiles().size()>0) {
							selectProfile(gameControllerService.getControllerProfiles().get(0));
						}
					}
				}
			});
		}
		return remove;
	}

	protected void updateComboBoxModel() {
		Vector<ControllerBindingProfile> list = new Vector<ControllerBindingProfile>();
		list.addAll(gameControllerService.getControllerProfiles());
		getControllerBindingProfiles().setModel(new DefaultComboBoxModel(list));
		getControllerBindingProfiles().updateUI();
	}

	/**
	 * This method initializes controllerBindingProfiles	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getControllerBindingProfiles() {
		if (controllerBindingProfiles == null) {
			controllerBindingProfiles = new JComboBox();
			controllerBindingProfiles.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					gameControllerService.selectControllerBindingProfile((ControllerBindingProfile)getControllerBindingProfiles().getSelectedItem());
				}
			});
		}
		return controllerBindingProfiles;
	}

	@Override
	protected Serializable instantiateState() {
		return null;
	}

	@Override
	protected void onStateUpdated() {
	}

	@Override
	protected void prepareState() {
	}

	@Override
	public void onGameControllerServiceStartup() {
		updateComboBoxModel();
		if(gameControllerService.getControllerProfiles()!=null) {
			selectProfile(gameControllerService.getCurrentControllerProfile());
		} else {
			if(gameControllerService.getControllerProfiles().size()>0) {
				selectProfile(gameControllerService.getControllerProfiles().get(0));
			}
		}
	}
}
