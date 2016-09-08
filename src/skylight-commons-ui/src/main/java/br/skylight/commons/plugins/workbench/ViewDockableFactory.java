package br.skylight.commons.plugins.workbench;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DockFactory;
import bibliothek.gui.dock.dockable.DefaultDockablePerspective;
import bibliothek.gui.dock.layout.LocationEstimationMap;
import bibliothek.gui.dock.perspective.PerspectiveDockable;
import bibliothek.gui.dock.station.support.PlaceholderStrategy;
import bibliothek.util.xml.XElement;

public class ViewDockableFactory implements DockFactory<View, DefaultDockablePerspective, Serializable> {

	private ViewExtensionPoint viewExtensionPoint;

	public ViewDockableFactory(ViewExtensionPoint viewExtensionPoint) {
		this.viewExtensionPoint = viewExtensionPoint;
	}

	@Override
	public String getID() {
		return viewExtensionPoint.getClass().getName();
	}

	@Override
	public void estimateLocations(Serializable layout, LocationEstimationMap arg1) {
	}

	@Override
	public void write(Serializable layout, DataOutputStream out) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(layout);
	}

	@Override
	public void write(Serializable layout, XElement element) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public Serializable read(DataInputStream in, PlaceholderStrategy arg1) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(in);
		try {
			return (Serializable)ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Serializable read(XElement element, PlaceholderStrategy arg1) {
		throw new RuntimeException("NOT IMPLEMENTED");
//		return element.getElement("state").getString().getBytes();
	}

	@Override
	public void setLayout(View element, Serializable layout, Map<Integer, Dockable> children, PlaceholderStrategy placeholders) {
		element.setState(layout);
	}

	@Override
	public void setLayout(View element, Serializable layout, PlaceholderStrategy placeholders) {
		element.setState(layout);
	}

	@Override
	public View layout(Serializable layout, PlaceholderStrategy placeholders) {
		try {
			View v = viewExtensionPoint.createView();
			v.initRestoredView(layout);
			return v;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public DefaultDockablePerspective layoutPerspective(Serializable layout, Map<Integer, PerspectiveDockable> children) {
		return null;
	}

	@Override
	public void layoutPerspective(DefaultDockablePerspective perspective, Serializable layout, Map<Integer, PerspectiveDockable> children) {
	}

	@Override
	public View layout(Serializable layout, Map<Integer, Dockable> children, PlaceholderStrategy placeholders) {
		return this.layout(layout, placeholders);
	}

	@Override
	public Serializable getLayout(View element, Map<Dockable, Integer> children) {
		element.prepareState();
		return element.getState();
	}

	@Override
	public Serializable getPerspectiveLayout(DefaultDockablePerspective element, Map<PerspectiveDockable, Integer> children) {
		return null;
	}

}
