package br.skylight.cucs.plugins.payload.eoir.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import br.skylight.cucs.widgets.ButtonControllerWidget;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.HoldControllerWidget;

public class CameraControl extends JFrame {

	private static final long serialVersionUID = 1L;

	private List<CaptureDeviceInfo> captureDev = new ArrayList<CaptureDeviceInfo>();
	
	private JPanel leftPane = null;

	private JSplitPane mainSplitPane = null;  //  @jve:decl-index=0:visual-constraint="368,-10"

	private JPanel imagesPanel = null;

	private JSplitPane leftSplitPane = null;

	private JTabbedPane tabbedPane = null;

	private JPanel controlsPanel = null;

	private JPanel viewPanel = null;

	private JPanel cameraPanel = null;

	private JPanel gimbalPanel = null;

	private HoldControllerWidget cameraPan = null;

	private HoldControllerWidget cameraTilt = null;

	private HoldControllerWidget cameraZoom = null;

	private ButtonControllerWidget takePicture = null;

	private ButtonControllerWidget missionCommand = null;

	private ButtonControllerWidget replay = null;

	private ButtonControllerWidget record = null;

	private JCheckBox centerOnTrack = null;

	private JCheckBox digitalStabilization = null;

	private JCheckBox showOsd = null;

	private JCheckBox objectDetection = null;

	private JButton objectDetectionSetup = null;

	private JRadioButton autoExposure = null;

	private JRadioButton fixedExposure = null;

	private JRadioButton autoFocus = null;

	private JRadioButton manualFocus = null;

	private JCheckBox cameraImageStabilizer = null;

	private JLabel irFilterState = null;

	private JComboBox irFilterValue = null;

	private JSpinner fixedFocusValue = null;

	private JSpinner fixedExposureValue = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JCheckBox compensateForAbsolute = null;

	private JLabel objectTrackingGain = null;

	private JSpinner objectTrackingGainValue = null;

	private JScrollPane thumbScroll = null;

	private CameraVideoImage cameraVideoImage = null;

	private JButton capture = null;
	
	private boolean capturing = false;

	private JComboBox captureDevices = null;

	private JLabel cameraCaptureDevice = null;

	private ButtonGroup evGroup = new ButtonGroup();  //  @jve:decl-index=0:
	private ButtonGroup focusGroup = new ButtonGroup();
	
	private static final int rightAreaSize = 150;
	private static final int bottomAreaSize = 120;
	
	/**
	 * @param owner
	 * @throws IOException 
	 */
	public CameraControl() throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		this.setSize(505, 380);
		this.setTitle("Skylight camera view");
		this.setContentPane(getMainSplitPane());
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/br/skylight/groundstation/images/webcam.gif")));
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent event) {
				getMainSplitPane().setDividerLocation(getMainSplitPane().getWidth()-rightAreaSize);
				getLeftSplitPane().setDividerLocation(getMainSplitPane().getHeight()-bottomAreaSize);
			}
		});
	}

	/**
	 * This method initializes leftPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getLeftPane() {
		if (leftPane == null) {
			leftPane = new JPanel();
			leftPane.setLayout(new BorderLayout());
			leftPane.setPreferredSize(new Dimension(100, 0));
			leftPane.add(getLeftSplitPane(), BorderLayout.CENTER);
		}
		return leftPane;
	}

	/**
	 * This method initializes mainSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getMainSplitPane() {
		if (mainSplitPane == null) {
			mainSplitPane = new JSplitPane();
//			mainSplitPane.setSize(new Dimension(479, 260));
			mainSplitPane.setDividerSize(8);
			mainSplitPane.setOneTouchExpandable(true);
			mainSplitPane.setDividerLocation(360);
			mainSplitPane.setRightComponent(getThumbScroll());
			mainSplitPane.setLeftComponent(getLeftPane());
		}
		return mainSplitPane;
	}

	/**
	 * This method initializes imagesPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getImagesPanel() {
		if (imagesPanel == null) {
			imagesPanel = new JPanel();
			imagesPanel.setLayout(new FlowLayout());
			imagesPanel.setPreferredSize(new Dimension(80, 0));
		}
		return imagesPanel;
	}

	/**
	 * This method initializes leftSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getLeftSplitPane() {
		if (leftSplitPane == null) {
			leftSplitPane = new JSplitPane();
			leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			leftSplitPane.setDividerLocation(220);
			leftSplitPane.setDividerSize(8);
			leftSplitPane.setOneTouchExpandable(true);
			leftSplitPane.setTopComponent(getCameraVideoImage());
			leftSplitPane.setBottomComponent(getTabbedPane());
		}
		return leftSplitPane;
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Controls", null, getControlsPanel(), null);
			tabbedPane.addTab("View", null, getViewPanel(), null);
			tabbedPane.addTab("Camera", null, getCameraPanel(), null);
			tabbedPane.addTab("Gimbal", null, getGimbalPanel(), null);
		}
		return tabbedPane;
	}

	/**
	 * This method initializes controlsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getControlsPanel() {
		if (controlsPanel == null) {
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 2;
			gridBagConstraints27.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints27.insets = new Insets(4, 5, 3, 3);
			gridBagConstraints27.gridy = 2;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.insets = new Insets(3, 5, 0, 3);
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.insets = new Insets(4, 5, 3, 7);
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.gridy = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.insets = new Insets(3, 5, 0, 7);
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(3, 5, 0, 7);
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.insets = new Insets(3, 3, 3, 7);
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.weighty = 0.0;
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.insets = new Insets(3, 3, 0, 7);
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(3, 3, 0, 7);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 0;
			controlsPanel = new JPanel();
			controlsPanel.setLayout(new GridBagLayout());
			controlsPanel.add(getCameraPan(), gridBagConstraints);
			controlsPanel.add(getCameraTilt(), gridBagConstraints1);
			controlsPanel.add(getCameraZoom(), gridBagConstraints2);
			controlsPanel.add(getTakePicture(), gridBagConstraints3);
			controlsPanel.add(getMissionCommand(), gridBagConstraints4);
			controlsPanel.add(getReplay(), gridBagConstraints5);
			controlsPanel.add(getRecord(), gridBagConstraints11);
			controlsPanel.add(getCapture(), gridBagConstraints27);
		}
		return controlsPanel;
	}

	/**
	 * This method initializes viewPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getViewPanel() {
		if (viewPanel == null) {
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 2;
			gridBagConstraints29.anchor = GridBagConstraints.WEST;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.gridy = 0;
			cameraCaptureDevice = new JLabel();
			cameraCaptureDevice.setText("Camera capture device:");
			cameraCaptureDevice.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints28.gridy = 1;
			gridBagConstraints28.weightx = 0.0;
			gridBagConstraints28.insets = new Insets(1, 0, 0, 3);
			gridBagConstraints28.gridx = 2;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.insets = new Insets(0, 28, 0, 0);
			gridBagConstraints10.gridy = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new Insets(0, 5, 0, 3);
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints8.insets = new Insets(0, 3, 3, 0);
			gridBagConstraints8.weighty = 0.0;
			gridBagConstraints8.gridy = 2;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints6.gridy = 0;
			viewPanel = new JPanel();
			viewPanel.setLayout(new GridBagLayout());
			viewPanel.add(getCenterOnTrack(), gridBagConstraints6);
			viewPanel.add(getDigitalStabilization(), gridBagConstraints7);
			viewPanel.add(getShowOsd(), gridBagConstraints8);
			viewPanel.add(getObjectDetection(), gridBagConstraints9);
			viewPanel.add(getObjectDetectionSetup(), gridBagConstraints10);
			viewPanel.add(getCaptureDevices(), gridBagConstraints28);
			viewPanel.add(cameraCaptureDevice, gridBagConstraints29);
		}
		return viewPanel;
	}

	/**
	 * This method initializes cameraPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCameraPanel() {
		if (cameraPanel == null) {
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 4;
			gridBagConstraints23.anchor = GridBagConstraints.WEST;
			gridBagConstraints23.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints23.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setFont(new Font("Dialog", Font.PLAIN, 10));
			jLabel1.setText("(0mm to 1000mm)");
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 4;
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints22.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("(-5EV to +5EV)");
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 3;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.gridy = 0;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 3;
			gridBagConstraints20.anchor = GridBagConstraints.WEST;
			gridBagConstraints20.gridy = 1;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.fill = GridBagConstraints.NONE;
			gridBagConstraints19.gridy = 2;
			gridBagConstraints19.weightx = 0.0;
			gridBagConstraints19.anchor = GridBagConstraints.WEST;
			gridBagConstraints19.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints19.gridx = 3;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints18.gridy = 1;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.gridx = 1;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 1;
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.gridy = 2;
			irFilterState = new JLabel();
			irFilterState.setText("IR filter usage:");
			irFilterState.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.gridy = 2;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.gridy = 1;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 0;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 0;
			cameraPanel = new JPanel();
			cameraPanel.setLayout(new GridBagLayout());
			cameraPanel.add(getAutoExposure(), gridBagConstraints12);
			cameraPanel.add(getFixedExposure(), gridBagConstraints13);
			cameraPanel.add(getAutoFocus(), gridBagConstraints14);
			cameraPanel.add(getManualFocus(), gridBagConstraints15);
			cameraPanel.add(getCameraImageStabilizer(), gridBagConstraints16);
			cameraPanel.add(irFilterState, gridBagConstraints17);
			cameraPanel.add(getIrFilterValue(), gridBagConstraints19);
			cameraPanel.add(getFixedFocusValue(), gridBagConstraints20);
			cameraPanel.add(getFixedExposureValue(), gridBagConstraints21);
			cameraPanel.add(jLabel, gridBagConstraints22);
			cameraPanel.add(jLabel1, gridBagConstraints23);
		}
		return cameraPanel;
	}

	/**
	 * This method initializes gimbalPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGimbalPanel() {
		if (gimbalPanel == null) {
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints26.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints26.weightx = 1.0;
			gridBagConstraints26.gridy = 1;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints25.insets = new Insets(5, 7, 3, 0);
			gridBagConstraints25.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints25.weightx = 0.0;
			gridBagConstraints25.weighty = 1.0;
			gridBagConstraints25.gridy = 1;
			objectTrackingGain = new JLabel();
			objectTrackingGain.setText("Object tracking gain:");
			objectTrackingGain.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.anchor = GridBagConstraints.WEST;
			gridBagConstraints24.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.weightx = 0.0;
			gridBagConstraints24.gridwidth = 2;
			gridBagConstraints24.gridy = 0;
			gimbalPanel = new JPanel();
			gimbalPanel.setLayout(new GridBagLayout());
			gimbalPanel.add(getCompensateForAbsolute(), gridBagConstraints24);
			gimbalPanel.add(objectTrackingGain, gridBagConstraints25);
			gimbalPanel.add(getObjectTrackingGainValue(), gridBagConstraints26);
		}
		return gimbalPanel;
	}

	/**
	 * This method initializes cameraPan	
	 * 	
	 * @return br.skylight.groundstation.widgets.HoldControllerWidget	
	 */
	private HoldControllerWidget getCameraPan() {
		if (cameraPan == null) {
			cameraPan = new HoldControllerWidget();
			cameraPan.getUnholdButton().setVisible(false);
			cameraPan.setMinValue(new Integer(-360));
			cameraPan.setLabel("Camera Pan:");
			cameraPan.setMaxValue(new Integer(360));
		}
		return cameraPan;
	}

	/**
	 * This method initializes cameraTilt	
	 * 	
	 * @return br.skylight.groundstation.widgets.HoldControllerWidget	
	 */
	private HoldControllerWidget getCameraTilt() {
		if (cameraTilt == null) {
			cameraTilt = new HoldControllerWidget();
			cameraTilt.getUnholdButton().setVisible(false);
			cameraTilt.setLabel("Camera Tilt:");
		}
		return cameraTilt;
	}

	/**
	 * This method initializes cameraZoom	
	 * 	
	 * @return br.skylight.groundstation.widgets.HoldControllerWidget	
	 */
	private HoldControllerWidget getCameraZoom() {
		if (cameraZoom == null) {
			cameraZoom = new HoldControllerWidget();
			cameraZoom.getUnholdButton().setVisible(false);
			cameraZoom.setLabel("Camera Zoom:");
		}
		return cameraZoom;
	}

	/**
	 * This method initializes takePicture	
	 * 	
	 * @return br.skylight.groundstation.widgets.ButtonWidget	
	 */
	private ButtonControllerWidget getTakePicture() {
		if (takePicture == null) {
			takePicture = new ButtonControllerWidget();
			takePicture.getButton().setText("Take picture");
			takePicture.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//TODO implement
				}
			});
		}
		return takePicture;
	}

	/**
	 * This method initializes missionCommand	
	 * 	
	 * @return br.skylight.groundstation.widgets.ButtonWidget	
	 */
	private ButtonControllerWidget getMissionCommand() {
		if (missionCommand == null) {
			missionCommand = new ButtonControllerWidget();
			missionCommand.getButton().setText("Loiter around current uav position");
			missionCommand.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//TODO implement
				}
			});
		}
		return missionCommand;
	}

	/**
	 * This method initializes replay	
	 * 	
	 * @return br.skylight.groundstation.widgets.ButtonWidget	
	 */
	private ButtonControllerWidget getReplay() {
		if (replay == null) {
			replay = new ButtonControllerWidget();
			replay.getButton().setText("Replay");
			replay.setToolTipText("Replay last 10s from now");
			replay.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//TODO implement
				}
			});
		}
		return replay;
	}

	/**
	 * This method initializes record	
	 * 	
	 * @return br.skylight.groundstation.widgets.ButtonWidget	
	 */
	private ButtonControllerWidget getRecord() {
		if (record == null) {
			record = new ButtonControllerWidget();
			record.getButton().setText("Record");
			record.setToolTipText("Record streaming video to a file");
			record.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//TODO implement
				}
			});
		}
		return record;
	}

	/**
	 * This method initializes centerOnTrack	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCenterOnTrack() {
		if (centerOnTrack == null) {
			centerOnTrack = new JCheckBox();
			centerOnTrack.setText("Center view when tracking objects");
			centerOnTrack.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return centerOnTrack;
	}

	/**
	 * This method initializes digitalStabilization	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDigitalStabilization() {
		if (digitalStabilization == null) {
			digitalStabilization = new JCheckBox();
			digitalStabilization.setText("Digital stabilization");
			digitalStabilization.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return digitalStabilization;
	}

	/**
	 * This method initializes showOsd	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getShowOsd() {
		if (showOsd == null) {
			showOsd = new JCheckBox();
			showOsd.setText("OSD (On Screen Display)");
			showOsd.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return showOsd;
	}

	/**
	 * This method initializes objectDetection	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getObjectDetection() {
		if (objectDetection == null) {
			objectDetection = new JCheckBox();
			objectDetection.setText("Object detection");
			objectDetection.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return objectDetection;
	}

	/**
	 * This method initializes objectDetectionSetup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getObjectDetectionSetup() {
		if (objectDetectionSetup == null) {
			objectDetectionSetup = new JButton();
			objectDetectionSetup.setText("Detection setup...");
			objectDetectionSetup.setFont(new Font("Dialog", Font.PLAIN, 10));
			objectDetectionSetup.setMargin(CUCSViewHelper.getMinimalButtonMargin());
		}
		return objectDetectionSetup;
	}

	/**
	 * This method initializes autoExposure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAutoExposure() {
		if (autoExposure == null) {
			autoExposure = new JRadioButton();
			autoExposure.setText("Auto exposure");
			autoExposure.setFont(new Font("Dialog", Font.PLAIN, 12));
			evGroup.add(autoExposure);
		}
		return autoExposure;
	}

	/**
	 * This method initializes fixedExposure	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getFixedExposure() {
		if (fixedExposure == null) {
			fixedExposure = new JRadioButton();
			fixedExposure.setText("Fixed exposure:");
			fixedExposure.setFont(new Font("Dialog", Font.PLAIN, 12));
			evGroup.add(fixedExposure);
		}
		return fixedExposure;
	}

	/**
	 * This method initializes autoFocus	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getAutoFocus() {
		if (autoFocus == null) {
			autoFocus = new JRadioButton();
			autoFocus.setText("Auto focus");
			autoFocus.setFont(new Font("Dialog", Font.PLAIN, 12));
			focusGroup.add(autoFocus);
		}
		return autoFocus;
	}

	/**
	 * This method initializes manualFocus	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getManualFocus() {
		if (manualFocus == null) {
			manualFocus = new JRadioButton();
			manualFocus.setText("Fixed focus:");
			manualFocus.setFont(new Font("Dialog", Font.PLAIN, 12));
			focusGroup.add(manualFocus);
		}
		return manualFocus;
	}

	/**
	 * This method initializes cameraImageStabilizer	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCameraImageStabilizer() {
		if (cameraImageStabilizer == null) {
			cameraImageStabilizer = new JCheckBox();
			cameraImageStabilizer.setText("Activate camera stabilizer");
			cameraImageStabilizer.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return cameraImageStabilizer;
	}

	/**
	 * This method initializes irFilterValue	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getIrFilterValue() {
		if (irFilterValue == null) {
			irFilterValue = new JComboBox();
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			model.addElement("Auto");
			model.addElement("On");
			model.addElement("Off");
			irFilterValue.setModel(model);
		}
		return irFilterValue;
	}

	/**
	 * This method initializes fixedFocusValue	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getFixedFocusValue() {
		if (fixedFocusValue == null) {
			fixedFocusValue = new JSpinner(new SpinnerNumberModel());
			fixedFocusValue.setPreferredSize(new Dimension(50, 20));
		}
		return fixedFocusValue;
	}

	/**
	 * This method initializes fixedExposureValue	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getFixedExposureValue() {
		if (fixedExposureValue == null) {
			fixedExposureValue = new JSpinner(new SpinnerNumberModel());
			fixedExposureValue.setPreferredSize(new Dimension(50, 20));
		}
		return fixedExposureValue;
	}

	/**
	 * This method initializes compensateForAbsolute	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCompensateForAbsolute() {
		if (compensateForAbsolute == null) {
			compensateForAbsolute = new JCheckBox();
			compensateForAbsolute.setText("Compensate attitude for absolute pan/tilt");
			compensateForAbsolute.setToolTipText("Compensates uav attitude in order to put gimbal in a neutral reference");
			compensateForAbsolute.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return compensateForAbsolute;
	}

	/**
	 * This method initializes objectTrackingGainValue	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getObjectTrackingGainValue() {
		if (objectTrackingGainValue == null) {
			objectTrackingGainValue = new JSpinner();
			objectTrackingGainValue.setPreferredSize(new Dimension(40, 20));
		}
		return objectTrackingGainValue;
	}

	/**
	 * This method initializes thumbScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getThumbScroll() {
		if (thumbScroll == null) {
			thumbScroll = new JScrollPane();
			thumbScroll.setViewportView(getImagesPanel());
		}
		return thumbScroll;
	}

	/**
	 * This method initializes cameraVideoImage	
	 * 	
	 * @return br.skylight.groundstation.widgets.camera.CameraVideoImage	
	 */
	private CameraVideoImage getCameraVideoImage() {
		if (cameraVideoImage == null) {
			cameraVideoImage = new CameraVideoImage();
			cameraVideoImage.setPreferredSize(new Dimension(10, 200));
		}
		return cameraVideoImage;
	}

	/**
	 * This method initializes capture	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCapture() {
		if (capture == null) {
			capture = new JButton();
			capture.setPreferredSize(new Dimension(34, 20));
			capture.setFont(new Font("Dialog", Font.PLAIN, 12));
			capture.setText("Start capture");
			capture.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//stop capturing
					if(capturing) {
						
						capture.setText("Start capture");
						capturing = false;
					//start capturing
					} else {
						
						capture.setText("Stop capture");
						capturing = true;
					}
				}
			});
		}
		return capture;
	}

	private void refreshCaptureDevices() {
		captureDev.clear();
		DefaultComboBoxModel model = (DefaultComboBoxModel)captureDevices.getModel();
		model.removeAllElements();
		model.addElement("-refresh-");
		Vector info = CaptureDeviceManager.getDeviceList(null);
		for(int i=0; i<info.size(); i++) {
			boolean addDevice = false;
			CaptureDeviceInfo di = (CaptureDeviceInfo)info.get(i);
			Format[] formats = di.getFormats();
			for(int c=0; c<formats.length; c++) {
				if(formats[c] instanceof VideoFormat) {
					addDevice = true;
				}
			}
			if(addDevice) {
				model.addElement(di.getName());
				captureDev.add(di);
			}
		}
	}

	/**
	 * This method initializes captureDevices	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCaptureDevices() {
		if (captureDevices == null) {
			captureDevices = new JComboBox();
			captureDevices.setModel(new DefaultComboBoxModel());
			captureDevices.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(getCaptureDevices().getSelectedIndex()==0) {
						refreshCaptureDevices();
					}
				}
			});
			captureDevices.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(getCaptureDevices().getSelectedIndex()>0) {
						getCameraVideoImage().setCaptureDevice(captureDev.get(getCaptureDevices().getSelectedIndex()-1));
					}
				}
			});
			refreshCaptureDevices();
		}
		return captureDevices;
	}
	
	public static void main(String[] args) throws IOException {
		CameraControl c = new CameraControl();
		c.setVisible(true);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
