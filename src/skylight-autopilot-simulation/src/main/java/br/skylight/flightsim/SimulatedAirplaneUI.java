package br.skylight.flightsim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import br.skylight.commons.infra.VectorHelper;
import br.skylight.commons.j3d.Airplane3dViewer;
import br.skylight.flightsim.flyablebody.BodyPart;
import br.skylight.flightsim.flyablebody.Environment;
import br.skylight.flightsim.flyablebody.PartFaceRect;
import br.skylight.flightsim.rigidbody.PositionedVector;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;

public class SimulatedAirplaneUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private BasicAirplane p;

	private JPanel jPanel = null;
	private JTextField aileron = null;
	private JTextField throttle = null;
	private JTextField elevator = null;
	private JTextField rudder = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JButton jButton = null;
	private JLabel jLabel21 = null;
	private JTextField latitude0 = null;
	private JLabel jLabel4 = null;
	private JTextField long1 = null;
	private JTextField altitude0 = null;
	private JLabel jLabel41 = null;
	private JButton jButton1 = null;
	private JTextField heading0 = null;
	private JLabel jLabel411 = null;
	private JCheckBox automode = null;
	
	private JCheckBox paused = null;

	private Airplane3dViewer airplane = null;
	
	public SimulatedAirplaneUI(BasicAirplane airplane) throws Exception {
		super();
		this.p = airplane;
		initialize();

		// render view
		Thread t = new Thread(new Runnable() {
			public void run() {
//				p.getAirplane().setAngles(Math.toRadians(0), Math.toRadians(45), Math.toRadians(0));
				while (true) {
					try {
						getAirplane().setOrientation(p.getOrientationAngles().x, p.getOrientationAngles().z, p.getOrientationAngles().y);
//						updateVector("N", p.getAirplane().getNormalDir(), Color.RED.darker(), 0.5);
//						updateVector("H", p.getAirplane().getHeadDir(), Color.BLUE.darker(), 0.5);
//						updateVector("S", p.getAirplane().getSideDir(), Color.YELLOW.darker(), 0.5);
//						updateVector("TF", p.getAirplane().getAirfoilLiftForce(), Color.ORANGE.darker(), 0.1);

						//draw drag force vectors
						for (String bpn : p.getParts().keySet()) {
							BodyPart bp = p.getParts().get(bpn);
							for (String fn : bp.getFaces().keySet()) {
								PartFaceRect pf = bp.getFaces().get(fn);
								
								//drag forces
								PositionedVector af = pf.getDragForce();
								if(af.getVector().length()>0) {
//									Vector3d as = p.getTrueAirspeed(pf.getCenterInMainBodyReference());
//									System.out.println(bpn + "("+ fn +"): normal: " + VectorHelper.str(pf.getNormalInMainBodyReference()) + "; force: " + VectorHelper.str(af) + "; IAS: " + VectorHelper.str(pf.getIAS()) + "; AS: " + VectorHelper.str(as));
								}
								getAirplane().updateVector(pf, af.getPoint(), af.getVector(), Color.YELLOW.darker(), 0.01);
//								System.out.println(bpn + "("+ fn +"): normal: " + VectorHelper.str(pf.getNormal()) + "; " + VectorHelper.str(pf.getNormalInMainBodyReference()));
//								updateVector(pf, pf.getCenterInMainBodyReference(), pf.getNormalInMainBodyReference(), Color.YELLOW.darker(), 0.5);
								
								//self forces from parts (engine, airfoil etc)
								PositionedVector sf = pf.getSelfForce();
//								getAirplane().updateVector(pf.toString(), sf.getPoint(), sf.getVector(), Color.RED.darker(), 0.01);
								
								//wind speed
//								getAirplane().updateVector(pf.toString(), pf.getAirspeed().getPoint(), pf.getAirspeed().getVector(), Color.LIGHT_GRAY.darker(), 0.01);
							}
						}
						
						//draw speed vector
						Vector3d v = new Vector3d(p.getVelocity());
						getAirplane().updateVector("V", new Point3d(), v, Color.PINK.darker(), 0.5);

						//draw gravity force vector
						getAirplane().updateVector("VG", new Point3d(), p.getWeightForce(), Color.RED.darker(), 0.01);

						Thread.sleep(40);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 * @throws ParsingErrorException
	 * @throws IncorrectFormatException
	 * @throws FileNotFoundException
	 */
	private void initialize() throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		this.setSize(800, 600);
		this.setContentPane(getJContentPane());
		this.setTitle("Simulated plane");
	}

	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 * @throws ParsingErrorException
	 * @throws IncorrectFormatException
	 * @throws FileNotFoundException
	 */
	private JPanel getJContentPane() throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.EAST);
			jContentPane.add(getAirplane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridwidth = 2;
			gridBagConstraints15.insets = new Insets(3, 8, 0, 0);
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.gridy = 0;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.gridwidth = 2;
			gridBagConstraints14.insets = new Insets(0, 8, 0, 0);
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints51.anchor = GridBagConstraints.EAST;
			gridBagConstraints51.gridy = 10;
			jLabel411 = new JLabel();
			jLabel411.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel411.setText("Heading:");
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.fill = GridBagConstraints.BOTH;
			gridBagConstraints42.gridy = 10;
			gridBagConstraints42.weightx = 1.0;
			gridBagConstraints42.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints42.gridx = 1;
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 0;
			gridBagConstraints32.gridwidth = 2;
			gridBagConstraints32.weighty = 1.0;
			gridBagConstraints32.anchor = GridBagConstraints.NORTH;
			gridBagConstraints32.gridy = 11;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.anchor = GridBagConstraints.EAST;
			gridBagConstraints22.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints22.gridy = 9;
			jLabel41 = new JLabel();
			jLabel41.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel41.setText("Altitude:");
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.fill = GridBagConstraints.BOTH;
			gridBagConstraints13.gridy = 9;
			gridBagConstraints13.weightx = 1.0;
			gridBagConstraints13.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints13.gridx = 1;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.fill = GridBagConstraints.BOTH;
			gridBagConstraints41.gridy = 8;
			gridBagConstraints41.weightx = 1.0;
			gridBagConstraints41.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints41.gridx = 1;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.anchor = GridBagConstraints.EAST;
			gridBagConstraints31.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints31.gridy = 8;
			jLabel4 = new JLabel();
			jLabel4.setText("Long. ref.:");
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridy = 7;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints21.gridx = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints12.anchor = GridBagConstraints.EAST;
			gridBagConstraints12.gridy = 7;
			jLabel21 = new JLabel();
			jLabel21.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel21.setText("Lat. ref.:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridwidth = 2;
			gridBagConstraints7.insets = new Insets(1, 0, 15, 0);
			gridBagConstraints7.weighty = 0.0;
			gridBagConstraints7.anchor = GridBagConstraints.NORTH;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.NONE;
			gridBagConstraints7.gridy = 6;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints6.gridy = 5;
			jLabel3 = new JLabel();
			jLabel3.setText("Throttle:");
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints5.gridy = 4;
			jLabel2 = new JLabel();
			jLabel2.setText("Rudder:");
			jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints4.gridy = 3;
			jLabel1 = new JLabel();
			jLabel1.setText("Elevator:");
			jLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(3, 5, 3, 5);
			gridBagConstraints3.weighty = 0.0;
			gridBagConstraints3.gridy = 2;
			jLabel = new JLabel();
			jLabel.setText("Aileron:");
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 4;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 3;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints11.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 5;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(3, 0, 3, 5);
			gridBagConstraints.gridx = 1;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new Dimension(100, 106));
			jPanel.add(getAileron(), gridBagConstraints);
			jPanel.add(getThrottle(), gridBagConstraints1);
			jPanel.add(getElevator(), gridBagConstraints11);
			jPanel.add(getRudder(), gridBagConstraints2);
			jPanel.add(jLabel, gridBagConstraints3);
			jPanel.add(jLabel1, gridBagConstraints4);
			jPanel.add(jLabel2, gridBagConstraints5);
			jPanel.add(jLabel3, gridBagConstraints6);
			jPanel.add(getJButton(), gridBagConstraints7);
			jPanel.add(jLabel21, gridBagConstraints12);
			jPanel.add(getLatitude0(), gridBagConstraints21);
			jPanel.add(jLabel4, gridBagConstraints31);
			jPanel.add(getLong1(), gridBagConstraints41);
			jPanel.add(getAltitude0(), gridBagConstraints13);
			jPanel.add(jLabel41, gridBagConstraints22);
			jPanel.add(getJButton1(), gridBagConstraints32);
			jPanel.add(getHeading0(), gridBagConstraints42);
			jPanel.add(jLabel411, gridBagConstraints51);
			jPanel.add(getAutomode(), gridBagConstraints14);
			jPanel.add(getPaused(), gridBagConstraints15);
		}
		return jPanel;
	}

	/**
	 * This method initializes aileron
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getAileron() {
		if (aileron == null) {
			aileron = new JTextField();
			aileron.setText("0");
			aileron.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						p.setAileron(Double.parseDouble(aileron.getText()));
					}
				}

				public void keyReleased(KeyEvent e) {
					resetControllers();
				}
			});
		}
		return aileron;
	}

	public void resetControllers() {
		p.setAileron(0);
		p.setElevator(0);
		p.setRudder(0);
		p.setThrottle(0);
	}

	/**
	 * This method initializes throttle
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getThrottle() {
		if (throttle == null) {
			throttle = new JTextField();
			throttle.setText("0");
			throttle.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						p.setThrottle(Double.parseDouble(throttle.getText()));
					}
				}

				public void keyReleased(KeyEvent e) {
					resetControllers();
				}
			});
		}
		return throttle;
	}

	/**
	 * This method initializes elevator
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getElevator() {
		if (elevator == null) {
			elevator = new JTextField();
			elevator.setText("0");
			elevator.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						p.setElevator(Double.parseDouble(elevator.getText()));
					}
				}

				public void keyReleased(KeyEvent e) {
					resetControllers();
				}
			});
		}
		return elevator;
	}

	/**
	 * This method initializes rudder
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getRudder() {
		if (rudder == null) {
			rudder = new JTextField();
			rudder.setText("0");
			rudder.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						p.setRudder(Double.parseDouble(rudder.getText()));
					}
				}

				public void keyReleased(KeyEvent e) {
					resetControllers();
				}
			});
		}
		return rudder;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Update");
			jButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			jButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					jButton.setBackground(Color.RED);
					p.setAileron(Double.parseDouble(aileron.getText()));
					p.setElevator(Double.parseDouble(elevator.getText()));
					p.setRudder(Double.parseDouble(rudder.getText()));
					p.setThrottle(Double.parseDouble(throttle.getText()));
				}

				public void mouseReleased(MouseEvent e) {
					jButton.setBackground(Color.LIGHT_GRAY);
//					p.setAileron(0);
//					p.setElevator(0);
//					p.setRudder(0);
//					p.setThrottle(0);
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes latitude0
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getLatitude0() {
		if (latitude0 == null) {
			latitude0 = new JTextField();
			latitude0.setText("15");
		}
		return latitude0;
	}

	/**
	 * This method initializes long1
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getLong1() {
		if (long1 == null) {
			long1 = new JTextField();
			long1.setText("45");
		}
		return long1;
	}

	/**
	 * This method initializes altitude0
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getAltitude0() {
		if (altitude0 == null) {
			altitude0 = new JTextField();
			altitude0.setText("400");
		}
		return altitude0;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Update");
			jButton1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					p.setInitialCoordinates(Double.parseDouble(latitude0.getText()), Double.parseDouble(long1.getText()));
					p.setAltitude(Float.parseFloat(altitude0.getText()));
					p.setHeading(Float.parseFloat(heading0.getText()));
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes heading0
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getHeading0() {
		if (heading0 == null) {
			heading0 = new JTextField();
			heading0.setText("400");
		}
		return heading0;
	}

	/**
	 * This method initializes automode
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getAutomode() {
		if (automode == null) {
			automode = new JCheckBox();
			automode.setText("Auto pilot");
			automode.setFont(new Font("Dialog", Font.PLAIN, 12));
			automode.setSelected(true);
		}
		return automode;
	}

	/**
	 * This method initializes paused	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getPaused() {
		if (paused == null) {
			paused = new JCheckBox();
			paused.setFont(new Font("Dialog", Font.PLAIN, 12));
			paused.setText("Paused");
			paused.setSelected(true);
			paused.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					try {
						if(getPaused().isSelected()) {
							p.getEnvironment().deactivate();
						} else {
							p.getEnvironment().activate();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return paused;
	}

	/**
	 * This method initializes airplane	
	 * 	
	 * @return br.skylight.commons.j3d.Airplane3dViewer	
	 */
	private Airplane3dViewer getAirplane() {
		if (airplane == null) {
			airplane = new Airplane3dViewer(SceneHelper.createFlyableRigidBodyGroup(p));
			airplane.setShowAxis(true);
		}
		return airplane;
	}

	public static void main(String[] args) throws Exception {
		if(false) {
			BasicAirplane r = new BasicAirplane(new Environment());
			r.setOrientationAngles(new Vector3d(Math.toRadians(90), Math.toRadians(0), 0));
			r.setMass(30);
			r.applyTorque(new Vector3d(0, 0, 15), 1);
			r.move(1);
			System.out.println(VectorHelper.str(r.getPosition()) + "     " + VectorHelper.str(r.getOrientationAngles()));
			return;
		}
		Environment e = new Environment();
		BasicAirplane ba = new BasicAirplane(e);
		e.addRigidBody(ba);
		ba.setVelocity(new Vector3d(20, 0, 0));
		ba.setPosition(new Vector3d(0,10000,0));
		SimulatedAirplaneUI s = new SimulatedAirplaneUI(ba);
//		e.activate();
		s.setVisible(true);
	}

	public boolean isAutoMode() {
		return getAutomode().isSelected();
	}
	
} // @jve:decl-index=0:visual-constraint="10,10"
