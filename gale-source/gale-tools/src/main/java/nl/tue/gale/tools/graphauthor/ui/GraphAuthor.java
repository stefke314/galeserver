/*

	This file is part of GALE (Generic Adaptation Language and Engine).

    GALE is free software: you can redistribute it and/or modify it under the 
    terms of the GNU Lesser General Public License as published by the Free 
    Software Foundation, either version 3 of the License, or (at your option) 
    any later version.

    GALE is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
    more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GALE. If not, see <http://www.gnu.org/licenses/>.
    
 */
/**
 * GraphAuthor.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.tools.graphauthor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import nl.tue.gale.tools.amt.AMtc;
import nl.tue.gale.tools.amt.ServerFileChooser;
import nl.tue.gale.tools.graphauthor.data.AHAOutConcept;
import nl.tue.gale.tools.graphauthor.data.AuthorSTATIC;
import nl.tue.gale.tools.graphauthor.data.CRTConceptRelationType;
import nl.tue.gale.tools.graphauthor.data.ConceptTemplate;
import nl.tue.gale.tools.graphauthor.data.ReadCRTXML;
import nl.tue.gale.tools.graphauthor.data.ReadTemplateXML;
import nl.tue.gale.tools.util.AuthorLogin;

import com.jgraph.JGraph;
import com.jgraph.event.GraphModelEvent;
import com.jgraph.event.GraphModelListener;
import com.jgraph.graph.BasicMarqueeHandler;
import com.jgraph.graph.CellMapper;
import com.jgraph.graph.CellView;
import com.jgraph.graph.ConnectionSet;
import com.jgraph.graph.DefaultEdge;
import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.DefaultGraphModel;
import com.jgraph.graph.DefaultPort;
import com.jgraph.graph.Edge;
import com.jgraph.graph.EdgeView;
import com.jgraph.graph.GraphConstants;
import com.jgraph.graph.GraphModel;
import com.jgraph.graph.Port;
import com.jgraph.graph.PortView;

/**
 * Main class for the gui.
 * 
 */
public class GraphAuthor extends JApplet implements KeyListener,
		TreeSelectionListener, DropTargetListener {
	public static final String iconpath = "/nl/tue/gale/tools/graphauthor/ui/resource/";

	static MyGraph graph;
	static LinkedList conceptList = new LinkedList();
	static public JTree sharedConceptTree = new JTree();
	static DefaultMutableTreeNode loadTree = null;
	static String projectName = "";
	static Vector visListData = new Vector();
	static Vector filteredListData = new Vector();
	static int opened = 0;
	public static GraphAuthor myinstance = null;
	public boolean graphChanged = false;
	public DragTree ConceptTree = new DragTree();
	private GraphModel model;
	private Map attributes;
	private JPanel pnlMain = new JPanel();
	private JFrame mainFrame;
	private JSplitPane splConceptGraph = new JSplitPane();
	private JButton AddConceptButton = new JButton();
	// private JLabel jLabel2 = new JLabel(); //NOT USED
	private JComboBox RelationList = new JComboBox();
	public ReadCRTXML crt;
	public ReadTemplateXML templateList;
	private Vector listDataRel = new Vector();
	private JButton processButton = new JButton();
	private boolean isStandalone = false;
	public URL home = null;
	private JButton Load_Button = new JButton();
	private JMenuBar menuBar = new JMenuBar();
	private JButton Export_button = new JButton();
	private String selection = "";
	private JScrollPane spnConceptTree = new JScrollPane();
	private JPanel balk = new JPanel();
	// private FlowLayout flowLayout1 = new FlowLayout(); //NOT USED
	// private FlowLayout flowLayout2 = new FlowLayout(); //NOT USED
	public LinkedList treeConceptList = new LinkedList();
	public JScrollPane graphScroll;
	public String selectedFilter = "No filter";
	private JButton filterButton = new JButton();
	private JToolBar toolBar;
	private String cutConcept = "";
	private TreePath cutPath = null;
	private DefaultMutableTreeNode cutTreeNode = null;
	private JCheckBoxMenuItem showAdvanced = null;
	private JMenuItem menuAdvAttributes = null;
	public String dirname;
	// added @David @11-10-2003
	private Termination termination = null;
	// end added @David @11-10-2003
	private boolean setresource;

	/**
	 * Constructor does nothing.
	 */
	public GraphAuthor() {
	}

	/**
	 * Constructor. Initializes the graph author and shows the main frame for a
	 * certain author without the need to log in.
	 * 
	 * @param codebase
	 *            The codebase for this applet. Necessary if this class is not
	 *            the entry class of the started applet. (i.e. to start the
	 *            Graph Author from the AMt)
	 * @param authorname
	 *            the name of the author to load the file for
	 * @param show
	 *            if the mainFrame should be set visible
	 */
	public GraphAuthor(URL codebase, String authorname, boolean show) {

		home = codebase;

		// init graph author
		try {
			AuthorSTATIC.authorName = authorname;

			initAuthor();
			jbInit();
			initTree();
			AddExtraComponents();
			AddMenu();
			// create frame - do not use addMainFrame(), since this also
			// displays the
			// JFrame. This is unwanted.
			mainFrame = new JFrame(
					"Graphical Author tool for AHA! applications v3.0");
			mainFrame.setJMenuBar(menuBar);
			mainFrame.setSize(800, 620);
			mainFrame.setLocation(100, 100);
			mainFrame.getContentPane().add(pnlMain);
			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					mainFrame_Closing(e);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		mainFrame.repaint();

		mainFrame.setVisible(show);
	}

	/**
	 * Constructor. Initializes the graph author, shows the main frame and loads
	 * a .gaf file for a certain author without the need to log in.
	 * 
	 * @param codebase
	 *            The codebase for this applet. Necessary if this class is not
	 *            the entry class of the started applet. (i.e. to start the
	 *            Graph Author from the AMt)
	 * @param authorname
	 *            the name of the author to load the file for
	 * @param filename
	 *            The name of the file to open, ending with ".gaf". This file
	 *            exists in the authorfiles directory of the specified author
	 */
	public GraphAuthor(URL codebase, String authorname, String filename) {

		this(codebase, authorname, false);

		// open .gaf file
		// added @David @14-10-2003
		termination.nocheck();
		// end added @David @14-10-2003

		NewAuthor(null, false);

		AuthorIn ain = new AuthorIn(home, filename);
		ain.LoadFromServer();
		spnConceptTree.getViewport().remove(ConceptTree);
		// added by Natalia Stash
		String tmp = filename.trim();
		if (tmp.endsWith(".gaf"))
			tmp = tmp.substring(0, tmp.length() - 4);
		projectName = tmp;

		ConceptTree = new DragTree();
		initTree();
		spnConceptTree.getViewport().add(ConceptTree, null);
		filterEdges();
		setAdvanced(false);

		// added @David @14-10-2003
		termination.check();
		termination.modelChanged();
		// end added @David @14-10-2003

		mainFrame.setVisible(true);
	}

	public void initAuthor() {
		myinstance = this;
		try {
			conceptList = new LinkedList();
			sharedConceptTree = new JTree();
			loadTree = null;
			projectName = "";
			visListData = new Vector();
			filteredListData = new Vector();

			this.projectName = "unnamed";

			String path = home.getPath();
			String pathttemp = path.substring(1, path.length());
			int index = pathttemp.indexOf("/");
			index++;
			dirname = path.substring(0, index);
			if (dirname.equals("/GraphAuthor")) {
				dirname = "";
			}

			crt = new ReadCRTXML("crtlist.txt", home);
			templateList = new ReadTemplateXML("templatelist.txt", home);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isOpen() {
		return mainFrame.isVisible();
	}

	public void AddMainFrame() {
		try {
			mainFrame = new JFrame(
					"Graphical Author tool for AHA! applications v3.0");
			mainFrame.setJMenuBar(menuBar);
			mainFrame.setSize(800, 620);
			mainFrame.setLocation(100, 100);
			mainFrame.getContentPane().add(pnlMain);
			mainFrame.setVisible(true);
			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					mainFrame_Closing(e);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void mainFrame_Closing(WindowEvent e) {
		int result = 0;

		if (this.graphChanged) {
			result = JOptionPane
					.showConfirmDialog(
							this,
							"There is unsaved data! \n Do you want to save these changes?",
							"alert", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				this.save_Button_actionPerformed(null, false);
				mainFrame.setVisible(false);
				mainFrame.dispose();
			} else {
				if (result == JOptionPane.NO_OPTION) {
					mainFrame.setVisible(false);
					mainFrame.dispose();
				} else {
					mainFrame.setVisible(true);
				}
			}
		} else {
			/*
			 * result = JOptionPane.showConfirmDialog( this,
			 * "This action will exit Graph Author \n Are you sure?", "alert",
			 * JOptionPane.YES_NO_OPTION);
			 * 
			 * if (result == JOptionPane.YES_OPTION) {
			 */
			mainFrame.setVisible(false);
			mainFrame.dispose();
			/*
			 * } else { mainFrame.setVisible(true); }
			 */
		}
	}

	// changed by @Bart @ 27-03-2003, added advanced menu
	public void AddMenu() {
		JMenu fileMenu = new JMenu("File");
		JMenuItem newItem = new JMenuItem("New");

		newItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewAuthor(e, true);
			}
		});

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Load_Button_actionPerformed(e);
			}
		});

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_Button_actionPerformed(e, false);
			}
		});

		JMenuItem saveasItem = new JMenuItem("Save to AHA!");
		saveasItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Export_button_actionPerformed(e);
			}
		});

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit_button_actionPerformed(e);
			}
		});

		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveasItem);
		fileMenu.add(exitItem);

		JMenu conceptMenu = new JMenu("Concept");

		JMenuItem addConceptItem = new JMenuItem("Add Concept");
		addConceptItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddConceptButton_actionPerformed(e);
			}
		});

		JMenuItem filterRelationItem = new JMenuItem("Filter Concept Relations");
		filterRelationItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						filterButton_actionPerformed(e);
					}
				});

		JMenuItem zoomItem = new JMenuItem("Zoom 1:1");
		zoomItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(1.0);
			}
		});

		JMenuItem zoomInItem = new JMenuItem("Zoom In");
		zoomInItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(2 * graph.getScale());
			}
		});

		JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
		zoomOutItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph.setScale(graph.getScale() / 2);
			}
		});

		conceptMenu.add(addConceptItem);
		conceptMenu.addSeparator();
		conceptMenu.add(filterRelationItem);
		conceptMenu.addSeparator();
		conceptMenu.add(zoomItem);
		conceptMenu.add(zoomInItem);
		conceptMenu.add(zoomOutItem);

		// added by @Bart @ 27-03-2003
		JMenu advancedMenu = new JMenu("Advanced");
		showAdvanced = new JCheckBoxMenuItem("Advanced mode");
		setAdvanced(false);
		showAdvanced.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				AbstractButton button = (AbstractButton) e.getItem();
				if (button.isSelected()) {
					setAdvanced(true);
				} else {
					setAdvanced(false);
				}
			}
		});

		advancedMenu.add(showAdvanced);
		// end added by @Bart

		JMenu helpMenu = new JMenu("Help");
		JMenuItem howToItem = new JMenuItem("How to use GraphAuthor");
		howToItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpBox help = new HelpBox(mainFrame, home);
				help.show();
			}
		});

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutBox about = new AboutBox(mainFrame, home);
				about.show();
			}
		});

		helpMenu.add(howToItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);

		menuBar.add(fileMenu);
		menuBar.add(conceptMenu);
		// added by @Bart @ 27-03-2003
		menuBar.add(advancedMenu);
		menuBar.add(helpMenu);
	}

	public void pasteTree() {
		this.graphChanged = true;

		TreePath p = ConceptTree.getSelectionPath();

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) cutPath
				.getLastPathComponent();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
				.getParent();
		parent.remove(node);

		if (p != null) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) p
					.getLastPathComponent();
			n.add(cutTreeNode);
		} else {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) ConceptTree
					.getModel().getRoot();
			n.add(cutTreeNode);
		}

		if (p != null) {
			((DefaultTreeModel) ConceptTree.getModel()).reload();
			ConceptTree.setSelectionPath(p);
			ConceptTree.expandPath(p);
		}
	}

	public void initTree() {
		ConceptTree.addTreeSelectionListener(this);

		// popup menu
		final JPopupMenu jpop = new JPopupMenu();
		JMenuItem addItem = new JMenuItem("add concept");
		JMenuItem cutItem = new JMenuItem("cut");
		JMenuItem pasteItem = new JMenuItem("paste");
		JMenuItem deleteItem = new JMenuItem("delete");
		JMenuItem editItem = new JMenuItem("edit");
		JMenuItem assignResources = new JMenuItem("assign resources");
		menuAdvAttributes = new JMenuItem("attributes");

		addItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddConceptButton_actionPerformed(e);
			}
		});

		cutItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath p = ConceptTree.getSelectionPath();

				if (p != null) {
					cutConcept = selection;
					cutPath = p;
					cutTreeNode = (DefaultMutableTreeNode) p
							.getLastPathComponent();
					JOptionPane
							.showMessageDialog(
									null,
									"Please select the destination node and click on paste",
									"information", JOptionPane.OK_OPTION);
				}
			}
		});

		pasteItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cutTreeNode != null) {
					pasteTree();
				}
			}
		});

		deleteItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conceptTreeKeyEvent((KeyEvent) null);
			}
		});

		editItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editConceptEvent(e);
			}
		});

		// added by @Bart @ 27-03-2003
		assignResources.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// show resources dialog
				assignResourcesEvent(e);
			}
		});

		menuAdvAttributes
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// edit attributes
						editAttributesEvent(e);
					}
				});
		// end added by @Bart @ 27-03-2003

		jpop.add(addItem);
		jpop.addSeparator();
		jpop.add(cutItem);
		jpop.add(pasteItem);
		jpop.add(deleteItem);
		jpop.add(editItem);
		jpop.add(assignResources);
		jpop.add(menuAdvAttributes);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				boolean rightClicked = e.isMetaDown();

				if (rightClicked) {
					String conceptName = projectName;
					TreePath p = ConceptTree.getSelectionPath();

					if (p != null) {
						jpop.show((Component) e.getSource(), e.getX(), e.getY());
					}
				}
			}
		};

		ConceptTree.addMouseListener(mouseListener);

		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					conceptTreeKeyEvent(e);
				}
			}
		};

		ConceptTree.addKeyListener(keyListener);

		DefaultMutableTreeNode n;

		if (loadTree == null) {
			n = new DefaultMutableTreeNode(this.projectName);
		} else {
			// sets the loaded tree
			n = loadTree;

			// loadTree must by null so the system can see that the load
			// function is finished.
			loadTree = null;
		}

		DefaultTreeModel m = new DefaultTreeModel(n);
		ConceptTree.setModel(m);

		// added @David @11-10-2003
		this.sharedConceptTree.setModel(this.ConceptTree.getModel());
		ConceptTree.getModel().addTreeModelListener(new MyTreeModelListener());
		// end added @David

		ConceptTree.invalidate();
	}

	// Get a parameter value
	public String getParameter(String key, String def) {
		return isStandalone ? System.getProperty(key, def)
				: ((getParameter(key) != null) ? getParameter(key) : def);
	}

	// Initialize the applet
	public void init() {
		home = getCodeBase();
		if (opened != 0) {
			JOptionPane.showMessageDialog(null, "Graph Author is already open",
					"information", JOptionPane.OK_OPTION);
			return;
		}

		AuthorLogin alogin = new AuthorLogin(home);
		if (!alogin.login()) {
			try {
				String codebase = home.toString();
				int index = codebase.lastIndexOf("/");
				index = codebase.substring(0, index).lastIndexOf("/");
				String base = codebase.substring(0, index + 1);
				getAppletContext().showDocument(
						new URL(base + "accessdenied.html"), "_top");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			try {

				AuthorSTATIC.authorName = alogin.getUserName();

				this.initAuthor();
				this.jbInit();
				this.initTree();
				this.AddExtraComponents();
				this.AddMenu();
				this.AddMainFrame();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Start the applet
	public void start() {
		opened--;
	}

	// Stop the applet
	public void stop() {
		opened++;
	}

	// Destroy the applet
	public void destroy() {
	}

	// Get Applet information
	public String getAppletInfo() {
		return "GrapAuthor applet v 3.0";
	}

	// Get parameter info
	public String[][] getParameterInfo() {
		return null;
	}

	private void jbInit() throws Exception {

		pnlMain.setLayout(new BorderLayout());
		splConceptGraph.setBorder(BorderFactory.createLoweredBevelBorder());
		splConceptGraph.setLastDividerLocation(50);
		spnConceptTree.setMinimumSize(new Dimension(50, 50));
		spnConceptTree.setPreferredSize(new Dimension(50, 50));
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		this.AddToolBarItems();

		pnlMain.add(toolBar, "North");
		pnlMain.add(splConceptGraph, null);
		splConceptGraph.add(spnConceptTree, JSplitPane.LEFT);
		spnConceptTree.getViewport().add(ConceptTree, null);
		splConceptGraph.setDividerLocation(150);
	}

	public void AddToolBarItems() {
		try {
			JButton buttonNew = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "new.gif")));
			buttonNew.setToolTipText("New AHA! application");
			buttonNew.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					NewAuthor(e, true);
				}
			});
			toolBar.add(buttonNew);

			JButton buttonOpen = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "open.gif")));
			buttonOpen.setToolTipText("Open AHA! application");
			buttonOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Load_Button_actionPerformed(e);
				}
			});
			toolBar.add(buttonOpen);

			JButton buttonSave = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "save.gif")));
			buttonSave.setToolTipText("Save AHA! application");
			buttonSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					save_Button_actionPerformed(e, false);
				}
			});
			toolBar.add(buttonSave);

			JButton buttonExport = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "export.gif")));
			buttonExport.setToolTipText("Commit into AHA! database");
			buttonExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Export_button_actionPerformed(e);
				}
			});
			toolBar.add(buttonExport);
			toolBar.addSeparator();

			JButton buttonAdd = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "add.gif")));
			buttonAdd.setToolTipText("Add new Concept");
			buttonAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AddConceptButton_actionPerformed(e);
				}
			});

			toolBar.add(buttonAdd);

			JButton buttonFilter = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "filter.gif")));
			buttonFilter.setToolTipText("Filter concept relations");
			buttonFilter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					filterButton_actionPerformed(e);
				}
			});
			toolBar.add(buttonFilter);

			JButton buttonCycle = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "cycle.gif")));
			buttonCycle.setToolTipText("Test cycles");
			buttonCycle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TestCycle();
				}
			});
			toolBar.add(buttonCycle);

			toolBar.addSeparator();
			JButton buttonZoom = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "zoom.gif")));
			buttonZoom.setToolTipText("Zoom 1:1");
			buttonZoom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graph.setScale(1.0);
				}
			});
			toolBar.add(buttonZoom);

			JButton buttonZoomIn = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "zoom.gif")));
			buttonZoomIn.setToolTipText("Zoom in");
			buttonZoomIn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graph.setScale(2 * graph.getScale());
				}
			});
			toolBar.add(buttonZoomIn);

			JButton buttonZoomOut = new JButton(new ImageIcon(getClass()
					.getResource(GraphAuthor.iconpath + "zoomout.gif")));
			buttonZoomOut.setToolTipText("Zoom out");
			buttonZoomOut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graph.setScale(graph.getScale() / 2);
				}
			});
			toolBar.add(buttonZoomOut);

			Dimension dim = new Dimension(180, 6);
			toolBar.addSeparator(dim);

			JLabel crtLabel = new JLabel("crt: ");
			toolBar.add(crtLabel);
			RelationList.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					RelationList_actionPerformed(e);
				}
			});
			toolBar.add(RelationList);
		} catch (Exception e) {
		}
	}

	public void TestCycle() {
		if (termination.containsCycle())
			JOptionPane.showMessageDialog(this, "Cycles have been detected!",
					"information", JOptionPane.OK_OPTION);
		else
			JOptionPane.showMessageDialog(this, "No cycles found",
					"information", JOptionPane.INFORMATION_MESSAGE);
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			this.graphChanged = true;
			this.RemoveSelectedItems();
		}
	}

	public void RemoveSelectedItems() {
		Object[] cells = GraphAuthor.graph.getSelectionCells();
		graph.getModel().remove(cells);
	}

	void AddExtraComponents() {
		model = new DefaultGraphModel(true);
		graph = new MyGraph(model);
		// added @David @11-10-2003
		termination = new Termination();
		termination.addCycleListener(new MyCycleListener());
		model.addGraphModelListener(new MyGraphListener());
		// end added @David
		new DropTarget(graph, DnDConstants.ACTION_COPY_OR_MOVE, this);
		graph.addKeyListener(this);

		graphScroll = new JScrollPane(graph);
		splConceptGraph.add(graphScroll, JSplitPane.RIGHT);

		for (Iterator i = AuthorSTATIC.CRTList.iterator(); i.hasNext();) {
			CRTConceptRelationType crel = (CRTConceptRelationType) i.next();
			RelationList.addItem(crel.name);
			this.visListData.add(crel.name);

			if (crel.properties.unary.booleanValue() == true) {
			}
		}
	}

	void NewAuthor(ActionEvent e, boolean confirmation) {
		int result = 0;

		if (confirmation && (this.graphChanged)) {
			result = JOptionPane
					.showConfirmDialog(
							this,
							"There is unsaved data. This action will erase all unsaved data \n Are you sure?",
							"alert", JOptionPane.YES_NO_OPTION);
		}

		if (result == 0) {
			this.graphChanged = false;

			Object[] cells = GraphAuthor.graph.getRoots();
			graph.getModel().remove(cells);
			conceptList.clear();

			spnConceptTree.getViewport().remove(ConceptTree);
			ConceptTree = new DragTree();
			this.projectName = "unnamed";
			this.initTree();
			spnConceptTree.getViewport().add(ConceptTree, null);
		}
	}

	public void valueChanged(TreeSelectionEvent event) {
		if (ConceptTree.getLastSelectedPathComponent() != null) {
			selection = ConceptTree.getLastSelectedPathComponent().toString()
					.trim();
		}
	}

	public void getConceptsFromTree(DefaultMutableTreeNode element) {
		for (Enumeration i = element.children(); i.hasMoreElements();) {
			DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) i
					.nextElement();
			this.treeConceptList.add(tnode.toString());
			this.getConceptsFromTree(tnode);
		}
	}

	// changed by @Bart @ 23-04-2003
	void editConceptEvent(ActionEvent e) {
		this.graphChanged = true;

		AHAOutConcept aout = null;
		AHAOutConcept aoutToEdit = null;

		String oldName = this.selection;
		try {
			oldName = oldName.trim();
		} catch (Exception te) {
		}

		// find description and resource information
		String oldResource = "";
		String oldDescription = "";
		String oldTemplate = "";
		boolean oldNoCommit = false;
		String oldStable = "";
		String oldStable_expr = "";
		String oldConcepttype = "";
		String oldTitle = "";

		for (Iterator i = this.conceptList.iterator(); i.hasNext();) {
			aout = (AHAOutConcept) i.next();

			if (aout.name.trim().equals(oldName.trim())) {
				aoutToEdit = aout;
				oldResource = aout.resource.trim();
				oldDescription = aout.description.trim();
				oldTemplate = aout.template.trim();
				oldNoCommit = aout.nocommit;
				oldStable = aout.stable;
				oldStable_expr = aout.stable_expr;
				oldConcepttype = aout.concepttype;
				oldTitle = aout.title;
			}
		}

		EditDialog editD = new EditDialog(mainFrame, oldName, oldDescription,
				oldResource, oldTemplate, oldNoCommit,
				showAdvanced.isSelected(), oldStable, oldStable_expr,
				oldConcepttype, oldTitle);
		editD.show();

		if (editD.cancelled) {
			return;
		}
		setresource = false;
		for (Iterator i = AuthorSTATIC.templateList.iterator(); i.hasNext();) {
			ConceptTemplate ctemp = (ConceptTemplate) i.next();
			if (ctemp.name.equals(editD.newTemplate)) {
				if (ctemp.hasresource.trim().toLowerCase().equals("true"))
					setresource = true;
				break;
			}
		}
		if ((editD.newResource.indexOf("http:") == -1)
				&& (editD.newResource.indexOf("file:") == -1) && setresource) {
			int result = JOptionPane.showConfirmDialog(this,
					"Invalid resource url \n Use 'http:/' or ''file:/'",
					"alert", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (editD.newResource.equals("file:/")) {
			int result = JOptionPane.showConfirmDialog(this,
					"Please, specify a correct resource url!", "alert",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}

		try {
			String tempName = editD.newConceptName.trim();
		} catch (Exception te) {
			// hmm new concept name must be null
		}

		if (this.inTree(editD.newConceptName.trim())
				&& (!oldName.equals(editD.newConceptName))) {
			JOptionPane.showMessageDialog(this,
					"Concept is not unique, rename is cancelled!",
					"information", JOptionPane.OK_OPTION);

			return;
		}

		DefaultMutableTreeNode selObj = (DefaultMutableTreeNode) this.ConceptTree
				.getLastSelectedPathComponent();

		// update tree
		if ((null == selObj) || (!(selObj instanceof DefaultMutableTreeNode))) {
			System.out.println("invalid selection for editing!");

			return;
		}

		try {
			selObj.setUserObject(editD.newConceptName.trim());
			((DefaultTreeModel) ConceptTree.getModel()).reload();
			this.EditConceptInGraph(oldName, editD.newConceptName.trim());
		} catch (Exception e1) {
			System.out.println("error during editing.");
		}

		// update conceptlist
		aoutToEdit.name = editD.newConceptName.trim();
		try {
			aoutToEdit.description = editD.newDescription.trim();
		} catch (Exception e1) {
			aoutToEdit.description = editD.newDescription;
		}
		try {
			aoutToEdit.resource = editD.newResource.trim();
		} catch (Exception e1) {
			aoutToEdit.resource = editD.newResource;
		}
		aoutToEdit.concepttype = editD.newconcepttype;
		if (aoutToEdit.concepttype != null)
			aoutToEdit.concepttype = aoutToEdit.concepttype.trim();
		aoutToEdit.title = editD.newtitle;
		if (aoutToEdit.title != null)
			aoutToEdit.title = aoutToEdit.title.trim();

		aoutToEdit.template = editD.newTemplate.trim();
		if (!aoutToEdit.template.equals(oldTemplate)) {
			// aoutToEdit.attributeList = new LinkedList();
			aoutToEdit.attributeList.clear();
			aoutToEdit.AddTemplateAttributes();
		}

		// added @David @11-11-2003
		termination.modelChanged();
		// end added @David @11-11-2003

		// added by @Bart @ 28-04-2003
		if (showAdvanced.isSelected()) {
			aoutToEdit.nocommit = editD.newNoCommit;
			aoutToEdit.stable = editD.newStable;
			aoutToEdit.stable_expr = editD.newStable_expr;
		} else {
			aoutToEdit.nocommit = oldNoCommit;
			aoutToEdit.stable = oldStable;
			aoutToEdit.stable_expr = oldStable_expr;
		}

		/*
		 * this.conceptList.remove(aoutToRemove); //removes the old concept
		 * 
		 * AHAOutConcept anew = new AHAOutConcept(); anew.name =
		 * editD.newConceptName.trim(); try { anew.description =
		 * editD.newDescription.trim(); } catch (Exception e1) {
		 * anew.description = editD.newDescription; } try { anew.resource =
		 * editD.newResource.trim(); } catch (Exception e1) { anew.resource =
		 * editD.newResource; } anew.template = editD.newTemplate.trim(); //
		 * added by @Bart @ 28-04-2003 if (showAdvanced.isSelected()) {
		 * anew.nocommit = editD.newNoCommit; } else { anew.nocommit =
		 * oldNoCommit; } // end added by @Bart this.conceptList.add(anew);
		 */

	}

	/**
	 * 
	 * @param e
	 *            click event added by @Bart @01-04-2003
	 */
	public void editAttributesEvent(ActionEvent e) {
		AHAOutConcept aout = null;
		AHAOutConcept concept = null;
		String oldName = this.selection;

		for (Iterator i = this.conceptList.iterator(); i.hasNext();) {
			aout = (AHAOutConcept) i.next();

			if (aout.name.trim().equals(selection.trim())) {
				// concept found
				concept = aout;
			}
		}
		Attributes attributeWindow = new Attributes(mainFrame, concept);
		attributeWindow.show();
	}

	/**
	 * 
	 * @param e
	 *            Added by Bart @ 07-04-2003
	 */
	public void assignResourcesEvent(ActionEvent e) {
		AHAOutConcept aout = null;
		AHAOutConcept concept = null;
		String oldName = this.selection;

		for (Iterator i = this.conceptList.iterator(); i.hasNext();) {
			aout = (AHAOutConcept) i.next();
			if (aout.name.trim().equals(selection.trim())) {
				// concept found
				concept = aout;
			}
		}
		if (concept != null) {
			ResourcesEditor resEditor = new ResourcesEditor(mainFrame, concept);
			resEditor.show();
		} else {
			System.out
					.println("GraphAuthor: assignResourcesEvent: no concept selected.");
		}
	}

	// added by @Bart @ 31-03-2003
	// checks or unchecks the "Advanced" menu entry
	private void setAdvanced(boolean selected) {
		showAdvanced.setSelected(selected);
		menuAdvAttributes.setVisible(selected);
	}

	public void EditConceptInGraph(String oldName, String newName) {
		Object[] cells = graph.getRoots();

		if (cells != null) {
			CellView[] views = GraphAuthor.graph.getView().getMapping(cells);

			for (int i = 0; i < views.length; i++) {
				if (views[i].getCell().getClass().getName()
						.equals("com.jgraph.graph.DefaultGraphCell")) {
					DefaultGraphCell dcell = (DefaultGraphCell) views[i]
							.getCell();

					if (dcell.toString().equals(oldName)) {
						dcell.setUserObject(newName);

						Map cellAtt = dcell.getAttributes();
						Map prop = dcell.getAttributes();
						Map propMap = new Hashtable();
						propMap.put(dcell, prop);
						graph.getModel().edit(null, propMap, null, null);
					}
				}
			}
		}
	}

	public void RemoveUnaryRelations() {
		this.graphChanged = true;

		Object cell = graph.getSelectionCell();

		if (cell != null) {
			if (cell.getClass().getName()
					.equals("com.jgraph.graph.DefaultGraphCell")) {
				DefaultGraphCell dcell = (DefaultGraphCell) cell;
				Map cellAtt = dcell.getAttributes();
				Map prop = dcell.getAttributes();
				Map propMap = new Hashtable();
				GraphConstants.setBackground(prop, Color.lightGray);
				propMap.put(dcell, prop);
				graph.getModel().edit(null, propMap, null, null);
			}
		}
	}

	public void AddUnaryRelations() {
		Object cell = graph.getSelectionCell();

		if (cell != null) {
			if (cell.getClass().getName()
					.equals("com.jgraph.graph.DefaultGraphCell")) {
				DefaultGraphCell dcell = (DefaultGraphCell) cell;
				Map cellAtt = dcell.getAttributes();
				Map prop = dcell.getAttributes();
				UnaryDialog uDialog = new UnaryDialog(this.mainFrame,
						(Hashtable) prop.get("unaryRelations"));
				uDialog.show();

				if (uDialog.cancelled == true) {
					return;
				}

				prop.put("unaryRelations", uDialog.unaryRel);

				Map propMap = new Hashtable();

				if (uDialog.unaryRel.isEmpty()) {
					GraphConstants.setBackground(prop, Color.lightGray);
				} else {
					GraphConstants.setBackground(prop, Color.red);
				}

				propMap.put(dcell, prop);
				graph.getModel().edit(null, propMap, null, null);
			}
		}
	}

	public void EditLabel() {
		Object cell = graph.getSelectionCell();

		if (cell != null) {
			if (cell.getClass().getName()
					.equals("com.jgraph.graph.DefaultGraphCell")) {
				DefaultGraphCell dcell = (DefaultGraphCell) cell;
				Map cellAtt = dcell.getAttributes();
				Map prop = dcell.getAttributes();
				Hashtable testcrt = (Hashtable) prop.get("unaryRelations");

				String oldlabel = (String) prop.get("ulabel");
				JOptionPane opane = new JOptionPane();
				String label = opane.showInputDialog("Please enter the label:",
						oldlabel);

				if (label == null) {
					label = oldlabel;
				}

				prop.put("ulabel", label);

				Map propMap = new Hashtable();
				GraphConstants.setBackground(prop, Color.red);
				propMap.put(dcell, prop);
				graph.getModel().edit(null, propMap, null, null);
			}
		}
	}

	//
	// PopupMenu
	//
	public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
		JPopupMenu menu = new JPopupMenu();

		if (!graph.isSelectionEmpty()) {
			// com.jgraph.graph.DefaultGraphCell
			if (graph.getSelectionCell().getClass().toString()
					.lastIndexOf("DefaultGraphCell") != -1) {
				// menu.addSeparator();
				menu.add(new AbstractAction("Unary Relations") {
					public void actionPerformed(ActionEvent e) {
						AddUnaryRelations();
					}
				});
			}
		}

		return menu;
	}

	public void deleteInGraph(String delConcept) {
		delConcept = delConcept.trim();

		LinkedList rcells = new LinkedList();
		Object[] cells = GraphAuthor.graph.getRoots();

		if (cells != null) {
			CellView[] views = GraphAuthor.graph.getView().getMapping(cells);

			for (int i = 0; i < views.length; i++) {
				if (views[i].getCell().getClass().getName()
						.equals("com.jgraph.graph.DefaultGraphCell")) {
					if (views[i].getCell().toString().trim()
							.equals(delConcept.trim())) {
						Object dcell = views[i].getCell();
						rcells.add(dcell);
					}
				}

				if (views[i].getCell().getClass().getName()
						.equals("com.jgraph.graph.DefaultEdge")) {
					DefaultEdge oEdge = (DefaultEdge) views[i].getCell();
					GraphModel model = GraphAuthor.graph.getModel();
					DefaultEdge dedge = (DefaultEdge) oEdge;
					Map edgeAtt = dedge.getAttributes();
					String source = "";
					String destination = "";

					if (model.getSource(oEdge) != null) {
						source = model.getParent(model.getSource(oEdge))
								.toString().trim();
					}

					if (model.getTarget(oEdge) != null) {
						destination = model.getParent(model.getTarget(oEdge))
								.toString().trim();
					}

					if (source.equals(delConcept)
							|| destination.equals(delConcept)) {
						Object dcell = views[i].getCell();
						rcells.add(dcell);
					}
				}
			}
		}

		graph.getModel().remove(rcells.toArray());
	}

	public void conceptTreeKeyEvent(KeyEvent e) {
		// removes the cut selection
		this.cutTreeNode = null;

		TreePath p = ConceptTree.getSelectionPath();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) p
				.getLastPathComponent();
		this.getConceptsFromTree(n);

		for (Iterator i = this.treeConceptList.iterator(); i.hasNext();) {
			String conceptn = (String) i.next();
			this.deleteInGraph(conceptn);

			// find de concept in the internal list and remove it
			AHAOutConcept delConcept = null;

			for (Iterator j = this.conceptList.iterator(); j.hasNext();) {
				AHAOutConcept removeConcept = (AHAOutConcept) j.next();

				if (removeConcept.name.equals(conceptn.toString().trim())) {
					delConcept = removeConcept;
				}
			}

			if (delConcept != null) {
				this.conceptList.remove(delConcept);

			}
		}

		this.deleteInGraph(n.toString());

		AHAOutConcept delConcept = null;

		for (Iterator j = this.conceptList.iterator(); j.hasNext();) {
			AHAOutConcept removeConcept = (AHAOutConcept) j.next();

			if (removeConcept.name.equals(n.toString().trim())) {
				delConcept = removeConcept;

			}
		}

		if (delConcept != null) {
			this.conceptList.remove(delConcept);

		}

		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) n.getParent();
		parent.remove(n);

		((DefaultTreeModel) ConceptTree.getModel()).reload();

		p = new TreePath(parent.getPath());
		ConceptTree.expandPath(p);
	}

	public boolean inTree(String conceptname) {
		boolean result = false;

		for (Iterator i = conceptList.iterator(); i.hasNext();) {
			AHAOutConcept concept = (AHAOutConcept) i.next();

			if (concept.name.equals(conceptname)) {
				result = true;
				return result;
			}
		}

		return false;
	}

	void AddConceptButton_actionPerformed(ActionEvent e) {
		TreePath p = null;
		AddConcept addconcept = new AddConcept(mainFrame,
				showAdvanced.isSelected());
		addconcept.show();

		if (addconcept.cancelled == false) {
			this.graphChanged = true;
			p = ConceptTree.getSelectionPath();

			for (Iterator i = addconcept.conceptlist.iterator(); i.hasNext();) {
				AHAOutConcept cout = (AHAOutConcept) i.next();
				if (this.inTree(cout.name) == false) {
					conceptList.add(cout); // adds concept to the internal
											// conceptlist

					// ConceptTree.add(cout.name);
					if (p != null) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) p
								.getLastPathComponent();
						n.add(new DefaultMutableTreeNode(cout.name));
					} else {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) ConceptTree
								.getModel().getRoot();
						DefaultMutableTreeNode testnode = new DefaultMutableTreeNode(
								"test");
						n.add(new DefaultMutableTreeNode(cout.name));
					}

					// ConceptList.setListData(listData);
				} else {
					JOptionPane o = new JOptionPane();
					o.showMessageDialog(this, "Concept: " + cout.name
							+ " already exists!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			if (p != null) {
				((DefaultTreeModel) ConceptTree.getModel()).reload();
				ConceptTree.setSelectionPath(p);
				ConceptTree.expandPath(p);
				// added by @Bart @ 13-05-2003
				// fixes bug that the tree model wasn't updated when a concept
				// was added
				// this happend when nothing was selected when that was done
			} else {
				((DefaultTreeModel) ConceptTree.getModel()).reload();
			}
			// end added by @Bart @ 13-05-2003
		}
	}

	public void addConcept(String conceptName, int x, int y) {
		int ongraph = 0;
		this.graphChanged = true;

		Object[] cells = GraphAuthor.graph.getRoots();

		if (cells != null) {
			CellView[] views = GraphAuthor.graph.getView().getMapping(cells);

			for (int i = 0; i < views.length; i++) {
				if (views[i].getCell().getClass().getName()
						.equals("com.jgraph.graph.DefaultGraphCell")) {
					if (views[i].getCell().toString().trim()
							.equals(conceptName.trim())) {
						ongraph = 1;

						DefaultGraphCell dGraph = (DefaultGraphCell) views[i]
								.getCell();
						graph.setSelectionCell(dGraph);
					}
				}
			}
		}

		if (ongraph == 0) {
			DefaultGraphCell vertex = new DefaultGraphCell(conceptName);
			vertex.add(new DefaultPort());

			// vertex.add(new DefaultPort());
			Point point = graph.snap(new Point(x, y));
			Dimension size = new Dimension(80, 20);
			Map map = GraphConstants.createMap();
			GraphConstants.setBounds(map, new Rectangle(point, size));
			GraphConstants.setBorderColor(map, Color.magenta);
			GraphConstants.setBackground(map, Color.lightGray);
			GraphConstants.setOpaque(map, true);
			GraphConstants.setEditable(map, false);
			GraphConstants.setAutoSize(map, true);
			GraphConstants.setSizeable(map, true);

			Hashtable attributes = new Hashtable();
			attributes.put(vertex, map);

			graph.getModel().insert(new Object[] { vertex }, null, null,
					attributes);
		}
	}

	/**
	 * Retrieves the style and color from the concept relation type.
	 * 
	 * @return ArrowStyle the color and style of the crt.
	 */
	public ArrowStyle getArrowStyle() {
		ArrowStyle aStyle = new ArrowStyle();
		aStyle.color = Color.black;

		String relationName = (String) RelationList.getSelectedItem();
		aStyle.crtname = relationName;

		for (Iterator i = AuthorSTATIC.CRTList.iterator(); i.hasNext();) {
			CRTConceptRelationType crel = (CRTConceptRelationType) i.next();

			if (relationName.equals(crel.name)) {
				if (crel.color.equals("black")) {
					aStyle.color = Color.black;
				}

				if (crel.color.equals("blue")) {
					aStyle.color = Color.blue;
				}

				if (crel.color.equals("cyan")) {
					aStyle.color = Color.cyan;
				}

				if (crel.color.equals("gray")) {
					aStyle.color = Color.gray;
				}

				if (crel.color.equals("green")) {
					aStyle.color = Color.green;
				}

				if (crel.color.equals("magenta")) {
					aStyle.color = Color.magenta;
				}

				if (crel.color.equals("red")) {
					aStyle.color = Color.red;
				}

				if (crel.color.equals("yellow")) {
					aStyle.color = Color.yellow;
				}

				if (crel.style.equals("full")) {
					aStyle.lineStyle[0] = 1;
					aStyle.lineStyle[1] = 0;
				}

				if (crel.style.equals("dashed")) {
					aStyle.lineStyle[0] = 5;
					aStyle.lineStyle[1] = 6;
				}

				if (crel.style.equals("dotted")) {
					aStyle.lineStyle[0] = 1;
					aStyle.lineStyle[1] = 1;
				}
			}
		}

		return aStyle;
	}

	// Insert a new Edge between source and target
	public void connect(Port source, Port target) {
		ArrowStyle aStyle;
		aStyle = this.getArrowStyle();

		// Connections that will be inserted into the Model
		ConnectionSet cs = new ConnectionSet();

		// Construct Edge with no label
		DefaultEdge edge = new DefaultEdge();

		// Create Connection between source and target using edge
		cs.connect(edge, source, target);

		// Create a Map thath holds the attributes for the edge
		Map map = GraphConstants.createMap();

		// GraphConstants.set
		GraphConstants.setLineColor(map, aStyle.color);
		GraphConstants.setDashPattern(map, aStyle.lineStyle);

		// custom properties
		map.put("crt", aStyle.crtname);

		// Add a Line End Attribute
		GraphConstants.setLineEnd(map, GraphConstants.SIMPLE);

		// Construct a Map from cells to Maps (for insert)
		Hashtable attributes = new Hashtable();

		// Associate the Edge with its Attributes
		attributes.put(edge, map);

		// Insert the Edge and its Attributes
		graph.getModel().insert(new Object[] { edge }, cs, null, attributes);
	}

	// Insert a new Vertex at point
	public void insert(Point point) {
		// Construct Vertex with no Label
		DefaultGraphCell vertex = new DefaultGraphCell();

		// Add one Floating Port
		vertex.add(new DefaultPort());

		// Snap the Point to the Grid
		point = graph.snap(new Point(point));

		// Default Size for the new Vertex
		Dimension size = new Dimension(25, 25);

		// Create a Map that holds the attributes for the Vertex
		Map map = GraphConstants.createMap();

		// Add a Bounds Attribute to the Map
		GraphConstants.setBounds(map, new Rectangle(point, size));

		// Add a Border Color Attribute to the Map
		GraphConstants.setBorderColor(map, Color.black);

		// Add a White Background
		GraphConstants.setBackground(map, Color.white);

		// Make Vertex Opaque
		GraphConstants.setOpaque(map, true);

		// Construct a Map from cells to Maps (for insert)
		Hashtable attributes = new Hashtable();

		// Associate the Vertex with its Attributes
		attributes.put(vertex, map);

		// Insert the Vertex and its Attributes
		graph.getModel()
				.insert(new Object[] { vertex }, null, null, attributes);
	}

	void RelationList_actionPerformed(ActionEvent e) {
		String relationName = ((String) RelationList.getSelectedItem()).trim();

		if (this.filteredListData.contains(relationName)) {
			this.filteredListData.removeElement(relationName);
			this.visListData.add(relationName);
			this.filterEdges();
		}
	}

	public void edgeGXL(JGraph graph, Object id, Object edge) {
		GraphModel model = graph.getModel();
		DefaultEdge dedge = (DefaultEdge) edge;
		Map edgeAtt = dedge.getAttributes();
	}

	public void testEdge() {
		Object[] cells = graph.getDescendants(graph.getRoots());
		int edges = 0;

		for (int i = 0; i < cells.length; i++)
			if (cells[i].getClass().getName()
					.equals("com.jgraph.graph.DefaultEdge")) {
				this.edgeGXL(graph, new Integer(edges++), cells[i]);
			}
	}

	public void getEdgeInfo(Object oEdge) {
		GraphModel model = graph.getModel();
		DefaultEdge dedge = (DefaultEdge) oEdge;
		Map edgeAtt = dedge.getAttributes();

		String svalue = (String) edgeAtt.get("crt");
	}

	/**
	 * Handles mouseclicks performed on the save button. The currently opened
	 * file is saved with the current name or with a filename entered by the
	 * user.
	 * 
	 * @param e
	 *            the event that triggered this action
	 * @param noOutput
	 *            output data to AHA! database
	 */
	private void save_Button_actionPerformed(ActionEvent e, boolean noOutput) {
		// sets the current dragtree model into a static/shared tree
		sharedConceptTree.setModel(ConceptTree.getModel());

		// FileSave saveDialog = new FileSave(this.home, mainFrame,
		// this.projectName );
		ServerFileChooser saveDialog = new ServerFileChooser(home,
				AuthorSTATIC.authorName, AMtc.AUTHOR_FILES_MODE, mainFrame,
				true);

		String oldProjectName = projectName;
		if (noOutput) {// use existing filename
			saveDialog.fileName = projectName;
		} else {// prompt user to enter filename
			String[] ff = { ".gaf" };
			saveDialog.showSaveDialog(ff);
		}

		String fileName = saveDialog.fileName;
		if (fileName == null)
			return;

		if (fileName.endsWith(".gaf"))
			fileName = fileName.trim().substring(0, fileName.length() - 4);

		// save file
		boolean exists = false;

		AHAOutConcept ahac = null;
		AHAOutConcept aproject = null;

		// added by Natalia Stash
		Vector conceptsvector = new Vector();
		String ahacresource = "";

		for (Iterator i = conceptList.iterator(); i.hasNext();) {// check for
																	// double
																	// resource
																	// usage
			ahac = (AHAOutConcept) i.next();
			// check name of root concept
			if (ahac.name.equals(oldProjectName))
				ahac.name = fileName;
			ahacresource = ahac.resource.trim();
			if (conceptsvector.contains(ahacresource)
					&& !ahacresource.equals("")) {
				exists = true;
				break;
			} else
				conceptsvector.add(ahacresource);
			// if (!oldProjectName.equals("unnamed") &&
			// ahac.name.trim().equals(oldProjectName)) {
			// ahac.name = this.projectName.trim();
			// break;
			// }
		}
		if (exists) {
			JOptionPane
					.showConfirmDialog(
							this,
							"There are 2 concepts with the same resource! The file is not saved!",
							"alert", JOptionPane.PLAIN_MESSAGE);
		} else {// no double resources, continue saving file
			graphChanged = false;
			projectName = fileName;

			if (oldProjectName.equals("unnamed")) {
				aproject = new AHAOutConcept();
				aproject.name = projectName;
				aproject.description = "";
				aproject.resource = "";
				aproject.concepttype = "abstract";
				aproject.template = "abstract concept";
				aproject.nocommit = false;
				aproject.stable = "";
				aproject.stable_expr = "";
				aproject.AddTemplateAttributes();
				conceptList.add(aproject);
			}
			DefaultMutableTreeNode oldRoot = (DefaultMutableTreeNode) ConceptTree
					.getModel().getRoot();
			String rootName = fileName.trim();
			oldRoot.setUserObject(rootName);

			((DefaultTreeModel) ConceptTree.getModel()).reload();
			AuthorOut aout = new AuthorOut(home, fileName);
			aout.WriteAuthorXML(noOutput);
			SaveToAHA saha = new SaveToAHA(home, noOutput);
		}
	}

	/**
	 * Handles mouseclicks performed on the load button. Requests the user to
	 * save unsaved data of the currently opened file. Depending on the users
	 * answer, the file is saved or not. A file open dialog box is shown where a
	 * user can select a file from it's authorfiles folder. The selected file is
	 * opened in the current view.
	 * 
	 * @param e
	 *            the event that triggered this action
	 */
	private void Load_Button_actionPerformed(ActionEvent e) {

		if (graphChanged) {// request user to save unsaved data
			int result = JOptionPane
					.showConfirmDialog(
							this,
							"There is unsaved data! \n Do you want to save these changes?",
							"alert", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				graphChanged = false;
				save_Button_actionPerformed(e, false);
			}

			if (result == JOptionPane.CANCEL_OPTION)
				return;
		}

		// show open file dialog, load selected file

		ServerFileChooser loadDialog = new ServerFileChooser(home,
				AuthorSTATIC.authorName, AMtc.AUTHOR_FILES_MODE, mainFrame,
				true);
		String[] ff = { ".gaf" };
		loadDialog.showOpenDialog(ff);
		// FileLoad loadDialog = new FileLoad(home, mainFrame);
		// loadDialog.show();
		String fileName = loadDialog.fileName;

		if (fileName != null) {// cancelled == false) {
			// added @David @14-10-2003
			termination.nocheck();
			// end added @David @14-10-2003

			NewAuthor(e, false);

			AuthorIn ain = new AuthorIn(home, fileName);
			ain.LoadFromServer();
			spnConceptTree.getViewport().remove(ConceptTree);

			// added by Natalia Stash
			String tmp = fileName.trim();
			if (tmp.endsWith(".gaf"))
				tmp = tmp.substring(0, tmp.length() - 4);
			projectName = tmp;
			// this.projectName = loadDialog.fileName.trim();

			ConceptTree = new DragTree();
			initTree();
			spnConceptTree.getViewport().add(ConceptTree, null);
			filterEdges();
			setAdvanced(false);

			// added @David @14-10-2003
			termination.check();
			termination.modelChanged();
			// end added @David @14-10-2003
		}
	}

	void exit_button_actionPerformed(ActionEvent e) {
		mainFrame.setVisible(false);
	}

	void Export_button_actionPerformed(ActionEvent e) {
		save_Button_actionPerformed(e, true);

		boolean created = false;
		try {

			URL url = new URL("http://" + home.getHost() + ":" + home.getPort()
					+ dirname + "/authorservlets/ExportFile?fileName="
					+ this.projectName + "&author=" + AuthorSTATIC.authorName);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String sFile = "";
			do {
				sFile = in.readLine();
				if (sFile.trim().equals("true")) {
					created = true;
				}
			} while (sFile != null);
			in.close();

		} catch (Exception ex) {
		}
		if (created) {
			JOptionPane.showConfirmDialog(this,
					"This project is successfully added to the AHA! database!",
					"alert", JOptionPane.PLAIN_MESSAGE);
		} else
			JOptionPane
					.showConfirmDialog(
							this,
							"There was an error adding this project to the AHA! database!\nPlease, check if you have entered all the data correctly!",
							"Error", JOptionPane.PLAIN_MESSAGE);
	}

	public void filterEdges() {
		Object[] cells = graph.getRoots();

		if (cells != null) {
			CellView[] views = GraphAuthor.graph.getView().getMapping(cells);

			for (int i = 0; i < views.length; i++) {
				if (views[i].getCell().getClass().getName()
						.equals("com.jgraph.graph.DefaultEdge")) {
					DefaultEdge dedge = (DefaultEdge) views[i].getCell();
					Map edgeAtt = dedge.getAttributes();
					String svalue = ((String) edgeAtt.get("crt")).trim();
					Map prop = dedge.getAttributes();
					GraphConstants.setVisible(prop, true);

					for (ListIterator j = this.filteredListData.listIterator(); j
							.hasNext();) {
						String listvalue = ((String) j.next()).trim();

						if (svalue.equals(listvalue)) {
							GraphConstants.setVisible(prop, false);
						}
					}

					Map propMap = new Hashtable();
					propMap.put(dedge, prop);
					graph.getModel().edit(null, propMap, null, null);
				}
			}
		}
	}

	void filterButton_actionPerformed(ActionEvent e) {
		Filters filterDialog = new Filters(mainFrame);
		filterDialog.show();

		if (filterDialog.cancelled == false) {
			this.filterEdges();
		}
	}

	public void drop(DropTargetDropEvent e) {
		String rootName = (String) this.ConceptTree.getModel().getRoot()
				.toString();
		/*
		 * if (this.ConceptTree.getSelectionPath().getPathCount() == 1) { String
		 * error = "The project name is not dragable.";
		 * javax.swing.JOptionPane.showMessageDialog(null, error, "Error",
		 * javax.swing.JOptionPane.ERROR_MESSAGE);
		 * 
		 * return; }
		 */
		int x = (int) (e.getLocation().x / graph.getScale());
		int y = (int) (e.getLocation().y / graph.getScale());
		addConcept(this.selection, x, y);
		cutTreeNode = null;
	}

	public void dragEnter(DropTargetDragEvent e) {
	}

	public void dragExit(DropTargetEvent e) {
	}

	public void dragOver(DropTargetDragEvent e) {
	}

	public void dropActionChanged(DropTargetDragEvent e) {
	}

	public static Hashtable getExecRequest(Hashtable reqinfo, URL home)
			throws IOException {
		String path = home.getPath();
		String pathttemp = path.substring(1, path.length());
		int index = pathttemp.indexOf("/");
		index++;

		String dirname = path.substring(0, index);

		if (dirname.equals("/graphAuthor")) {
			dirname = "";
		}

		URL url;
		try {
			url = new URL("http://" + home.getHost() + ":" + home.getPort()
					+ dirname + "/Exec");
		} catch (MalformedURLException e) {
			return new Hashtable();
		}
		URLConnection con = url.openConnection();
		con.setDoOutput(true);
		ObjectOutputStream oos = new ObjectOutputStream(con.getOutputStream());
		oos.writeObject(reqinfo);
		ObjectInputStream ios = new ObjectInputStream(con.getInputStream());
		try {
			Hashtable result = (Hashtable) ios.readObject();
			return result;
		} catch (ClassNotFoundException e) {
			return new Hashtable();
		}
	}

	class DragTree extends JTree implements DragGestureListener,
			DragSourceListener {
		public DragTree() {
			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(this, // component
																// where drag
																// originates
					DnDConstants.ACTION_COPY_OR_MOVE, // actions
					this);
		}

		public void dragGestureRecognized(DragGestureEvent e) {
			e.startDrag(DragSource.DefaultCopyDrop, // cursor
					new StringSelection("blackoak"), // transferable
					this); // drag source listener
		}

		public void dragDropEnd(DragSourceDropEvent e) {
		}

		public void dragEnter(DragSourceDragEvent e) {
		}

		public void dragExit(DragSourceEvent e) {
		}

		public void dragOver(DragSourceDragEvent e) {
		}

		public void dropActionChanged(DragSourceDragEvent e) {
		}
	}

	public class MyGraph extends JGraph {
		// Construct the Graph using the Model as its Data Source
		public MyGraph(GraphModel model) {
			super(model);

			// Use a Custom Marquee Handler
			setMarqueeHandler(new MyMarqueeHandler());

			// Tell the Graph to Select new Cells upon Insertion
			setSelectNewCells(true);

			// Make Ports Visible by Default
			setPortsVisible(true);

			// Use the Grid (but don't make it Visible)
			setGridEnabled(true);

			// Set the Grid Size to 10 Pixel
			setGridSize(6);

			// Set the Snap Size to 2 Pixel
			setSnapSize(1);

			// no need to press enter after edit
			setInvokesStopCellEditing(true);
		}

		/*
		 * overides processMouseEvent checks for negative coordinates changed by
		 * brendan: 23-04-2003
		 */
		protected void processMouseEvent(MouseEvent e) {
			if ((e.getPoint().x < 0) || (e.getPoint().y) < 0) {
				return;
			} else {
				super.processMouseEvent(e);
			}

		}

		// Override Superclass Method to Return Custom EdgeView
		protected EdgeView createEdgeView(Edge e, CellMapper cm) {
			// Return Custom EdgeView
			return new EdgeView(e, this, cm) {
				// Override Superclass Method
				public boolean isAddPointEvent(MouseEvent event) {
					// Points are Added using Shift-Click
					return event.isShiftDown();
				}

				// Override Superclass Method
				public boolean isRemovePointEvent(MouseEvent event) {
					// Points are Removed using Shift-Click
					return event.isShiftDown();
				}
			};
		}
	}

	// Custom MarqueeHandler
	// MarqueeHandler that Connects Vertices and Displays PopupMenus
	public class MyMarqueeHandler extends BasicMarqueeHandler {
		// Holds the Start and the Current Point
		protected Point start;

		// Holds the Start and the Current Point
		protected Point current;

		// Holds the First and the Current Port
		protected PortView port;

		// Holds the First and the Current Port
		protected PortView firstPort;

		// Override to Gain Control (for PopupMenu and ConnectMode)
		public boolean isForceMarqueeEvent(MouseEvent e) {
			// If Right Mouse Button we want to Display the PopupMenu
			if (SwingUtilities.isRightMouseButton(e)) {
				return true;
			}

			// Find and Remember Port
			port = getSourcePortAt(e.getPoint());

			// If Port Found and in ConnectMode (=Ports Visible)
			if ((port != null) && graph.isPortsVisible()) {
				return true;
			}

			// Else Call Superclass
			return super.isForceMarqueeEvent(e);
		}

		// Display PopupMenu or Remember Start Location and First Port
		public void mousePressed(final MouseEvent e) {
			// If Right Mouse Button
			if (SwingUtilities.isRightMouseButton(e)) {
				// Scale From Screen to Model
				Point loc = graph.fromScreen(e.getPoint());

				// Find Cell in Model Coordinates
				Object cell = graph.getFirstCellForLocation(loc.x, loc.y);

				// Create PopupMenu for the Cell
				JPopupMenu menu = createPopupMenu(e.getPoint(), cell);

				// Display PopupMenu
				menu.show(graph, e.getX(), e.getY());

				// Else if in ConnectMode and Remembered Port is Valid
			} else if ((port != null) && !e.isConsumed()
					&& graph.isPortsVisible()) {
				// Remember Start Location
				start = graph.toScreen(port.getLocation(null));

				// Remember First Port
				firstPort = port;

				// Consume Event
				e.consume();
			} else {
				// Call Superclass
				super.mousePressed(e);
			}
		}

		// Find Port under Mouse and Repaint Connector
		public void mouseDragged(MouseEvent e) {
			// If remembered Start Point is Valid
			if ((start != null) && !e.isConsumed()) {
				// Fetch Graphics from Graph
				Graphics g = graph.getGraphics();

				// Xor-Paint the old Connector (Hide old Connector)
				paintConnector(Color.black, graph.getBackground(), g);

				// Reset Remembered Port
				port = getTargetPortAt(e.getPoint());

				// If Port was found then Point to Port Location
				if (port != null) {
					current = graph.toScreen(port.getLocation(null));
				}
				// Else If no Port was found then Point to Mouse Location
				else {
					current = graph.snap(e.getPoint());
				}

				// Xor-Paint the new Connector
				paintConnector(graph.getBackground(), Color.black, g);

				// Consume Event
				e.consume();
			}

			// Call Superclass
			super.mouseDragged(e);
		}

		/*
		 * protected void prepareForUIInstall() { // Data member initializations
		 * stopEditingInCompleteEditing = true; preferredSize = new Dimension();
		 * setView(graph.getView()); setModel(graph.getModel()); }
		 */
		public PortView getSourcePortAt(Point point) {
			// Scale from Screen to Model
			Point tmp = graph.fromScreen(new Point(point));

			// Find a Port View in Model Coordinates and Remember
			return graph.getPortViewAt(tmp.x, tmp.y);
		}

		// Find a Cell at point and Return its first Port as a PortView
		protected PortView getTargetPortAt(Point point) {
			// Find Cell at point (No scaling needed here)
			Object cell = graph.getFirstCellForLocation(point.x, point.y);

			// Loop Children to find PortView
			for (int i = 0; i < graph.getModel().getChildCount(cell); i++) {
				// Get Child from Model
				Object tmp = graph.getModel().getChild(cell, i);

				// Get View for Child using the Graph's View as a Cell Mapper
				tmp = graph.getView().getMapping(tmp, false);

				// If Child View is a Port View and not equal to First Port
				if (tmp instanceof PortView && (tmp != firstPort)) {
					// Return as PortView
					return (PortView) tmp;
				}
			}

			// No Port View found
			return getSourcePortAt(point);
		}

		// Connect the First Port and the Current Port in the Graph or Repaint
		public void mouseReleased(MouseEvent e) {
			// If Valid Event, Current and First Port
			if ((e != null) && !e.isConsumed() && (port != null)
					&& (firstPort != null) && (firstPort != port)) {
				// Then Establish Connection
				connect((Port) firstPort.getCell(), (Port) port.getCell());

				// Consume Event
				e.consume();

				// Else Repaint the Graph
			} else {
				graph.repaint();
			}

			// Reset Global Vars
			firstPort = port = null;
			start = current = null;

			// Call Superclass
			super.mouseReleased(e);
		}

		// Show Special Cursor if Over Port
		public void mouseMoved(MouseEvent e) {
			// Check Mode and Find Port
			if ((e != null) && (getSourcePortAt(e.getPoint()) != null)
					&& !e.isConsumed() && graph.isPortsVisible()) {
				// Set Cusor on Graph (Automatically Reset)
				graph.setCursor(new Cursor(Cursor.HAND_CURSOR));

				// Consume Event
				e.consume();
			}

			// Call Superclass
			super.mouseReleased(e);
		}

		// Use Xor-Mode on Graphics to Paint Connector
		protected void paintConnector(Color fg, Color bg, Graphics g) {
			// Set Foreground
			g.setColor(fg);

			// Set Xor-Mode Color
			g.setXORMode(bg);

			// Highlight the Current Port
			paintPort(graph.getGraphics());

			// If Valid First Port, Start and Current Point
			if ((firstPort != null) && (start != null) && (current != null)) {
				// Then Draw A Line From Start to Current Point
				g.drawLine(start.x, start.y, current.x, current.y);
			}
		}

		// Use the Preview Flag to Draw a Highlighted Port
		protected void paintPort(Graphics g) {
			// If Current Port is Valid
			if (port != null) {
				// If Not Floating Port...
				boolean o = (GraphConstants.getOffset(port.getAttributes()) != null);

				// ...Then use Parent's Bounds
				Rectangle r = o ? port.getBounds() : port.getParentView()
						.getBounds();

				// Scale from Model to Screen
				r = graph.toScreen(new Rectangle(r));

				// Add Space For the Highlight Border
				r.setBounds(r.x - 3, r.y - 3, r.width + 6, r.height + 6);

				// Paint Port in Preview (=Highlight) Mode
				graph.getUI().paintCell(g, port, r, true);
			}
		}
	}

	// added @David @11-10-2003
	static boolean inevent = false;

	public class MyGraphListener implements GraphModelListener {
		public MyGraphListener() {
		}

		public void graphChanged(GraphModelEvent e) {
			if (inevent)
				return;
			inevent = true;
			DefaultGraphModel gmodel = (DefaultGraphModel) GraphAuthor.graph
					.getModel();
			GraphModelEvent.GraphModelChange gchange = e.getChange();
			boolean b = ((gchange.getInserted() != null)
					|| (gchange.getRemoved() != null) || (gchange.getChanged() != null));
			// added by @David @13-01-2005
			Object[] roots = GraphAuthor.graph.getRoots();
			Vector delroots = new Vector();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i] instanceof DefaultEdge) {
					DefaultEdge edge = (DefaultEdge) roots[i];
					boolean d = ((gmodel.getSource(edge) != null) && (gmodel
							.getTarget(edge) != null));
					if (d)
						d = ((gmodel.getParent(gmodel.getSource(edge)) != null) && (gmodel
								.getParent(gmodel.getTarget(edge)) != null));
					if (!d) {
						delroots.add(edge);
						System.out.println("Autodeleting edge: " + edge);
					}
				}
			}
			gmodel.remove(delroots.toArray());
			// end added by @David @13-01-2005
			if (b)
				termination.modelChanged();
			inevent = false;
		}
	}

	public class MyTreeModelListener implements TreeModelListener {
		public MyTreeModelListener() {
		}

		public void treeNodesChanged(TreeModelEvent e) {
			termination.modelChanged();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			termination.modelChanged();
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			termination.modelChanged();
		}

		public void treeStructureChanged(TreeModelEvent e) {
			termination.modelChanged();
		}
	}

	public class MyCycleListener implements TermCycleListener {
		public MyCycleListener() {
		}

		public void cycleStateChanged(boolean state) {
			if (state)
				TestCycle();
		}
	}
	// end added @david @11-10-2003
}