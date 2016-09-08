package br.skylight.commons.plugins.workbench;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.swing.JPanel;

import bibliothek.gui.dock.DefaultDockable;

public abstract class View<P extends Serializable> extends DefaultDockable {

	private static final Logger logger = Logger.getLogger(View.class.getName());
	private P state = null;
	
	public View(ViewExtensionPoint<P> viewExtensionPoint) {
		setFactoryID(viewExtensionPoint.getClass().getName());
	}
	
	protected void initNewView() {
		add(getContents());
		try {
			onActivate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		state = instantiateState();
		onStateUpdated();
	}
	
	protected void initRestoredView(P state) {
		add(getContents());
		try {
			onActivate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		setState(state);
//		state = instantiateState();
//		setStateData(layout);
	}

	protected void onActivate() throws Exception {};
	protected void onDeactivate() throws Exception {};

//	public void setStateData(byte[] stateData) {
//		if(stateData!=null && state!=null) {
//			try {
//				state.readState(new DataInputStream(new ByteArrayInputStream(stateData)));
//				onStateUpdated();
//			} catch (IOException e) {
//				logger.warning("Could not load defaultPlugin state. e=" + e.toString());
//			}
//		}
//	}
//	public byte[] getStateData() {
//		prepareState();
//		try {
//			onDeactivate();
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//		if(state!=null) {
//			try {
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				state.writeState(new DataOutputStream(bos));
//				return bos.toByteArray();
//			} catch (IOException e) {
//				logger.warning("Could not save view state. e=" + e.toString());
//				return new byte[0];
//			}
//		} else {
//			return null;
//		}
//	}

	public void setState(P state) {
		this.state = state;
		onStateUpdated();
	}
	
	public P getState() {
		return state;
	}
	
	protected abstract JPanel getContents();
	protected abstract P instantiateState();
	protected abstract void onStateUpdated();
	protected abstract void prepareState();

}
