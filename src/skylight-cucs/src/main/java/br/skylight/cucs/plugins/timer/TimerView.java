package br.skylight.cucs.plugins.timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.sound.SoundDefinition;
import br.skylight.cucs.plugins.sound.SoundService;

public class TimerView extends View<TimerState> {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="36,10"
	private JLabel elapsedTime = null;
	private JLabel remaningTime = null;
	private JButton resumeButton = null;
	private JButton stopButton = null;
	private JTextField label = null;
	private JButton countDownButton = null;
	
	private boolean biped = false;
	
	private ImageIcon startIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/start.gif"));
	private ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/pause.gif"));
	private JLabel millis;
	
	private Timer timer;  //  @jve:decl-index=0:
	private NumberFormat nf;
	private NumberFormat nf1;  //  @jve:decl-index=0:
	private JToggleButton soundButton;
	
	private ImageIcon bellIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/bell.gif"));
	private ImageIcon disabledBellIcon = new ImageIcon(getClass().getResource("/br/skylight/cucs/images/bell-disabled.gif"));
	
	private TimerConfigUI timerConfigUI;  //  @jve:decl-index=0:visual-constraint="360,12"
	
	private Border activeBorder = BorderFactory.createLineBorder(Color.BLUE.darker(), 5);
	private Border inactiveBorder = BorderFactory.createLineBorder(new Color(198, 195, 195), 5);  //  @jve:decl-index=0:
	private Border alertBorder = BorderFactory.createLineBorder(Color.RED.darker(), 5);  //  @jve:decl-index=0:

	private static final SoundDefinition SOUND_ALARM = new SoundDefinition(TimerView.class.getResource("/br/skylight/cucs/plugins/timer/alarm.wav"), false);
	private JPanel jPanel = null;
	
	@ServiceInjection
	public SoundService soundService;  //  @jve:decl-index=0:

	public TimerView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
		setTitleText("Timer");
	}
	
	private void initialize() {
		nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(0);
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumIntegerDigits(2);
		
		nf1 = NumberFormat.getNumberInstance();
		nf1.setMinimumFractionDigits(0);
		nf1.setMaximumFractionDigits(0);
		nf1.setMinimumIntegerDigits(3);
		nf1.setMaximumIntegerDigits(3);
		
		timer = new Timer();
		timer.setListener(new TimerListener() {
			public void onTimeElapsed(long et) {
				String[] ft = formatTime(et, false);
				elapsedTime.setText(ft[0]);
				millis.setText("."+ft[1]);
				if(timer.isCountdownEnabled()) {
					remaningTime.setText(formatTime(timer.getRemainingTime(), true)[0]);
					remaningTime.setVisible(true);
					
					//alert if countdown is reached
					if(timer.getRemainingTime()>0) {
						elapsedTime.setForeground(Color.BLACK);
						millis.setForeground(Color.BLACK);
						getContents().setBorder(activeBorder);
					}
				} else {
					remaningTime.setVisible(false);
				}
			}

			@Override
			public void onCountdownFinished() {
				if(((System.currentTimeMillis()/1000)%2)==0) {
					elapsedTime.setForeground(Color.RED);
					millis.setForeground(Color.RED);
					//beep
					if(getSoundButton().isSelected() && !biped) {
						biped = true;
						soundService.playSound(SOUND_ALARM);
					}
				} else {
					elapsedTime.setForeground(Color.BLACK);
					millis.setForeground(Color.BLACK);
					biped = false;
				}
				getContents().setBorder(alertBorder);
			}
		});
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			initialize();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.insets = new Insets(2, 0, 4, 0);
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints6.gridy = 3;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(148, 129));
			contents.setMinimumSize(new Dimension(0,0));

			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 3;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.insets = new Insets(0, 8, 4, 5);
			gridBagConstraints11.gridy = 2;
			millis = new JLabel();
			millis.setText(".000");
			millis.setFont(new Font("Dialog", Font.PLAIN, 11));
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.gridwidth = 4;
			gridBagConstraints31.weighty = 0.0;
			gridBagConstraints31.insets = new Insets(3, 5, 4, 5);
			gridBagConstraints31.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.insets = new Insets(0, 8, 0, 0);
			gridBagConstraints2.gridy = 2;
			remaningTime = new JLabel();
			remaningTime.setText("+00:00:00");
			remaningTime.setFont(new Font("Dialog", Font.PLAIN, 16));
			remaningTime.setForeground(new Color(100, 100, 100));
			remaningTime.setVisible(true);
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridwidth = 4;
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.insets = new Insets(0, 8, 0, 5);
			gridBagConstraints1.gridy = 1;
			elapsedTime = new JLabel();
			elapsedTime.setText("00:00:00");
			elapsedTime.setPreferredSize(new Dimension(104, 22));
			elapsedTime.setFont(new Font("Dialog", Font.BOLD, 26));
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(137, 110));
			contents.add(remaningTime, gridBagConstraints2);
			contents.setBorder(inactiveBorder);
			contents.add(elapsedTime, gridBagConstraints1);
			contents.add(millis, gridBagConstraints11);
			contents.add(getLabel(), gridBagConstraints31);
			contents.add(getJPanel(), gridBagConstraints6);
			
			contents.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
//					remaningTime.setVisible(getSize().getHeight()>84);
//					label.setVisible(getSize().getHeight()>77);
//					millis.setVisible(getSize().getHeight()>30);
				}
			});
		
		}
		return contents;
	}

	//return 0-time without millis; 1-millis
	protected String[] formatTime(long time, boolean showSignal) {
		
		String signal = "";
		if(showSignal) {
			if(time<0) {
				signal = "-";
				time *= -1;
			} else {
				signal = "+";
			}
		}
			
		int[] parts = getTimeParts(time);
		
		return new String[] {
			signal + nf.format(parts[0]) + ":" + nf.format(parts[1]) + ":" + nf.format(parts[2]),
			nf1.format(parts[3])+""
		};
	}

	public static int[] getTimeParts(long time) {
		float t = time;
		
		int hours = (int)Math.floor(t/(1000F*60F*60F));
		t -= hours * (1000F*60F*60F);
		
		int min = (int)Math.floor(t/(1000F*60F));
		t -= min * (1000F*60F);
		
		int sec = (int)Math.floor(t/(1000F));
		t -= sec * (1000F);
		
		int millis = (int)t;
		
		return new int[] {hours, min, sec, millis};
	}
	
	/**
	 * This method initializes resumeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getResumeButton() {
		if (resumeButton == null) {
			resumeButton = new JButton();
			resumeButton.setIcon(startIcon);
			resumeButton.setMargin(ViewHelper.getMinimalButtonMargin());
			resumeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(timer.isStarted()) {
						timer.pauseTimer();
						resumeButton.setIcon(startIcon);
					} else {
						timer.startTimer();
						resumeButton.setIcon(pauseIcon);
					}
					getContents().setBorder(activeBorder);
				}
			});
		}
		return resumeButton;
	}

	/**
	 * This method initializes stopButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getStopButton() {
		if (stopButton == null) {
			stopButton = new JButton();
			stopButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/stop.gif")));
			stopButton.setMargin(ViewHelper.getMinimalButtonMargin());
			stopButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					timer.resetTimer();
					getResumeButton().setIcon(startIcon);
					getContents().setBorder(inactiveBorder);
				}
			});
		}
		return stopButton;
	}

	/**
	 * This method initializes label	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getLabel() {
		if (label == null) {
			label = new JTextField();
			label.setOpaque(false);
			label.setText("Type a label...");
			label.setFont(new Font("Dialog", Font.BOLD, 12));
			label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
			label.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(label.getText().length()>0) {
						label.setSelectionStart(0);
						label.setSelectionEnd(label.getText().length());
					}
				}
			});
		}
		return label;
	}

	/**
	 * This method initializes countDownButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCountDownButton() {
		if (countDownButton == null) {
			countDownButton = new JButton();
			countDownButton.setMargin(ViewHelper.getMinimalButtonMargin());
			countDownButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/hourglass.gif")));
			countDownButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getTimerConfigUI().showScreen();
				}
			});
		}
		return countDownButton;
	}

	/**
	 * This method initializes soundButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getSoundButton() {
		if (soundButton == null) {
			soundButton = new JToggleButton();
			soundButton.setMargin(ViewHelper.getMinimalButtonMargin());
			soundButton.setIcon(bellIcon);
			soundButton.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(soundButton.isSelected()) {
						soundButton.setIcon(bellIcon);
						soundButton.setToolTipText("Disable sound alerts");
					} else {
						soundButton.setIcon(disabledBellIcon);
						soundButton.setToolTipText("Enable sound alerts");
					}
						
				}
			});
		}
		return soundButton;
	}
	
	public TimerConfigUI getTimerConfigUI() {
		if(timerConfigUI==null) {
			timerConfigUI = new TimerConfigUI(timer);
			timerConfigUI.setModal(true);
			ViewHelper.centerWindow(timerConfigUI);
		}
		return timerConfigUI;
	}

	@Override
	protected void onStateUpdated() {
		getLabel().setText(getState().getLabel());
		getSoundButton().setSelected(getState().isSoundEnabled());
		getTimerConfigUI().getCountDown().setSelected(getState().isCountDownEnabled());
		getTimerConfigUI().getSeconds().setValue((int)getState().getCountDownTime()/1000);
		timer.setCountdownEnabled(getState().isCountDownEnabled());
		timer.setCountdownTime(getState().getCountDownTime());
//		timer.notifyTimeElapsed();
	}

	@Override
	protected void prepareState() {
		getState().setLabel(getLabel().getText());
		getState().setCountDownEnabled(timer.isCountdownEnabled());
		getState().setCountDownTime(timer.getCountdownTime());
		getState().setSoundEnabled(getSoundButton().isSelected());
	}

	@Override
	protected void onDeactivate() {
		timer.pauseTimer();
	}
	
	@Override
	protected TimerState instantiateState() {
		return new TimerState();
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 3;
			gridBagConstraints5.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints3.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(0, 8, 0, 0);
			gridBagConstraints.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getResumeButton(), gridBagConstraints);
			jPanel.add(getStopButton(), gridBagConstraints3);
			jPanel.add(getSoundButton(), gridBagConstraints4);
			jPanel.add(getCountDownButton(), gridBagConstraints5);
		}
		return jPanel;
	}
	
	@ServiceInjection
	public void setSoundService(SoundService soundService) {
		this.soundService = soundService;
	}
	
}
