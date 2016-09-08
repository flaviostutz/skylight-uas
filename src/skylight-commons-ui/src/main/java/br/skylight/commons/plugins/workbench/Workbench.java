package br.skylight.commons.plugins.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.DockTheme;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=Workbench.class)
public class Workbench extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Workbench.class.getName());  //  @jve:decl-index=0:
	private static final String FILENAME_WORKBENCH = "workbench-general.dat";
	private static final String FILENAME_PERSPECTIVES = "workbench-perspectives.dat";

	public static final String PROPERTY_ICON_URL = "icon.url";
	public static final String PROPERTY_BASE_DIR = "base.dir";
	
	private JPanel jContentPane = null;
	private JMenuBar jMenuBar = null;
	private JMenu viewsMenu = null;
	private JMenu toolsMenu = null;

	private Perspective currentPerspective; // @jve:decl-index=0:
	private JToolBar toolBar = null;
	private JButton addPerspective = null;
	private List<Perspective> perspectives = new ArrayList<Perspective>();  //  @jve:decl-index=0:
	private Map<Perspective, JPopupMenu> contextMenus = new HashMap<Perspective, JPopupMenu>();
	
	private ButtonGroup perspectivesGroup = new ButtonGroup();
	private Map<Class,JRadioButtonMenuItem> themeMenus = new HashMap<Class,JRadioButtonMenuItem>();  //  @jve:decl-index=0:

	private ButtonGroup lookAndFeedGroup = new ButtonGroup();
	private Map<String,JRadioButtonMenuItem> lookAndFeelMenus = new HashMap<String,JRadioButtonMenuItem>();  //  @jve:decl-index=0:
	
	private WorkbenchPreferences preferences;  //  @jve:decl-index=0:
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@ExtensionPointsInjection
	public List<ViewExtensionPoint> viewExtensionPoints;

	@ExtensionPointsInjection
	public List<DialogExtensionPoint> dialogExtensionPoints;
	
	@ServiceInjection
	public StorageService storageService;

	public Workbench() {
		initialize();
	}

	public void loadPreferences() {
		try {
			preferences = storageService.loadState("workbench", FILENAME_WORKBENCH, WorkbenchPreferences.class);
			if(preferences!=null) {
				setPreferredSize(new Dimension(preferences.getWindowWidth(), preferences.getWindowHeight()));
				setLocation(preferences.getWindowPosX(), preferences.getWindowPosY());
				setExtendedState(preferences.getWindowState());
	
				//select last perspective
				if(perspectives.size()>0) {
					if(preferences.getSelectedPerpective()>=perspectives.size()) {
						preferences.setSelectedPerpective(0);
					}
					switchPerspective(perspectives.get(preferences.getSelectedPerpective()));
				}
				
				//select last look and feel
				changeLookAndFeel(preferences.getSelectedLookAndFeelClassName());
			}
			
		} catch(Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {}
		}
	}
	private void savePreferences() throws IOException {
		if(preferences==null) {
			preferences = new WorkbenchPreferences();
		}
		preferences.setWindowState(getState());
		preferences.setWindowPosX(getX());
		preferences.setWindowPosY(getY());
		preferences.setWindowWidth(getWidth());
		preferences.setWindowHeight(getHeight());
		int c = 0;
		for (Perspective p : perspectives) {
			if(p.equals(currentPerspective)) {
				break;
			} else {
				c++;
			}
		}
		preferences.setSelectedPerpective(c);
		preferences.setSelectedLookAndFeelClassName(UIManager.getLookAndFeel().getClass().getName());
		storageService.saveState(preferences, "workbench", FILENAME_WORKBENCH);
	}
	
	private void changeLookAndFeel(String lookAndFeelClassName) {
		try {
			//workaround so that all perspective themes that uses look and feel colors 
			//are notified of look and feel color changes upon l&f change
			for (Perspective p : perspectives) {
				if(!p.equals(currentPerspective)) {
					add(p.getContentArea());
				}
			}

			//update look and feel
			UIManager.setLookAndFeel(lookAndFeelClassName);
			SwingUtilities.updateComponentTreeUI(getThis());

			//workaround for handling look and feel color schemes changes
			for (Perspective p : perspectives) {
				if(!p.equals(currentPerspective)) {
					remove(p.getContentArea());
				}
			}
			switchPerspective(currentPerspective);
			pack();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void loadPerspectives() {
		try {
			File file = storageService.resolveFile("workbench", FILENAME_PERSPECTIVES);
			FileInputStream fis = new FileInputStream(file);
			
			//look for previously saved perspectives
			if(fis.available()>0) {
				DataInputStream dis = new DataInputStream(fis);
				
				//number of perspectives
				int s = dis.readInt();
				
				//load each perspective
				for(int i=0; i<s; i++) {
					Perspective pv = new Perspective(this,viewExtensionPoints);
					pv.read(dis);
					installPerspective(pv);
				}
				
			//create default perspectives
			} else {
				Perspective newPerspective = new Perspective(this,viewExtensionPoints);
				newPerspective.setName("Basic");
				installPerspective(newPerspective);
				switchPerspective(newPerspective);
			}
			fis.close();
		} catch(Exception e) {
			logger.throwing(null, null, e);
			logger.warning("Error loading perpectives. e=" + e.getMessage());
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {}
		}
	}

	private void savePerspectives() throws IOException {
		File file = storageService.resolveFile("workbench", FILENAME_PERSPECTIVES);
		FileOutputStream fos = new FileOutputStream(file);
		DataOutputStream dos = new DataOutputStream(fos);
		
		//number of perspectives
		dos.writeInt(perspectives.size());
		
		//save each perspective
		for (Perspective perspective : perspectives) {
			perspective.write(dos);
		}
		fos.flush();
		fos.close();
	}

	public void loadExtensionPoints() {
		//order dialogs according to menu order
		List<MenuableExtensionPoint> menuables = new ArrayList<MenuableExtensionPoint>();
		menuables.addAll(dialogExtensionPoints);
		menuables.addAll(viewExtensionPoints);
		
		Collections.sort(menuables, new Comparator<MenuableExtensionPoint>() {
			@Override
			public int compare(MenuableExtensionPoint o1, MenuableExtensionPoint o2) {
				//TODO consider declared menu order or alphabetical?
				if(true) return o1.getMenuLabel().compareTo(o2.getMenuLabel());
				if(o1.getMenuOrder()<o2.getMenuOrder()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
		//create menus
		for (Object d : menuables) {
			if(d instanceof DialogExtensionPoint) {
				getToolsMenu().add(createMenuItem((DialogExtensionPoint)d));
			} else if(d instanceof ViewExtensionPoint) {
				getViewsMenu().add(createMenuItem((ViewExtensionPoint)d));
			}
		}
	}
	

	private void initialize() {
		this.setSize(554, 316);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(getJContentPane());
		this.setTitle("Skylight Ground Control Station");
		String pv = System.getProperty(Workbench.PROPERTY_ICON_URL);
		if(pv!=null) {
			try {
				this.setIconImage(Toolkit.getDefaultToolkit().getImage(new URL(pv)));
			} catch (MalformedURLException e1) {
				logger.throwing(null,null,e1);
			}
		}
		this.setJMenuBar(getMainBar());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});
	}

	private JMenuBar getMainBar() {
		if (jMenuBar == null) {
			jMenuBar = new JMenuBar();

			//FILE MENU
			JMenu file = new JMenu("File");
			JMenuItem exit = new JMenuItem("Exit");
			exit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shutdown();
				}
			});
			file.add(new JSeparator());
			file.add(exit);
			jMenuBar.add(file);
			
			//TOOLS MENU
			jMenuBar.add(getToolsMenu());
			
			//WINDOW MENU
			JMenu window = new JMenu("Window");

			JMenu lfMenu = new JMenu("Look and feel");
			lfMenu.add(createMenuItem("System default", UIManager.getSystemLookAndFeelClassName()));
			for (LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
				if(!lfi.getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
					lfMenu.add(createMenuItem(lfi.getName(), lfi.getClassName()));
				}
			}
			window.add(lfMenu);
			
			window.add(new JSeparator());
			JMenuItem perspectiveMenu = new JMenuItem("New perspective");
			perspectiveMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createNewPerspective();
				}
			});
			window.add(perspectiveMenu);
			
			JMenu themesMenu = new JMenu("Perpective theme");
			themesMenu.add(createMenuItem("Default", new NoStackBasicTheme()));
			themesMenu.add(createMenuItem("Bubble", new NoStackBubbleTheme()));
			themesMenu.add(createMenuItem("Eclipse", new EclipseTheme()));
			themesMenu.add(createMenuItem("Flat", new NoStackFlatTheme()));
			themesMenu.add(createMenuItem("Smooth", new NoStackSmoothTheme()));
			window.add(themesMenu);

			window.add(new JSeparator());
			window.add(getViewsMenu());
			jMenuBar.add(window);
			
			//HELP MENU
			JMenu help = new JMenu("Help");
			JMenuItem about = new JMenuItem("About");
			help.add(new JSeparator());
			help.add(about);
			jMenuBar.add(help);
		}
		return jMenuBar;
	}
	
	protected void shutdown() {
		try {
			savePerspectives();
			savePreferences();
			pluginManager.shutdownPlugins();
			System.exit(0);
		} catch (IOException e1) {
			ViewHelper.showException(null, e1);
			System.exit(1);
		}
	}

	private JMenu getViewsMenu() {
		if (viewsMenu == null) {
			viewsMenu = new JMenu("Add view");
		}
		return viewsMenu;
	}
	private JMenu getToolsMenu() {
		if (toolsMenu == null) {
			toolsMenu = new JMenu("Tools");
		}
		return toolsMenu;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getToolBar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	private JMenuItem createMenuItem(final DialogExtensionPoint dep) {
		JMenuItem item = new JMenuItem(dep.getMenuLabel());
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = dep.getDialog(getThis());
				ViewHelper.centerWindow(dialog);
				dialog.setVisible(true);
			}
		});
		return item;
	}
	private JMenuItem createMenuItem(String text, final String lookAndFeelClassName) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		lookAndFeedGroup.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					changeLookAndFeel(lookAndFeelClassName);
				} catch (Exception e1) {
					logger.throwing(null, null, e1);
				}
			}
		});
		lookAndFeelMenus.put(lookAndFeelClassName, item);
		return item;
	}

	private JMenuItem createMenuItem(final ViewExtensionPoint view) {
		JMenuItem item = new JMenuItem(view.getMenuLabel());
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					View v = view.createView();
					v.initNewView();
					currentPerspective.addView(v);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(getThis(), "Error adding view to workbench. View log for details.");
					ex.printStackTrace();
					throw new RuntimeException(ex);
				}
			}
		});
		return item;
	}

	private JMenuItem createMenuItem(String text, final DockTheme theme) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPerspective.setDockTheme(theme);
				updateGUI();
			}
		});
		themeMenus.put(theme.getClass(), item);
		return item;
	}

	/**
	 * This method initializes toolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.add(getAddPerspective());
		}
		return toolBar;
	}

	/**
	 * This method initializes addPerspective	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddPerspective() {
		if (addPerspective == null) {
			addPerspective = new JButton();
			addPerspective.setText("+");
			addPerspective.setMargin(ViewHelper.getMinimalButtonMargin());
			addPerspective.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					createNewPerspective();
				}
			});
		}
		return addPerspective;
	}

	protected void createNewPerspective() {
		String name = JOptionPane.showInputDialog("Perspective name:", "");
		if(name!=null) {
			Perspective newPerspective = new Perspective(this,viewExtensionPoints);
			newPerspective.setName(name);
			installPerspective(newPerspective);
			switchPerspective(newPerspective);
		}
	}

	private void installPerspective(final Perspective newPerspective) {
		//prepare perspective button
		final JToggleButton button = newPerspective.getButton();
		JPopupMenu popup = getContextMenu(newPerspective);
		button.setComponentPopupMenu(popup);
		perspectivesGroup.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPerspective(newPerspective);
			}
		});
		button.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				switchPerspective(newPerspective);
			}
		});
		
		perspectives.add(newPerspective);
		
		updateGUI();
	}

	private void switchPerspective(Perspective toPerspective) {
		if(currentPerspective!=null) {
			remove(currentPerspective.getContentArea());
		}
		
		if(toPerspective==null) {
			return;
		}

		//add station to main frame
		add(toPerspective.getContentArea(),BorderLayout.CENTER);
		validate();
		repaint();

		currentPerspective = toPerspective;
		updateGUI();
	}
	
	public void updateGUI() {
		//update selected theme menu item
		for (Entry<Class,JRadioButtonMenuItem> tm : themeMenus.entrySet()) {
			boolean selected = currentPerspective!=null?tm.getKey().equals(currentPerspective.getDockTheme().getClass()):false;
			tm.getValue().setSelected(selected);
		}
		
		//update selected look and feel menu item
		for (Entry<String,JRadioButtonMenuItem> lm : lookAndFeelMenus.entrySet()) {
			boolean selected = UIManager.getLookAndFeel()!=null?UIManager.getLookAndFeel().getClass().getName().equals(lm.getKey()):false;
			lm.getValue().setSelected(selected);
		}

		//hide tools menu if no item inside
		getToolsMenu().setVisible(getToolsMenu().getItemCount()>0);
		
		//cleanup toolbar
		for(int i=1; i<getToolBar().getComponents().length; i++) {
			getToolBar().remove(1);
		}
		
		//create perspective buttons in toolbar
		for (Perspective perspective : perspectives) {
			getToolBar().add(perspective.getButton());
			if(perspective.equals(currentPerspective)) {
				currentPerspective.getButton().setSelected(true);
			}
		}
		validate();
	}

	protected JPopupMenu getContextMenu(final Perspective perspective) {
		if(contextMenus.get(perspective)==null) {
			final JPopupMenu popup = new JPopupMenu("Perspective");

			//CLONE ACTION
			JMenuItem cloneItem = new JMenuItem("Clone");
			cloneItem.setToolTipText("Clone perspective");
			cloneItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = JOptionPane.showInputDialog("Perspective name:", "");
					if(name!=null) {
						try {
							//clone perspective instance
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);
							perspective.write(dos);
							Perspective newPerspective = new Perspective(getThis(),viewExtensionPoints);
							newPerspective.read(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())));
							newPerspective.setName(name);

							//add cloned perspective to workbench
							installPerspective(newPerspective);
							switchPerspective(newPerspective);
							
						} catch (IOException e1) {
							ViewHelper.showException(null, e1);
						}
					}
				}
			});
			
			//DELETE ACTION
			JMenuItem deleteItem = new JMenuItem("Delete");
			deleteItem.setToolTipText("Delete perspective");
			deleteItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean an = ViewHelper.showConfirmationDialog(getThis(), "Really delete this perspective?");
					if(an) {
						getToolBar().remove(perspective.getButton());
						perspectivesGroup.remove(perspective.getButton());
						perspectives.remove(perspective);
						if(perspectives.size()>0) {
							switchPerspective(perspectives.get(0));
						}
						updateGUI();
					}
				}
			});
			
			//RENAME ACTION
			JMenuItem renameItem = new JMenuItem("Rename");
			renameItem.setToolTipText("Rename perspective");
			renameItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = JOptionPane.showInputDialog("Perspective name:", perspective.getName());
					if(name!=null) {
						perspective.setName(name);
					}
				}
			});
			
			popup.add(renameItem);
			popup.add(deleteItem);
			popup.add(cloneItem);
			contextMenus.put(perspective, popup);
		}
		return contextMenus.get(perspective);
	}

	protected JFrame getThis() {
		return this;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
