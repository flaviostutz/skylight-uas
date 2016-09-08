package br.skylight.cucs.widgets;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FeebackToggleTest extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private RoundButton roundButton = null;
	private JButton jButton = null;
	private RoundButton feedbackButton = null;
	private FeedbackButton feedbackButton1 = null;
	/**
	 * This is the default constructor
	 */
	public FeebackToggleTest() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(276, 153);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 1.0;
			gridBagConstraints10.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints10.gridy = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints6.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getRoundButton(), gridBagConstraints);
			jContentPane.add(getJButton(), gridBagConstraints6);
			jContentPane.add(getFeedbackButton(), gridBagConstraints9);
			jContentPane.add(getFeedbackButton1(), gridBagConstraints10);
		}
		return jContentPane;
	}

	/**
	 * This method initializes roundButton	
	 * 	
	 * @return br.skylight.cucs.widgets.roundButton	
	 */
	private RoundButton getRoundButton() {
		if (roundButton == null) {
			roundButton = new RoundButton();
			roundButton.setText("Test");
			roundButton.setShowFeedbackMark(false);
		}
		return roundButton;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Test");
		}
		return jButton;
	}

	/**
	 * This method initializes feedbackButton	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private RoundButton getFeedbackButton() {
		if (feedbackButton == null) {
			feedbackButton = new RoundButton();
			feedbackButton.setText("Nav");
			feedbackButton.setColorSelected(Color.BLUE);
			feedbackButton.setColorUnselected(Color.RED);
		}
		return feedbackButton;
	}

	/**
	 * This method initializes feedbackButton1	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getFeedbackButton1() {
		if (feedbackButton1 == null) {
			feedbackButton1 = new FeedbackButton();
			feedbackButton1.setText("Landing");
			feedbackButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					feedbackButton.setSelected(true);
					feedbackButton1.setSelected(!feedbackButton1.isSelected());
				}
			});
		}
		return feedbackButton1;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
