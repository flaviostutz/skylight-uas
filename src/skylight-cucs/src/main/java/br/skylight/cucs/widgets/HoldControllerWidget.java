package br.skylight.cucs.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.skylight.PIDControllerState;
import br.skylight.commons.infra.ThreadWorker;

public class HoldControllerWidget extends JPanel {

	private static final long serialVersionUID = -5415965421819680798L;

	private static final Logger logger = Logger.getLogger(HoldControllerWidget.class.getName());
	
	private JLabel label = null;
	private JSpinner inputValue = null;
	private JButton holdButton = null;
	private JButton unholdButton = null;
	private HoldControllerWidgetListener listener; // @jve:decl-index=0:

	private JButton graphButton = null;
	private JLabel feedbackValue = null;
	private JLabel holdSetpoint = null;

	private boolean blinkManualHold;

	private float minValueChange = 3F;
	
	//for incremental controls
	private double currentIncrementValue;
	private double currentValue;
	private ThreadWorker incrementer = null;  //  @jve:decl-index=0:
	private boolean useIncrementalController;
	
	private double lastHoldValueSent = Float.MAX_VALUE;
	private MeasureType measureType;  //  @jve:decl-index=0:

	public HoldControllerWidget() {
		initialize();
		
		//turn incrementer thread on/off on focus
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if(useIncrementalController) {
					if(!incrementer.isActive()) {
						try {
							incrementer.activate();
						} catch (Exception e1) {
							logger.throwing(null,null,e1);
						}
					}
				}
			}
			public void focusLost(FocusEvent e) {
				if(incrementer!=null && incrementer.isActive()) {
					try {
						incrementer.deactivate();
					} catch (Exception e1) {
						logger.throwing(null,null,e1);
					}
				}
			}
		});
	}

	public String getLabel() {
		return label.getText();
	}

	public void setLabel(String labelstr) {
		label.setText(labelstr);
	}

	public double getValue() {
		return (Double) getModel().getValue();
	}

	public void setValue(double value) {
		getModel().setValue(value);
		currentValue = value;
	}

	public void setMeasureType(MeasureType measureType) {
		this.measureType = measureType;
	}
	public MeasureType getMeasureType() {
		return measureType;
	}
	
	public double getStep() {
		return (Double) getModel().getStepSize();
	}

	public void setStep(double step) {
		getModel().setStepSize(step);
	}

	public void setMinValue(double min) {
		getModel().setMinimum(min);
	}

	public double getMinValue() {
		return (Double) getModel().getMinimum();
	}

	public void setMaxValue(double max) {
		getModel().setMaximum(max);
	}

	public double getMaxValue() {
		return (Double) getModel().getMaximum();
	}
	
	private SpinnerNumberModel getModel() {
		return (SpinnerNumberModel) getInputValue().getModel();
	}

	public void initialize() {
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 6;
		gridBagConstraints21.insets = new Insets(2, 1, 0, 0);
		gridBagConstraints21.weightx = 2.0;
		gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints21.gridy = 0;
		holdSetpoint = new JLabel();
		holdSetpoint.setFont(new Font("Dialog", Font.PLAIN, 9));
		holdSetpoint.setForeground(new Color(130, 130, 130));
		holdSetpoint.setPreferredSize(new Dimension(10, 20));
		holdSetpoint.setText(" ");
		holdSetpoint.setToolTipText("Desired value");
		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		gridBagConstraints51.gridx = 5;
		gridBagConstraints51.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints51.weightx = 2.0;
		gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints51.gridy = 0;
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 3;
		gridBagConstraints41.weighty = 1.0;
		gridBagConstraints41.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints41.gridy = 0;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 9;
		gridBagConstraints5.insets = new Insets(0, 1, 0, 0);
		gridBagConstraints5.weighty = 1.0;
		gridBagConstraints5.gridy = 0;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 8;
		gridBagConstraints4.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints4.weighty = 1.0;
		gridBagConstraints4.fill = GridBagConstraints.NONE;
		gridBagConstraints4.gridy = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.weightx = 0.0;
		gridBagConstraints3.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints3.weighty = 1.0;
		gridBagConstraints3.gridx = 7;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 4;
		gridBagConstraints2.anchor = GridBagConstraints.EAST;
		gridBagConstraints2.weightx = 2.0;
		gridBagConstraints2.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.ipadx = 0;
		gridBagConstraints2.gridy = 0;
		label = new JLabel();
		label.setText("Controller name:");
		label.setFont(new Font("Dialog", Font.PLAIN, 11));
		setLayout(new GridBagLayout());
		this.setSize(new Dimension(310, 25));
		this.add(label, gridBagConstraints2);
		this.add(getInputValue(), gridBagConstraints3);
		this.add(getHoldButton(), gridBagConstraints4);
		this.add(getUnholdButton(), gridBagConstraints5);
		this.add(getGraphButton(), gridBagConstraints41);
		this.add(getFeedbackValue(), gridBagConstraints51);
		this.add(holdSetpoint, gridBagConstraints21);
	}

	/**
	 * This method initializes altitudeFromPitch
	 * 
	 * @return javax.swing.JTextField
	 */
	public JSpinner getInputValue() {
		if (inputValue == null) {
			inputValue = new JSpinner(new SpinnerNumberModel(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 1.0));
			inputValue.setPreferredSize(new Dimension(50, 20));
			CUCSViewHelper.setDefaultActionClick(inputValue, getHoldButton());
		}
		return inputValue;
	}

	/**
	 * This method initializes holdButton
	 * 
	 * @return javax.swing.JButton
	 */
	public JButton getHoldButton() {
		if (holdButton == null) {
			holdButton = new JButton();
			holdButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/start.gif")));
			holdButton.setMargin(ViewHelper.getMinimalButtonMargin());
			holdButton.setPreferredSize(new Dimension(20, 20));
			holdButton.setToolTipText("Activate hold");
			holdButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (listener != null) {
						if(measureType!=null) {
							listener.onHoldClicked(measureType.convertToSourceUnit(getValue()));
						} else {
							listener.onHoldClicked(getValue());
						}
					}
					blinkManualHold = true;
				}
			});
		}
		return holdButton;
	}

	/**
	 * This method initializes unholdButton
	 * 
	 * @return javax.swing.JButton
	 */
	public JButton getUnholdButton() {
		if (unholdButton == null) {
			unholdButton = new JButton();
			unholdButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/stop.gif")));
			unholdButton.setMargin(ViewHelper.getMinimalButtonMargin());
			unholdButton.setPreferredSize(new Dimension(20, 20));
			unholdButton.setToolTipText("Unhold");
			unholdButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (listener != null) {
						listener.onUnholdClicked();
						// getUnholdButton().setEnabled(false);
					}
				}
			});
		}
		return unholdButton;
	}

	public void setHoldControllerListener(HoldControllerWidgetListener listener) {
		this.listener = listener;
	}

	public void setHoldActive(boolean active) {
		getUnholdButton().setEnabled(active);
		if (active) {
			getHoldButton().setBackground(Color.RED);
			getHoldButton().setForeground(Color.RED);
			setBackground(CUCSViewHelper.getBrighter(SystemColor.control, -0.8F));
		} else {
			getHoldButton().setBackground(SystemColor.control);
			getHoldButton().setForeground(Color.BLACK);
			setBackground(SystemColor.control);
		}
	}

	/**
	 * This method initializes pidGraphButton
	 * 
	 * @return javax.swing.JButton
	 */
	public JButton getGraphButton() {
		if (graphButton == null) {
			graphButton = new JButton();
			graphButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/graph.gif")));
			graphButton.setMargin(ViewHelper.getMinimalButtonMargin());
			graphButton.setPreferredSize(new Dimension(20, 20));
			graphButton.setToolTipText("Show PID graph");
			graphButton.setEnabled(false);
			graphButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (listener != null)
						listener.onGraphClicked();
				}
			});
		}
		return graphButton;
	}

	/**
	 * This method initializes feedbackValue
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getFeedbackValue() {
		if (feedbackValue == null) {
			feedbackValue = new JLabel();
			feedbackValue.setText(" ");
			feedbackValue.setToolTipText("Real value");
			feedbackValue.setFont(new Font("Dialog", Font.PLAIN, 10));
			feedbackValue.setHorizontalTextPosition(SwingConstants.RIGHT);
			feedbackValue.setHorizontalAlignment(SwingConstants.RIGHT);
			feedbackValue.setPreferredSize(new Dimension(5, 20));
		}
		return feedbackValue;
	}

	public void setFeedback(String value) {
		getFeedbackValue().setText(value);
	}
	
	public void setMinValueChange(float minValueChange) {
		this.minValueChange = minValueChange;
	}
	
	public float getMinValueChange() {
		return minValueChange;
	}
	
	public void setPidData(PIDControllerState data) {
		setHoldActive(data.isActive());
		getGraphButton().setEnabled(data.isActive());
		if (data.isActive()) {
			String fbValue = "" + ((int) data.getFeedbackValue());
			String spValue = "/" + ((int) data.getSetpointValue());
			if(measureType!=null) {
				fbValue = measureType.convertToTargetUnitStr(data.getFeedbackValue(), false);
				spValue = "/" + measureType.convertToTargetUnitStr(data.getSetpointValue(), true);
			}
			holdSetpoint.setText(spValue);
			setFeedback(fbValue);
			
			// blink background if value has changed
			if (blinkManualHold && !holdSetpoint.getText().equals(spValue)) {
				setBackground(ViewHelper.getBrighter(SystemColor.control, -0.77F));
				blinkManualHold = false;
			}
		} else {
			holdSetpoint.setText("");
		}
	}
	
} // @jve:decl-index=0:visual-constraint="10,10"
