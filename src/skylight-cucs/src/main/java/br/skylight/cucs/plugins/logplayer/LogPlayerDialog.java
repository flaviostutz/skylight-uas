package br.skylight.cucs.plugins.logplayer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileFilter;

import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.DirectDataTerminal;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.datarecorder.LogExporter;
import br.skylight.commons.plugins.datarecorder.LogExporterListener;
import br.skylight.commons.services.StorageService;

@ManagedMember
public class LogPlayerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JButton openLogButton = null;
	private JButton playButton = null;

	private JSlider slider = null;
	private JLabel currentTime = null;
	private JLabel logLabel = null;

	private boolean sliderBeingDragged;

	private ImageIcon startIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/start.gif"));  //  @jve:decl-index=0:
	private ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/pause.gif"));  //  @jve:decl-index=0:
	
	private ImageIcon openIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif"));
	private ImageIcon disconnectIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/disconnect.gif"));  //  @jve:decl-index=0:
	
	private LogPlayer logPlayer;  //  @jve:decl-index=0:

	private JComboBox speedCombo = null;
	private File lastLogFileSelected;  //  @jve:decl-index=0:
	private JPanel jContentPane = null;
	
	private ThreadWorker sliderAnimator;  //  @jve:decl-index=0:
	
	private DirectDataTerminal directDataTerminal;
	private DataTerminal cucsDataTerminal;  //  @jve:decl-index=0:

	@ServiceInjection
	public StorageService storageService;
	
	@ServiceInjection
	public MessagingService messagingService;

	private JButton exportButton = null;

	private JLabel info = null;

	public LogPlayerDialog() {
		this(null);
	}
	
	/**
	 * @param owner
	 */
	public LogPlayerDialog(Frame owner) {
		super(owner);
		directDataTerminal = new DirectDataTerminal();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(405, 137);
		this.setTitle("Log Player");
		this.setContentPane(getJContentPane());
		this.setAlwaysOnTop(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 3;
			gridBagConstraints22.anchor = GridBagConstraints.EAST;
			gridBagConstraints22.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints22.gridy = 3;
			info = new JLabel();
			info.setText("");
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 7;
			gridBagConstraints17.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints17.gridy = 3;
			jContentPane = new JPanel();
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.NONE;
			gridBagConstraints16.gridy = 3;
			gridBagConstraints16.weightx = 0.0;
			gridBagConstraints16.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints16.gridwidth = 1;
			gridBagConstraints16.anchor = GridBagConstraints.CENTER;
			gridBagConstraints16.gridx = 2;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 7;
			gridBagConstraints15.insets = new Insets(0, 5, 5, 6);
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.gridy = 2;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.gridwidth = 3;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridy = 0;
			logLabel = new JLabel();
			logLabel.setText("[No log file opened]");
			logLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 6;
			gridBagConstraints21.insets = new Insets(6, 5, 0, 6);
			gridBagConstraints21.anchor = GridBagConstraints.EAST;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.weightx = 0.0;
			gridBagConstraints21.gridy = 0;
			currentTime = new JLabel();
			currentTime.setText("0s");
			currentTime.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.gridy = 2;
			gridBagConstraints13.weightx = 0.0;
			gridBagConstraints13.insets = new Insets(0, 6, 5, 0);
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.gridwidth = 7;
			gridBagConstraints13.gridx = 0;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints12.fill = GridBagConstraints.NONE;
			gridBagConstraints12.gridy = 3;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 0.0;
			gridBagConstraints3.gridwidth = 8;
			gridBagConstraints3.insets = new Insets(8, 5, 7, 5);
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 5;
			gridBagConstraints2.insets = new Insets(0, 5, 6, 5);
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 3;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 6;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(0, 0, 6, 0);
			gridBagConstraints1.gridy = 3;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 2;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getOpenLogButton(), gridBagConstraints11);
			jContentPane.add(getSlider(), gridBagConstraints3);
			jContentPane.add(getPlayButton(), gridBagConstraints12);
			jContentPane.add(currentTime, gridBagConstraints21);
			jContentPane.add(logLabel, gridBagConstraints14);
			jContentPane.add(getSpeedCombo(), gridBagConstraints16);
			jContentPane.add(getExportButton(), gridBagConstraints17);
			jContentPane.add(info, gridBagConstraints22);
		}
		return jContentPane;
	}

	/**
	 * This method initializes openLogButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOpenLogButton() {
		if (openLogButton == null) {
			openLogButton = new JButton();
			openLogButton.setText("");
			openLogButton.setIcon(openIcon);
			openLogButton.setToolTipText("Open log file");
			openLogButton.setMargin(ViewHelper.getDefaultButtonMargin());
			openLogButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//open log and connect ground station to it
					if(lastLogFileSelected==null) {
						JFileChooser fc = new JFileChooser(storageService.getBaseDir());
						fc.setFileFilter(getFlightLogFilter());
						// Show open dialog; this method does not return until the dialog is closed
						fc.showOpenDialog(getThis());
						File selFile = fc.getSelectedFile();
						if(selFile!=null) {
							loadFlightLog(selFile);
						}
						
					//disconnect ground station from current log
					} else {
						try {
							unloadFlightLog();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return openLogButton;
	}
	
	private JDialog getThis() {
		return this;
	}

	private void loadFlightLog(File inFile) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		lastLogFileSelected = inFile;
		try {
			//keep original data terminal and connect messaging service to a direct data terminal so logger will output contents to this data terminal
			cucsDataTerminal = messagingService.getDataTerminal();
//			directDataTerminal.setStatisticsEnabled(true);
			messagingService.bindTo(directDataTerminal);
			directDataTerminal.activate();
			
			logPlayer = new LogPlayer(inFile, directDataTerminal, new LogPlayerListener() {
				public void onTimeElapsed(long elapsedTime) {
				}
				public void onEndReached() {
					logPlayer.stopCurrentPlay();
					setTime(logPlayer.getBeginPacketTime());
					reloadGUI();
				}
			});
			
			//update screen
			reloadGUI();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, "Problem opening log file (" + inFile.getAbsolutePath() + "): " + e1, "Read error", JOptionPane.ERROR_MESSAGE);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void unloadFlightLog() throws Exception {
		if(logPlayer!=null) {
			logPlayer.stopCurrentPlay();
		}
		logPlayer = null;
		lastLogFileSelected = null;
		//rebind original data terminal to start receiving messages from the network
		messagingService.bindTo(cucsDataTerminal);
		directDataTerminal.forceDeactivation(1500);
		sliderAnimator.deactivate();
		reloadGUI();
	}

	private void reloadGUI() {
		boolean connected = lastLogFileSelected!=null;
		getSlider().setEnabled(connected);
		getPlayButton().setEnabled(connected);
		getSlider().setEnabled(connected);
		getSpeedCombo().setEnabled(connected);
		getExportButton().setEnabled(connected);
		currentTime.setEnabled(connected);
		if(!connected) {
			logLabel.setText("[No log file selected]");
			currentTime.setText("0s");
			getPlayButton().setIcon(startIcon);
			getOpenLogButton().setIcon(openIcon);
			getOpenLogButton().setToolTipText("Open log file to be analysed");
		} else {
			if(logPlayer.isStarted()) {
				playButton.setIcon(pauseIcon);
			} else {
				playButton.setIcon(startIcon);
			}
			logLabel.setText(lastLogFileSelected.getName());
			getSlider().setMaximum((int)(logPlayer.getEndPacketTime()-logPlayer.getBeginPacketTime()));
			getOpenLogButton().setIcon(disconnectIcon);
			getOpenLogButton().setToolTipText("Close current log file");
		}
	}

	private FileFilter getFlightLogFilter() {
		FileFilter ff = new FileFilter() {
			public String getDescription() {
				return "Skylight message logs (*.in)";
			}
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith(".in");
			}
		};
		return ff;
	}
	
	/**
	 * This method initializes playButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPlayButton() {
		if (playButton == null) {
			playButton = new JButton();
			playButton.setIcon(startIcon);
			playButton.setToolTipText("Play log file");
			playButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(logPlayer.isStarted()) {
						logPlayer.stopCurrentPlay();
						try {
							sliderAnimator.deactivate();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						try {
							messagingService.useNextPacketForSyncronization();
							logPlayer.setSpeed(getSelectedSpeed());
							logPlayer.start();
							sliderAnimator.activate();
						} catch (Exception e1) {
							e1.printStackTrace();
							ViewHelper.showException(e1);
						}
					}
					reloadGUI();
				}
			});
		}
		return playButton;
	}

	/**
	 * This method initializes slider	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider();
			slider.setValue(0);
			slider.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(MouseEvent arg0) {
					sliderBeingDragged = true;
				}
				public void mouseReleased(MouseEvent arg0) {
					//use thread so this will take place when slider value is updated
					Thread t = new Thread() {
						public void run() {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}
							setTime(logPlayer.getBeginPacketTime() + slider.getValue());
							sliderBeingDragged = false;
						}

					};
					t.start();
				}
			});
			slider.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					currentTime.setText(StringHelper.formatElapsedTime(getSlider().getValue()/1000));
				}
			});
			sliderAnimator = new ThreadWorker(5) {
				@Override
				public void onActivate() throws Exception {
					setName("LogPlayerDialog.sliderAnimator");
				}
				@Override
				public void step() throws Exception {
					if(!sliderBeingDragged) {
//						slider.setValue((int)(lastElapsedTime + (System.currentTimeMillis()-lastElapsedNotificationTime)));
						slider.setValue((int)logPlayer.getEstimatedElapsedTime());
					}
				};
			};
		}
		return slider;
	}

	private void setTime(long time) {
		boolean wasStarted = logPlayer.isStarted();
		messagingService.useNextPacketForSyncronization();
		logPlayer.setTime(time);
		if(wasStarted) {
			try {
				logPlayer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		slider.setValue((int)(logPlayer.getTimeElapsed()));
	}

	/**
	 * This method initializes speedCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSpeedCombo() {
		if (speedCombo == null) {
			speedCombo = new JComboBox();
			speedCombo.setPreferredSize(new Dimension(51, 20));
			speedCombo.setToolTipText("Play speed");
			speedCombo.setFont(new Font("Dialog", Font.PLAIN, 10));
			DefaultComboBoxModel items = new DefaultComboBoxModel();
			items.addElement("x0.1");//0
			items.addElement("x0.2");//1
			items.addElement("x0.5");//2
			items.addElement("x1");//3
			items.addElement("x2");//4
			items.addElement("x4");//5
			items.addElement("x10");//6
			items.addElement("x20");//7
			speedCombo.setModel(items);
			speedCombo.setSelectedIndex(3);
			speedCombo.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(logPlayer==null) return;
					logPlayer.setSpeed(getSelectedSpeed());
				}
			});
		}
		return speedCombo;
	}

	protected float getSelectedSpeed() {
		float speed = 1;
		if(speedCombo.getSelectedIndex()==0) {
			speed = 0.1F;
		} else if(speedCombo.getSelectedIndex()==1) {
			speed = 0.2F;
		} else if(speedCombo.getSelectedIndex()==2) {
			speed = 0.5F;
		} else if(speedCombo.getSelectedIndex()==3) {
			speed = 1F;
		} else if(speedCombo.getSelectedIndex()==4) {
			speed = 2F;
		} else if(speedCombo.getSelectedIndex()==5) {
			speed = 4F;
		} else if(speedCombo.getSelectedIndex()==6) {
			speed = 10F;
		} else if(speedCombo.getSelectedIndex()==7) {
			speed = 20F;
		}
		return speed;
	}

	/**
	 * This method initializes exportButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getExportButton() {
		if (exportButton == null) {
			exportButton = new JButton();
			exportButton.setText("Export...");
			exportButton.setMargin(ViewHelper.getDefaultButtonMargin());
			exportButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fc = new JFileChooser(storageService.getBaseDir());
					fc.setApproveButtonText("Save");
					fc.setFileFilter(new FileFilter() {
						public String getDescription() {
							return "CSV file (*.csv)";
						}
						public boolean accept(File file) {
							return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
						}
					});

					// Show open dialog; this method does not return until the dialog is closed
					fc.showOpenDialog(getThis());
					File selFile = fc.getSelectedFile();
					if(selFile!=null) {
						if(selFile.exists()) {
							if(JOptionPane.showConfirmDialog(getThis(), "File already exists. Confirm overwrite?")!=JOptionPane.YES_OPTION) {
								return;
							}
						}
						File outLogFile = new File(lastLogFileSelected.getPath().replaceFirst("\\.in", "\\.out"));
						try {
							LogExporter.exportLogsToCSV(lastLogFileSelected, outLogFile, selFile, new LogExporterListener() {
								@Override
								public void onProgress(float total) {
									info.setText("Exporting... " + (int)(total*100) + "%");
									getThis().repaint();
								}
							});
						} catch (IOException e1) {
							e1.printStackTrace();
							ViewHelper.showException(e1);
						} finally {
							info.setText("");
						}
					}
				}
			});
		}
		return exportButton;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
