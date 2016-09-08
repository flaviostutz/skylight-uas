package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.streamchannel.FileTransferOperatorListener;
import br.skylight.commons.plugins.streamchannel.FileTransferOperator.Mode;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleFileTransferView extends VehicleView {

	private JPanel contents; // @jve:decl-index=0:visual-constraint="17,-1"

	@ServiceInjection
	public PluginManager pluginManager;
	
	@MemberInjection
	public FileTransferClientOperator fileTransferClientOperator;
	
	@ServiceInjection
	public UserService userService;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JTextField downloadFileInput = null;

	private JButton downloadButton = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel21 = null;

	private JTextField uploadFileInput = null;

	private JButton uploadButton = null;

	private JButton uploadButton1 = null;

	private JProgressBar progressBar = null;

	private File uploadFile;

	private JLabel status = null;
	
	public VehicleFileTransferView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle TCP Tunnel";
	}

	@Override
	protected void updateGUI() {
		if (getCurrentVehicle() != null) {
			if(fileTransferClientOperator.getMode().equals(Mode.SENDING_FILE)) {
				getUploadButton().setEnabled(true);
				getUploadButton().setText("Cancel upload");
				getDownloadButton().setEnabled(false);
				getDownloadButton().setText("Download");
			} else if(fileTransferClientOperator.getMode().equals(Mode.RECEIVING_FILE)) {
				getUploadButton().setEnabled(false);
				getUploadButton().setText("Upload");
				getDownloadButton().setEnabled(true);
				getDownloadButton().setText("Cancel download");
			} else if(fileTransferClientOperator.getMode().equals(Mode.IDLE)) {
				getUploadButton().setEnabled(true);
				getUploadButton().setText("Upload");
				getDownloadButton().setEnabled(true);
				getDownloadButton().setText("Download");
			}
		}
	}

	@Override
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints8.insets = new Insets(0, 5, 3, 0);
			gridBagConstraints8.gridwidth = 4;
			gridBagConstraints8.gridy = 4;
			status = new JLabel();
			status.setText("Idle");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridwidth = 4;
			gridBagConstraints7.weighty = 0.0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints7.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints7.gridy = 5;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.gridy = 3;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 3;
			gridBagConstraints5.insets = new Insets(0, 3, 0, 8);
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.weightx = 0.0;
			gridBagConstraints5.gridy = 3;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints41.gridy = 3;
			gridBagConstraints41.weightx = 1.0;
			gridBagConstraints41.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints41.gridx = 1;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.insets = new Insets(0, 15, 0, 0);
			gridBagConstraints31.gridy = 3;
			jLabel21 = new JLabel();
			jLabel21.setText("Filename:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.insets = new Insets(0, 15, 0, 0);
			gridBagConstraints4.gridy = 1;
			jLabel2 = new JLabel();
			jLabel2.setText("Filename:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 3;
			gridBagConstraints3.insets = new Insets(0, 3, 0, 8);
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints1.insets = new Insets(7, 8, 4, 0);
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridy = 2;
			jLabel1 = new JLabel();
			jLabel1.setText("Upload file to vehicle");
			jLabel1.setFont(new Font("Tahoma", Font.BOLD, 11));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints.insets = new Insets(0, 8, 2, 0);
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Download file from vehicle");
			jLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(283, 153));
			contents.add(jLabel, gridBagConstraints);
			contents.add(jLabel1, gridBagConstraints1);
			contents.add(getDownloadFileInput(), gridBagConstraints2);
			contents.add(getDownloadButton(), gridBagConstraints3);
			contents.add(jLabel2, gridBagConstraints4);
			contents.add(jLabel21, gridBagConstraints31);
			contents.add(getUploadFileInput(), gridBagConstraints41);
			contents.add(getUploadButton(), gridBagConstraints5);
			contents.add(getUploadButton1(), gridBagConstraints6);
			contents.add(getProgressBar(), gridBagConstraints7);
			contents.add(status, gridBagConstraints8);
		}
		return contents;
	}

	/**
	 * This method initializes downloadFileInput	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDownloadFileInput() {
		if (downloadFileInput == null) {
			downloadFileInput = new JTextField();
		}
		return downloadFileInput;
	}

	/**
	 * This method initializes downloadButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDownloadButton() {
		if (downloadButton == null) {
			downloadButton = new JButton();
			downloadButton.setText("Download");
			downloadButton.setMargin(ViewHelper.getDefaultButtonMargin());
			downloadButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if(fileTransferClientOperator.getMode().equals(Mode.IDLE)) {
							fileTransferClientOperator.requestFile(getDownloadFileInput().getText(), userService.getCurrentCucsId(), getCurrentVehicle().getVehicleID().getVehicleID());
							status.setText("Receiving file...");
							updateGUI();
						} else {
							fileTransferClientOperator.cancelCurrentTransfer();
							updateGUI();
						}
						getProgressBar().setValue(0);
					} catch (IOException e1) {
						ViewHelper.showException(e1);
					}
				}
			});
		}
		return downloadButton;
	}

	/**
	 * This method initializes uploadFileInput	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getUploadFileInput() {
		if (uploadFileInput == null) {
			uploadFileInput = new JTextField();
			uploadFileInput.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					uploadFile = new File(uploadFileInput.getText().trim());
					getUploadButton().setEnabled(uploadFile.exists());
				}
			});
		}
		return uploadFileInput;
	}

	/**
	 * This method initializes uploadButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUploadButton() {
		if (uploadButton == null) {
			uploadButton = new JButton();
			uploadButton.setMargin(ViewHelper.getDefaultButtonMargin());
			uploadButton.setEnabled(false);
			uploadButton.setText("Upload");
			uploadButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if(fileTransferClientOperator.getMode().equals(Mode.IDLE)) {
							fileTransferClientOperator.sendFile(uploadFile, userService.getCurrentCucsId(), getCurrentVehicle().getVehicleID().getVehicleID());
							status.setText("Sending file...");
							updateGUI();
						} else {
							fileTransferClientOperator.cancelCurrentTransfer();
							updateGUI();
						}
						getProgressBar().setValue(0);
					} catch (IOException e1) {
						ViewHelper.showException(e1);
					}
				}
			});
		}
		return uploadButton;
	}

	/**
	 * This method initializes uploadButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUploadButton1() {
		if (uploadButton1 == null) {
			uploadButton1 = new JButton();
			uploadButton1.setMargin(ViewHelper.getDefaultButtonMargin());
			uploadButton1.setToolTipText("Browse file");
			uploadButton1.setText("...");
			uploadButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				}
			});
		}
		return uploadButton1;
	}

	/**
	 * This method initializes progressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setMinimum(0);
			progressBar.setMaximum(100);
			fileTransferClientOperator.setFileTransferOperatorListener(new FileTransferOperatorListener() {
				@Override
				public void onTransferCheckedAndComplete() {
					progressBar.setValue(100);
					status.setText("File transfer complete");
					updateGUI();
				}
				@Override
				public void onDataTransfer(int percent) {
					progressBar.setValue(percent);
					updateGUI();
				}
				@Override
				public void onTransferFailed(String message) {
					progressBar.setValue(0);
					status.setText("Transfer failed: " + message);
					updateGUI();
				}
				@Override
				public void onTransferMessage(String message) {
					status.setText(message);
				}
			});
		}
		return progressBar;
	}

}
