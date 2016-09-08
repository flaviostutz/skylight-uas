package br.skylight.cucs.plugins.timer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TimerConfigUI extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel minLabel = null;

	private JCheckBox countDown = null;

	private JSpinner hours = null;

	private JSpinner minutes = null;

	private JSpinner seconds = null;

	private JLabel hoursLabel = null;

	private JLabel minutesLabel = null;

	private JLabel secLabel = null;

	private JButton okButton = null;

	private JButton cancelButton = null;

	private JPanel jPanel = null;

	private JLabel jLabel = null;
	
	private Timer timer;

	/**
	 * @param owner
	 */
	public TimerConfigUI(Timer timer) {
		super();
		this.timer = timer;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(179, 190);
		this.setTitle("Timer configuration");
		this.setContentPane(getMinLabel());
		this.setResizable(false);
	}

	/**
	 * This method initializes minLabel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMinLabel() {
		if (minLabel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.insets = new Insets(4, 4, 2, 0);
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Timer configuration");
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.gridwidth = 3;
			gridBagConstraints41.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints41.weighty = 1.0;
			gridBagConstraints41.anchor = GridBagConstraints.NORTH;
			gridBagConstraints41.gridy = 7;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(0, 4, 3, 10);
			gridBagConstraints6.weighty = 0.0;
			gridBagConstraints6.gridy = 6;
			secLabel = new JLabel();
			secLabel.setText("seconds");
			secLabel.setEnabled(false);
			secLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.insets = new Insets(0, 4, 3, 10);
			gridBagConstraints5.gridy = 5;
			minutesLabel = new JLabel();
			minutesLabel.setText("minutes");
			minutesLabel.setEnabled(false);
			minutesLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.insets = new Insets(0, 4, 3, 10);
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridy = 3;
			hoursLabel = new JLabel();
			hoursLabel.setText("hours");
			hoursLabel.setEnabled(false);
			hoursLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(0, 33, 3, 0);
			gridBagConstraints3.gridy = 6;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(0, 33, 3, 0);
			gridBagConstraints2.gridy = 5;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.insets = new Insets(0, 33, 3, 0);
			gridBagConstraints11.gridy = 3;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.weighty = 0.0;
			gridBagConstraints.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.gridy = 2;
			minLabel = new JPanel();
			minLabel.setLayout(new GridBagLayout());
			minLabel.add(getCountDown(), gridBagConstraints);
			minLabel.add(getHours(), gridBagConstraints11);
			minLabel.add(getMinutes(), gridBagConstraints2);
			minLabel.add(getSeconds(), gridBagConstraints3);
			minLabel.add(hoursLabel, gridBagConstraints4);
			minLabel.add(minutesLabel, gridBagConstraints5);
			minLabel.add(secLabel, gridBagConstraints6);
			minLabel.add(getJPanel(), gridBagConstraints41);
			minLabel.add(jLabel, gridBagConstraints1);
		}
		return minLabel;
	}

	/**
	 * This method initializes countDown	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getCountDown() {
		if (countDown == null) {
			countDown = new JCheckBox();
			countDown.setText("Enable count down");
			countDown.setFont(new Font("Dialog", Font.PLAIN, 12));
			countDown.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = countDown.isSelected();
					getHours().setEnabled(enabled);
					hoursLabel.setEnabled(enabled);
					getMinutes().setEnabled(enabled);
					minutesLabel.setEnabled(enabled);
					getSeconds().setEnabled(enabled);
					secLabel.setEnabled(enabled);
					
					timer.setCountdownEnabled(enabled);
					long countDownTime = ((Integer)getHours().getValue()).intValue()*1000*60*60;
					countDownTime += ((Integer)getMinutes().getValue()).intValue()*1000*60;
					countDownTime += ((Integer)getSeconds().getValue()).intValue()*1000;
					timer.setCountdownTime(countDownTime);
				}
			});
		}
		return countDown;
	}

	/**
	 * This method initializes hours	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getHours() {
		if (hours == null) {
			SpinnerNumberModel sm = new SpinnerNumberModel();
			sm.setValue(new Integer(0));
			hours = new JSpinner(sm);
			hours.setPreferredSize(new Dimension(40, 20));
			hours.setEnabled(false);
		}
		return hours;
	}

	/**
	 * This method initializes minutes	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getMinutes() {
		if (minutes == null) {
			SpinnerNumberModel sm = new SpinnerNumberModel();
			sm.setValue(new Integer(0));
			minutes = new JSpinner(sm);
			minutes.setPreferredSize(new Dimension(40, 20));
			minutes.setEnabled(false);
		}
		return minutes;
	}

	/**
	 * This method initializes seconds	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	public JSpinner getSeconds() {
		if (seconds == null) {
			SpinnerNumberModel sm = new SpinnerNumberModel();
			sm.setValue(new Integer(0));
			seconds = new JSpinner(sm);
			seconds.setPreferredSize(new Dimension(40, 20));
			seconds.setEnabled(false);
		}
		return seconds;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					timer.setCountdownEnabled(getCountDown().isSelected());
					long countdownTime = ((Integer)getHours().getValue()).intValue()*1000*60*60;
					countdownTime += ((Integer)getMinutes().getValue()).intValue()*1000*60;
					countdownTime += ((Integer)getSeconds().getValue()).intValue()*1000;
					countdownTime += 999;
					timer.setCountdownTime(countdownTime);
					setVisible(false);
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getOkButton(), gridBagConstraints7);
			jPanel.add(getCancelButton(), gridBagConstraints8);
		}
		return jPanel;
	}

	public void showScreen() {
		setVisible(true);
		getCountDown().setSelected(timer.isCountdownEnabled());
		int[] t = TimerView.getTimeParts(timer.getCountdownTime());
		getHours().setValue(new Integer(t[0]));
		getMinutes().setValue(new Integer(t[1]));
		getSeconds().setValue(new Integer(t[2]));
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
