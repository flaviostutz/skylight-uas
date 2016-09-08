package br.skylight.commons.plugins.workbench;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;

import br.skylight.commons.infra.SerializableState;

public class WorkbenchPreferences implements SerializableState {

	private int windowPosX = 0;
	private int windowPosY = 0;
	private int windowWidth = 800;
	private int windowHeight = 600;
	private int windowState = JFrame.MAXIMIZED_BOTH;
	private String selectedLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
	private int selectedPerpective = 0;
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		windowPosX = in.readInt();
		windowPosY = in.readInt();
		windowWidth = in.readInt();
		windowHeight = in.readInt();
		windowState = in.readInt();
		selectedLookAndFeelClassName = in.readUTF();
		selectedPerpective = in.readInt();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(windowPosX);
		out.writeInt(windowPosY);
		out.writeInt(windowWidth);
		out.writeInt(windowHeight);
		out.writeInt(windowState);
		out.writeUTF(selectedLookAndFeelClassName);
		out.writeInt(selectedPerpective);
	}

	public int getWindowPosX() {
		return windowPosX;
	}

	public void setWindowPosX(int windowPosX) {
		this.windowPosX = windowPosX;
	}

	public int getWindowPosY() {
		return windowPosY;
	}

	public void setWindowPosY(int windowPosY) {
		this.windowPosY = windowPosY;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public int getWindowState() {
		return windowState;
	}

	public void setWindowState(int windowState) {
		this.windowState = windowState;
	}

	public int getSelectedPerpective() {
		return selectedPerpective;
	}

	public void setSelectedPerpective(int selectedPerpective) {
		this.selectedPerpective = selectedPerpective;
	}
	
	public void setSelectedLookAndFeelClassName(String selectedLookAndFeelClassName) {
		this.selectedLookAndFeelClassName = selectedLookAndFeelClassName;
	}
	public String getSelectedLookAndFeelClassName() {
		return selectedLookAndFeelClassName;
	}
	
}
