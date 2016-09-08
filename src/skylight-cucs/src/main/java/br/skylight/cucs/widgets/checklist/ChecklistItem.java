package br.skylight.cucs.widgets.checklist;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;

public class ChecklistItem extends JPanel {

	private static final long serialVersionUID = 1L;

	private ChecklistItemListener checklistItemListener;  //  @jve:decl-index=0:
	private ChecklistLogger logger;
	private ChecklistItemState state = ChecklistItemState.IDLE;  //  @jve:decl-index=0:
	private String message = null;  //  @jve:decl-index=0:

	private JLabel title = null;
	private JLabel icon = null;

	private JButton checkButton = null;
	private boolean testPending = false;
	
	/**
	 * This is the default constructor
	 */
	public ChecklistItem() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 3;
		gridBagConstraints6.gridy = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 0;
		icon = new JLabel();
		icon.setText(" ");
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		title = new JLabel();
		title.setText("Title");
		title.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.setSize(238, 24);
		this.setLayout(new GridBagLayout());
		this.add(title, gridBagConstraints);
		this.add(icon, gridBagConstraints3);
		this.add(getCheckButton(), gridBagConstraints6);
		refreshGUI();
	}

	private void refreshGUI() {
		this.setToolTipText(StringHelper.toHtml(message));
		icon.setIcon(new ImageIcon(state.getIcon()));
		title.setForeground(state.getForegroundColor());
		title.setFont(state.getFont());
	}

	public void checkTest() {
		if(testPending) {
			CheckItemResult r = checklistItemListener.checkItem();
			if(r!=null) {
				if(logger!=null) {
					logger.logTestResult(title.getText(), r);
				}
				//show resulting state
				setState(r.getState());
				setMessage(r.getMessage());
				testPending = false;
			} else {
				setState(ChecklistItemState.TESTING);
				setMessage("Test pending");
			}
		}
	}
	
	public void performTest() {
		if(checklistItemListener!=null) {
			//show "testing" state
			setState(ChecklistItemState.TESTING);
			setMessage(null);
			testPending = true;
			refreshGUI();
			
			//call test
			try {
				boolean readyToTest = checklistItemListener.prepareItemCheck();
				if(readyToTest) {
					checkTest();
				} else {
					//check in future
					Thread t = new Thread() {
						public void run() {
							try {
								Thread.sleep(5000);
								if(testPending) {
									checkTest();
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
					t.start();
				}
			} catch (Exception e) {
				setState(ChecklistItemState.TEST_ERROR);
				setMessage(e.toString());
			}
			refreshGUI();
		} else {
			setState(ChecklistItemState.TEST_ERROR);
			setMessage("Test not implemented yet");
			refreshGUI();
		}
	}
	
	public void setState(ChecklistItemState state) {
		this.state = state;
		refreshGUI();
	}
	public void setMessage(String message) {
		this.message = message;
		refreshGUI();
	}
	
	public void setChecklistItemListener(ChecklistItemListener checklistItemListener) {
		this.checklistItemListener = checklistItemListener;
	}
	public ChecklistItemListener getChecklistItemListener() {
		return checklistItemListener;
	}

	/**
	 * This method initializes checkButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCheckButton() {
		if (checkButton == null) {
			checkButton = new JButton();
			checkButton.setFont(new Font("Dialog", Font.PLAIN, 9));
			checkButton.setText("Test");
			checkButton.setMargin(ViewHelper.getMinimalButtonMargin());
			checkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							checkButton.setEnabled(false);
							try {
								performTest();
							} finally {
								checkButton.setEnabled(true);
							}
						}
					});
					t.start();
				}
			});
		}
		return checkButton;
	}
	
	public void setTitle(String title0) {
		this.title.setText(title0);
	}
	public String getTitle() {
		return this.title.getText();
	}

	public void setChecklistLogger(ChecklistLogger logger) {
		this.logger = logger;
	}
	
	public void setTestPending(boolean testPending) {
		this.testPending = testPending;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
