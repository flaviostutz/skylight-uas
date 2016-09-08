package br.skylight.cucs.plugins.preferences;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.measure.unit.Unit;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.infra.Activable;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class PreferencesDialog extends JDialog implements Activable {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JTabbedPane main = null;
	private JButton okButton = null;
	private JPanel measureTypes = null;
	private List<MeasureTypeSelector> measureTypesSelectors = new ArrayList<MeasureTypeSelector>();

	@ExtensionPointsInjection
	public List<PreferencesSectionExtensionPoint> preferencesSectionExtensionPoints;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	
	/**
	 * @param owner
	 * @param preferencesSectionExtensionPoints2 
	 */
	public PreferencesDialog(Frame owner) {
		super(owner);
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(337, 362);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Preferences");
		this.setContentPane(getJContentPane());
		ViewHelper.centerWindow(this);
		CUCSViewHelper.setDefaultIcon(this);
	}

	private void loadExtensionPointImpls() {
		Collections.sort(preferencesSectionExtensionPoints, new Comparator<PreferencesSectionExtensionPoint>() {
			public int compare(PreferencesSectionExtensionPoint o1, PreferencesSectionExtensionPoint o2) {
				if(o1.getOrder()<o2.getOrder()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		for (PreferencesSectionExtensionPoint ep : preferencesSectionExtensionPoints) {
			ep.load();
			getMain().addTab(ep.getName(), ep.getContents());
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.gridx = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.insets = new Insets(3, 0, 3, 0);
			gridBagConstraints4.gridy = 1;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getOkButton(), gridBagConstraints4);
			jContentPane.add(getMain(), gridBagConstraints5);
		}
		return jContentPane;
	}

	/**
	 * This method initializes main	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getMain() {
		if (main == null) {
			main = new JTabbedPane();
			main.addTab("Measure units", getMeasureTypes());
			main.setSelectedIndex(0);
		}
		return main;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("Save preferences");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//extension points save
					for (PreferencesSectionExtensionPoint ep : preferencesSectionExtensionPoints) {
						ep.save();
					}
					
					for (MeasureTypeSelector ms : measureTypesSelectors) {
						ms.getMeasureType().setTargetUnit((Unit)ms.getSelectedUnit().getSelectedItem());
					}
					
					//notify listeners
					subscriberService.notifyPreferencesUpdated();
					
					setVisible(false);
				}
			});
		}
		return okButton;
	}

	public Component getThis() {
		return this;
	}
	
	/**
	 * This method initializes measureTypes	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMeasureTypes() {
		if (measureTypes == null) {
			measureTypes = new JPanel();
			measureTypes.setLayout(new FlowLayout());
			for (MeasureType mt : MeasureType.values()) {
				if(!mt.equals(MeasureType.UNDEFINED)) {
					MeasureTypeSelector mts = new MeasureTypeSelector();
					mts.setMeasureType(mt);
					measureTypes.add(mts);
					measureTypesSelectors.add(mts);
				}
			}
		}
		return measureTypes;
	}

	@Override
	public void activate() throws Exception {
		loadExtensionPointImpls();
	}

	@Override
	public void deactivate() throws Exception {
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public void init() throws Exception {
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
