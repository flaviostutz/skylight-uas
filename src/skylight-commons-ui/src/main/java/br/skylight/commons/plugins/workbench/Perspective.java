package br.skylight.commons.plugins.workbench;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import bibliothek.gui.DockFrontend;
import bibliothek.gui.DockTheme;
import bibliothek.gui.dock.FlapDockStation;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.frontend.MissingDockableStrategy;

public class Perspective {

	private static final Logger logger = Logger.getLogger(Perspective.class.getName());
	
	private SplitDockStation centerStation;
	private FlapDockStation leftStation;
	private FlapDockStation rightStation;
	private FlapDockStation bottomStation;
	
	private DockFrontend dockFrontend;
	private int order;
	private transient JToggleButton button;
	private JPanel contentArea;

	public Perspective(JFrame owner, List<ViewExtensionPoint> viewExtensionPoint) {
		dockFrontend = new DockFrontend(owner);
		dockFrontend.addRoot("center", getCenterStation());
		dockFrontend.addRoot("left", getLeftStation());
		dockFrontend.addRoot("right", getRightStation());
		dockFrontend.addRoot("bottom", getBottomStation());
		dockFrontend.setDefaultHideable(true);
		dockFrontend.setShowHideAction(true);
		dockFrontend.getController().setTheme(new NoStackBasicTheme());
		for (ViewExtensionPoint vp : viewExtensionPoint) {
			dockFrontend.registerFactory(new ViewDockableFactory(vp), true);
		}
	}
	
	public JPanel getContentArea() {
		if(contentArea==null) {
			contentArea = new JPanel();
			contentArea.setLayout(new BorderLayout());
			contentArea.add(getCenterStation().getComponent(), BorderLayout.CENTER);
			contentArea.add(getLeftStation().getComponent(), BorderLayout.WEST);
			contentArea.add(getRightStation().getComponent(), BorderLayout.EAST);
			contentArea.add(getBottomStation().getComponent(), BorderLayout.SOUTH);
		}
		return contentArea;
	}
	
	public SplitDockStation getCenterStation() {
		if(centerStation==null) {
			centerStation = new SplitDockStation();
		}
		return centerStation;
	}
	public FlapDockStation getLeftStation() {
		if(leftStation==null) {
			leftStation = new FlapDockStation();
		}
		return leftStation;
	}
	public FlapDockStation getRightStation() {
		if(rightStation==null) {
			rightStation = new FlapDockStation();
		}
		return rightStation;
	}
	public FlapDockStation getBottomStation() {
		if(bottomStation==null) {
			bottomStation = new FlapDockStation();
		}
		return bottomStation;
	}
	
	public DockFrontend getDockFrontend() {
		return dockFrontend;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public void setName(String name) {
		getButton().setText(name);
	}
	
	public void read(DataInputStream in) throws IOException {
		order = in.readShort();
		getButton().setText(in.readUTF());//perspective name
		String themeClass = in.readUTF();//theme class name
		try {
			setDockTheme((DockTheme)Class.forName(themeClass).newInstance());
		} catch (Exception e) {
			logger.warning("Cannot apply theme "+ themeClass +". Using default. " + e.toString());
		}
		dockFrontend.setMissingDockableStrategy(MissingDockableStrategy.DISCARD_ALL);
		dockFrontend.read(in);
	}
	
	public void setDockTheme(DockTheme theme) {
		getCenterStation().getController().setTheme(theme);
		getLeftStation().getController().setTheme(theme);
		getRightStation().getController().setTheme(theme);
		getBottomStation().getController().setTheme(theme);
	}
	
	public DockTheme getDockTheme() {
		return getCenterStation().getTheme();
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeShort(order);
		out.writeUTF(getName());//perspective name
		out.writeUTF(getDockTheme().getClass().getName());//theme class name
		dockFrontend.setMissingDockableStrategy(MissingDockableStrategy.DISCARD_ALL);
		dockFrontend.write(out);
	}

	public void addView(View view) {
		//keep the sequence below (!)
		getDockFrontend().addDockable(System.currentTimeMillis()+"",view);
		getCenterStation().drop(view);
	}
	
	public int getOrder() {
		return order;
	}
	public String getName() {
		return getButton().getText();
	}
	
	public JToggleButton getButton() {
		if(button==null) {
			button = new JToggleButton();
			button.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
			button.setMargin(new Insets(1,1,1,1));
		}
		return button;
	}

}
