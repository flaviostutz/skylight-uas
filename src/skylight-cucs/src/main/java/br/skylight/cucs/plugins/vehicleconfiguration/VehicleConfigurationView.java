package br.skylight.cucs.plugins.vehicleconfiguration;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleConfigurationView extends VehicleView {

	private static final long serialVersionUID = 1L;
	private JPanel contents = null;  //  @jve:decl-index=0:visual-constraint="41,60"
	private JTabbedPane jTabbedPane = null;
	private List<JPanel> visibleExtTabs = new CopyOnWriteArrayList<JPanel>();  //  @jve:decl-index=0:

	@ServiceInjection
	public MessagingService messagingService;
	
	@ExtensionPointsInjection
	public List<VehicleConfigurationSectionExtensionPoint> sectionImpls;
	
	/**
	 * @param owner
	 */
	public VehicleConfigurationView(ViewExtensionPoint extensionPoint) {
		super(extensionPoint);
	}
	
	@Override
	protected void updateGUI() {
		for (VehicleConfigurationSectionExtensionPoint ep : sectionImpls) {
			if(ep.updateVehicle(getCurrentVehicle())) {
				if(!visibleExtTabs.contains(ep.getSectionComponent())) {
					getJTabbedPane().addTab(ep.getSectionName(), ep.getSectionComponent());
					visibleExtTabs.add(ep.getSectionComponent());
				}
			} else {
				jTabbedPane.remove(ep.getSectionComponent());
			}
		}
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle Configuration";
	}

	/**
	 * This method initializes contents	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(317, 263));
			contents.add(getJTabbedPane(), gridBagConstraints);
		}
		return contents;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
		}
		return jTabbedPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
