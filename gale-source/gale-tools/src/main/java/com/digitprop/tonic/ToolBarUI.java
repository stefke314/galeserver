package com.digitprop.tonic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicToolBarUI;

/**
 * UI delegate for JToolBars.
 * 
 * @author Markus Fischer
 * 
 *         <p>
 *         This software is under the <a
 *         href="http://www.gnu.org/copyleft/lesser.html" target="_blank">GNU
 *         Lesser General Public License</a>
 */

/*
 * ------------------------------------------------------------------------
 * Copyright (C) 2004 Markus Fischer
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2.1 as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * You can contact the author at: Markus Fischer www.digitprop.com
 * info@digitprop.com
 * ------------------------------------------------------------------------
 */
public class ToolBarUI extends BasicToolBarUI implements SwingConstants {
	protected JToolBar toolBar;
	private boolean floating;
	private int floatingX;
	private int floatingY;
	private JFrame floatingFrame;
	private RootPaneContainer floatingToolBar;
	protected DragWindow dragWindow;
	private Container dockingSource;
	private int dockingSensitivity = 0;
	protected int focusedCompIndex = -1;

	protected Color dockingColor = null;
	protected Color floatingColor = null;
	protected Color dockingBorderColor = null;
	protected Color floatingBorderColor = null;

	protected MouseInputListener dockingListener;
	protected PropertyChangeListener propertyListener;

	protected ContainerListener toolBarContListener;
	protected FocusListener toolBarFocusListener;

	protected String constraintBeforeFloating = BorderLayout.NORTH;

	// Rollover button implementation.
	private static String IS_ROLLOVER = "JToolBar.isRollover";
	private static Border rolloverBorder;
	private static Border nonRolloverBorder;
	private static Border nonRolloverToggleBorder;
	private boolean rolloverBorders = true;

	private HashMap borderTable = new HashMap();
	private Hashtable rolloverTable = new Hashtable();

	/**
	 * As of Java 2 platform v1.3 this previously undocumented field is no
	 * longer used. Key bindings are now defined by the LookAndFeel, please
	 * refer to the key bindings specification for further details.
	 * 
	 * @deprecated As of Java 2 platform v1.3.
	 */
	protected KeyStroke upKey;
	/**
	 * As of Java 2 platform v1.3 this previously undocumented field is no
	 * longer used. Key bindings are now defined by the LookAndFeel, please
	 * refer to the key bindings specification for further details.
	 * 
	 * @deprecated As of Java 2 platform v1.3.
	 */
	protected KeyStroke downKey;
	/**
	 * As of Java 2 platform v1.3 this previously undocumented field is no
	 * longer used. Key bindings are now defined by the LookAndFeel, please
	 * refer to the key bindings specification for further details.
	 * 
	 * @deprecated As of Java 2 platform v1.3.
	 */
	protected KeyStroke leftKey;
	/**
	 * As of Java 2 platform v1.3 this previously undocumented field is no
	 * longer used. Key bindings are now defined by the LookAndFeel, please
	 * refer to the key bindings specification for further details.
	 * 
	 * @deprecated As of Java 2 platform v1.3.
	 */
	protected KeyStroke rightKey;

	private static String FOCUSED_COMP_INDEX = "JToolBar.focusedCompIndex";

	public static ComponentUI createUI(JComponent c) {
		return new ToolBarUI();
	}

	public void installUI(JComponent c) {
		toolBar = (JToolBar) c;

		if (toolBar.getOrientation() == JToolBar.HORIZONTAL)
			toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		else
			toolBar.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0,
					0));

		// Set defaults
		installDefaults();
		installComponents();
		installListeners();
		installKeyboardActions();

		// Initialize instance vars
		dockingSensitivity = 0;
		floating = false;
		floatingX = floatingY = 0;
		floatingToolBar = null;

		setOrientation(toolBar.getOrientation());
		c.setOpaque(true);

		if (c.getClientProperty(FOCUSED_COMP_INDEX) != null) {
			focusedCompIndex = ((Integer) (c
					.getClientProperty(FOCUSED_COMP_INDEX))).intValue();
		}

		// Set the borders of contained buttons
		for (int i = 0; i < toolBar.getComponentCount(); i++) {
			Component child = toolBar.getComponent(i);

			if (toolBarFocusListener != null) {
				child.addFocusListener(toolBarFocusListener);
			}

			setButtonSize(child);

			if (isRolloverBorders()) {
				setBorderToRollover(child);
			} else {
				setBorderToNonRollover(child);
			}
		}
	}

	public void uninstallUI(JComponent c) {

		// Clear defaults
		uninstallDefaults();
		uninstallComponents();
		uninstallListeners();
		uninstallKeyboardActions();

		// Clear instance vars
		if (isFloating() == true)
			setFloating(false, null);

		floatingToolBar = null;
		dragWindow = null;
		dockingSource = null;

		c.putClientProperty(FOCUSED_COMP_INDEX, new Integer(focusedCompIndex));
	}

	protected void setOrientedBorder() {
		if (toolBar.getOrientation() == JToolBar.HORIZONTAL)
			LookAndFeel.installBorder(toolBar, "ToolBar.border");
		else
			LookAndFeel.installBorder(toolBar, "ToolBar.verticalBorder");
	}

	protected void installDefaults() {
		setOrientedBorder();
		LookAndFeel.installColorsAndFont(toolBar, "ToolBar.background",
				"ToolBar.foreground", "ToolBar.font");
		// Toolbar specific defaults
		if (dockingColor == null || dockingColor instanceof UIResource)
			dockingColor = UIManager.getColor("ToolBar.dockingBackground");
		if (floatingColor == null || floatingColor instanceof UIResource)
			floatingColor = UIManager.getColor("ToolBar.floatingBackground");
		if (dockingBorderColor == null
				|| dockingBorderColor instanceof UIResource)
			dockingBorderColor = UIManager
					.getColor("ToolBar.dockingForeground");
		if (floatingBorderColor == null
				|| floatingBorderColor instanceof UIResource)
			floatingBorderColor = UIManager
					.getColor("ToolBar.floatingForeground");

		// ToolBar rollover button borders
		Object rolloverProp = toolBar.getClientProperty(IS_ROLLOVER);
		if (rolloverProp != null) {
			rolloverBorders = ((Boolean) rolloverProp).booleanValue();
		}

		if (rolloverBorder == null) {
			rolloverBorder = createRolloverBorder();
		}
		if (nonRolloverBorder == null) {
			nonRolloverBorder = createNonRolloverBorder();
		}
		if (nonRolloverToggleBorder == null) {
			nonRolloverToggleBorder = createNonRolloverToggleBorder();
		}

		setRolloverBorders(isRolloverBorders());
	}

	protected void uninstallDefaults() {
		LookAndFeel.uninstallBorder(toolBar);
		dockingColor = null;
		floatingColor = null;
		dockingBorderColor = null;
		floatingBorderColor = null;

		installNormalBorders(toolBar);

		rolloverBorder = null;
		nonRolloverBorder = null;
		nonRolloverToggleBorder = null;
	}

	protected void installComponents() {
	}

	protected void uninstallComponents() {
	}

	protected void installListeners() {
		dockingListener = createDockingListener();

		if (dockingListener != null) {
			toolBar.addMouseMotionListener(dockingListener);
			toolBar.addMouseListener(dockingListener);
		}

		propertyListener = createPropertyListener(); // added in setFloating
		if (propertyListener != null) {
			toolBar.addPropertyChangeListener(propertyListener);
		}

		toolBarContListener = createToolBarContListener();
		if (toolBarContListener != null) {
			toolBar.addContainerListener(toolBarContListener);
		}

		toolBarFocusListener = createToolBarFocusListener();

		if (toolBarFocusListener != null) {
			// Put focus listener on all components in toolbar
			Component[] components = toolBar.getComponents();

			for (int i = 0; i < components.length; ++i) {
				components[i].addFocusListener(toolBarFocusListener);
			}
		}
	}

	protected void uninstallListeners() {
		if (dockingListener != null) {
			toolBar.removeMouseMotionListener(dockingListener);
			toolBar.removeMouseListener(dockingListener);

			dockingListener = null;
		}

		if (propertyListener != null) {
			toolBar.removePropertyChangeListener(propertyListener);
			propertyListener = null; // removed in setFloating
		}

		if (toolBarContListener != null) {
			toolBar.removeContainerListener(toolBarContListener);
			toolBarContListener = null;
		}

		if (toolBarFocusListener != null) {
			// Remove focus listener from all components in toolbar
			Component[] components = toolBar.getComponents();

			for (int i = 0; i < components.length; ++i) {
				components[i].removeFocusListener(toolBarFocusListener);
			}

			toolBarFocusListener = null;
		}
	}

	protected void installKeyboardActions() {
		InputMap km = getMyInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		SwingUtilities.replaceUIInputMap(toolBar,
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
		ActionMap am = getMyActionMap();

		if (am != null) {
			SwingUtilities.replaceUIActionMap(toolBar, am);
		}
	}

	InputMap getMyInputMap(int condition) {
		if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
			return (InputMap) UIManager.get("ToolBar.ancestorInputMap");
		}
		return null;
	}

	ActionMap getMyActionMap() {
		ActionMap map = (ActionMap) UIManager.get("ToolBar.actionMap");

		if (map == null) {
			map = createMyActionMap();
			if (map != null) {
				UIManager.getLookAndFeelDefaults()
						.put("ToolBar.actionMap", map);
			}
		}
		return map;
	}

	ActionMap createMyActionMap() {
		ActionMap map = new ActionMapUIResource();
		map.put("navigateRight", new RightAction());
		map.put("navigateLeft", new LeftAction());
		map.put("navigateUp", new UpAction());
		map.put("navigateDown", new DownAction());
		return map;
	}

	protected void uninstallKeyboardActions() {
		SwingUtilities.replaceUIActionMap(toolBar, null);
		SwingUtilities.replaceUIInputMap(toolBar,
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
	}

	protected void navigateFocusedComp(int direction) {
		int nComp = toolBar.getComponentCount();
		int j;

		switch (direction) {
		case EAST:
		case SOUTH:

			if (focusedCompIndex < 0 || focusedCompIndex >= nComp)
				break;

			j = focusedCompIndex + 1;

			while (j != focusedCompIndex) {
				if (j >= nComp)
					j = 0;
				Component comp = toolBar.getComponentAtIndex(j++);

				if (comp != null && comp.isFocusTraversable()) {
					comp.requestFocus();
					break;
				}
			}

			break;

		case WEST:
		case NORTH:

			if (focusedCompIndex < 0 || focusedCompIndex >= nComp)
				break;

			j = focusedCompIndex - 1;

			while (j != focusedCompIndex) {
				if (j < 0)
					j = nComp - 1;
				Component comp = toolBar.getComponentAtIndex(j--);

				if (comp != null && comp.isFocusTraversable()) {
					comp.requestFocus();
					break;
				}
			}

			break;

		default:
			break;
		}
	}

	/**
	 * Creates a rollover border for toolbar components. The rollover border
	 * will be installed if rollover borders are enabled.
	 * <p>
	 * Override this method to provide an alternate rollover border.
	 * 
	 * @since 1.4
	 */
	protected Border createRolloverBorder() {
		return new ToolBarBorder(); // BorderFactory.createEmptyBorder(0, 0, 0,
									// 0);
		// UIDefaults table= UIManager.getLookAndFeelDefaults();
		// return new CompoundBorder(
		// new BasicBorders.RolloverButtonBorder(
		// table.getColor("controlShadow"),
		// table.getColor("controlDkShadow"),
		// table.getColor("controlHighlight"),
		// table.getColor("controlLtHighlight")),
		// new ToolBarMarginBorder());
	}

	/**
	 * Creates the non rollover border for toolbar components. This border will
	 * be installed as the border for components added to the toolbar if
	 * rollover borders are not enabled.
	 * <p>
	 * Override this method to provide an alternate rollover border.
	 * 
	 * @since 1.4
	 */
	protected Border createNonRolloverBorder() {
		return new ToolBarBorder(); // BorderFactory.createEmptyBorder(0, 0, 0,
									// 0);
		// UIDefaults table= UIManager.getLookAndFeelDefaults();
		// return new CompoundBorder(
		// new BasicBorders.ButtonBorder(
		// table.getColor("Button.shadow"),
		// table.getColor("Button.darkShadow"),
		// table.getColor("Button.light"),
		// table.getColor("Button.highlight")),
		// new ToolBarMarginBorder());
	}

	/**
	 * Creates a non rollover border for Toggle buttons in the toolbar.
	 */
	private Border createNonRolloverToggleBorder() {
		return new ToolBarBorder(); // BorderFactory.createEmptyBorder(0, 0, 0,
									// 0);

		// UIDefaults table= UIManager.getLookAndFeelDefaults();
		// return new CompoundBorder(
		// new BasicBorders.RadioButtonBorder(
		// table.getColor("ToggleButton.shadow"),
		// table.getColor("ToggleButton.darkShadow"),
		// table.getColor("ToggleButton.light"),
		// table.getColor("ToggleButton.highlight")),
		// new ToolBarMarginBorder());
	}

	/**
	 * No longer used, use BasicToolBarUI.createFloatingWindow(JToolBar)
	 * 
	 * @see #createFloatingWindow
	 */
	protected JFrame createFloatingFrame(JToolBar toolbar) {
		Window window = SwingUtilities.getWindowAncestor(toolbar);
		JFrame frame = new JFrame(toolbar.getName(),
				(window != null) ? window.getGraphicsConfiguration() : null) {
			// Override createRootPane() to automatically resize
			// the frame when contents change
			protected JRootPane createRootPane() {
				JRootPane rootPane = new JRootPane() {
					private boolean packing = false;

					public void validate() {
						super.validate();
						if (!packing) {
							packing = true;
							pack();
							packing = false;
						}
					}
				};
				rootPane.setOpaque(true);
				return rootPane;
			}
		};
		frame.setResizable(false);
		WindowListener wl = createFrameListener();
		frame.addWindowListener(wl);
		return frame;
	}

	/**
	 * Creates a window which contains the toolbar after it has been dragged out
	 * from its container
	 * 
	 * @returns a <code>RootPaneContainer</code> object, containing the toolbar.
	 */
	protected RootPaneContainer createFloatingWindow(JToolBar toolbar) {
		class ToolBarDialog extends JDialog {
			public ToolBarDialog(Frame owner, String title, boolean modal) {
				super(owner, title, modal);
			}

			public ToolBarDialog(Dialog owner, String title, boolean modal) {
				super(owner, title, modal);
			}

			// Override createRootPane() to automatically resize
			// the frame when contents change
			protected JRootPane createRootPane() {
				JRootPane rootPane = new JRootPane() {
					private boolean packing = false;

					public void validate() {
						super.validate();
						if (!packing) {
							packing = true;
							pack();
							packing = false;
						}
					}
				};
				rootPane.setOpaque(true);
				return rootPane;
			}
		}

		JDialog dialog;
		Window window = SwingUtilities.getWindowAncestor(toolbar);
		if (window instanceof Frame) {
			dialog = new ToolBarDialog((Frame) window, toolbar.getName(), false);
		} else if (window instanceof Dialog) {
			dialog = new ToolBarDialog((Dialog) window, toolbar.getName(),
					false);
		} else {
			dialog = new ToolBarDialog((Frame) null, toolbar.getName(), false);
		}

		dialog.setTitle(toolbar.getName());
		dialog.setResizable(false);
		WindowListener wl = createFrameListener();
		dialog.addWindowListener(wl);
		return dialog;
	}

	protected DragWindow createMyDragWindow(JToolBar toolbar) {
		Window frame = null;
		if (toolBar != null) {
			Container p;
			for (p = toolBar.getParent(); p != null && !(p instanceof Window); p = p
					.getParent())
				;
			if (p != null && p instanceof Window)
				frame = (Window) p;
		}
		if (floatingToolBar == null) {
			floatingToolBar = createFloatingWindow(toolBar);
		}
		if (floatingToolBar instanceof Window)
			frame = (Window) floatingToolBar;
		DragWindow dragWindow = new DragWindow(frame);
		return dragWindow;
	}

	/**
	 * Returns a flag to determine whether rollover button borders are enabled.
	 * 
	 * @return true if rollover borders are enabled; false otherwise
	 * @see #setRolloverBorders
	 * @since 1.4
	 */
	public boolean isRolloverBorders() {
		return rolloverBorders;
	}

	/**
	 * Sets the flag for enabling rollover borders on the toolbar and it will
	 * also install the apropriate border depending on the state of the flag.
	 * 
	 * @param rollover
	 *            if true, rollover borders are installed. Otherwise
	 *            non-rollover borders are installed
	 * @see #isRolloverBorders
	 * @since 1.4
	 */
	public void setRolloverBorders(boolean rollover) {
		rolloverBorders = rollover;

		if (rolloverBorders) {
			installRolloverBorders(toolBar);
		} else {
			installNonRolloverBorders(toolBar);
		}
	}

	/**
	 * Installs rollover borders on all the child components of the JComponent.
	 * <p>
	 * This is a convenience method to call <code>setBorderToRollover</code> for
	 * each child component.
	 * 
	 * @param c
	 *            container which holds the child components (usally a JToolBar)
	 * @see #setBorderToRollover
	 * @since 1.4
	 */
	protected void installRolloverBorders(JComponent c) {
		// Put rollover borders on buttons
		Component[] components = c.getComponents();

		for (int i = 0; i < components.length; ++i) {
			if (components[i] instanceof JComponent) {
				((JComponent) components[i]).updateUI();
				setBorderToRollover(components[i]);
			}
		}
	}

	/**
	 * Installs non-rollover borders on all the child components of the
	 * JComponent. A non-rollover border is the border that is installed on the
	 * child component while it is in the toolbar.
	 * <p>
	 * This is a convenience method to call <code>setBorderToNonRollover</code>
	 * for each child component.
	 * 
	 * @param c
	 *            container which holds the child components (usally a JToolBar)
	 * @see #setBorderToNonRollover
	 * @since 1.4
	 */
	protected void installNonRolloverBorders(JComponent c) {
		// Put non-rollover borders on buttons. These borders reduce the margin.
		Component[] components = c.getComponents();

		for (int i = 0; i < components.length; ++i) {
			if (components[i] instanceof JComponent) {
				((JComponent) components[i]).updateUI();
				setBorderToNonRollover(components[i]);
			}
		}
	}

	/**
	 * Installs normal borders on all the child components of the JComponent. A
	 * normal border is the original border that was installed on the child
	 * component before it was added to the toolbar.
	 * <p>
	 * This is a convenience method to call <code>setBorderNormal</code> for
	 * each child component.
	 * 
	 * @param c
	 *            container which holds the child components (usally a JToolBar)
	 * @see #setBorderToNonRollover
	 * @since 1.4
	 */
	protected void installNormalBorders(JComponent c) {
		// Put back the normal borders on buttons
		Component[] components = c.getComponents();

		for (int i = 0; i < components.length; ++i) {
			setBorderToNormal(components[i]);
		}
	}

	/**
	 * Sets the border of the component to have a rollover border which was
	 * created by <code>createRolloverBorder</code>.
	 * 
	 * @param c
	 *            component which will have a rollover border installed
	 * @see #createRolloverBorder
	 * @since 1.4
	 */
	protected void setBorderToRollover(Component c) {
		if (c instanceof AbstractButton) {
			AbstractButton b = (AbstractButton) c;

			Border border = (Border) borderTable.get(b);
			if (border == null || border instanceof UIResource) {
				borderTable.put(b, b.getBorder());
			}
			// Don't set a border if the existing border is null
			if (b.getBorder() != null) {
				b.setBorder(rolloverBorder);
			}

			rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE
					: Boolean.FALSE);
			b.setRolloverEnabled(true);
		}
	}

	/**
	 * Sets the border of the component to have a non-rollover border which was
	 * created by <code>createNonRolloverBorder</code>.
	 * 
	 * @param c
	 *            component which will have a non-rollover border installed
	 * @see #createNonRolloverBorder
	 * @since 1.4
	 */
	protected void setBorderToNonRollover(Component c) {
		if (c instanceof AbstractButton) {
			AbstractButton b = (AbstractButton) c;

			Border border = (Border) borderTable.get(b);
			if (border == null || border instanceof UIResource) {
				borderTable.put(b, b.getBorder());
			}
			if (b.getBorder() != null) {
				if (b instanceof JToggleButton) {
					((JToggleButton) b).setBorder(nonRolloverToggleBorder);
				} else {
					// Don't set a border if the existing border is null
					b.setBorder(nonRolloverBorder);
				}
			}
			rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE
					: Boolean.FALSE);
			b.setRolloverEnabled(false);
		}
	}

	/**
	 * Sets the border of the component to have a normal border. A normal border
	 * is the original border that was installed on the child component before
	 * it was added to the toolbar.
	 * 
	 * @param c
	 *            component which will have a normal border re-installed
	 * @see #createNonRolloverBorder
	 * @since 1.4
	 */
	protected void setBorderToNormal(Component c) {
		if (c instanceof AbstractButton) {
			AbstractButton b = (AbstractButton) c;

			Border border = (Border) borderTable.remove(b);
			b.setBorder(border);

			Boolean value = (Boolean) rolloverTable.remove(b);
			if (value != null) {
				b.setRolloverEnabled(value.booleanValue());
			}
		}
	}

	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	public Dimension getPreferredSize(JComponent c) {
		return null;
	}

	public Dimension getMaximumSize(JComponent c) {
		return getPreferredSize(c);
	}

	// public void paint(Graphics g, JComponent c)
	// {
	// super.paint(g, c);
	//
	// g.setColor(UIManager.getColor("Button.borderColor"));
	//
	// switch(toolBar.getOrientation())
	// {
	// case WEST:
	// break;
	//
	// case NORTH:
	// g.setColor(Color.WHITE);
	// g.drawLine(0, c.getHeight()-2, c.getWidth()-1, c.getHeight()-2);
	// g.setColor(UIManager.getColor("Button.borderColor"));
	// g.drawLine(0, c.getHeight()-1, c.getWidth()-1, c.getHeight()-1);
	// break;
	//
	// case SOUTH:
	// case EAST:
	// }
	// }

	public void setFloatingLocation(int x, int y) {
		floatingX = x;
		floatingY = y;
	}

	public boolean isFloating() {
		return floating;
	}

	public void setFloating(boolean b, Point p) {
		if (toolBar.isFloatable() == true) {
			if (dragWindow != null)
				dragWindow.setVisible(false);
			this.floating = b;
			if (b == true) {
				if (dockingSource == null) {
					dockingSource = toolBar.getParent();
					dockingSource.remove(toolBar);
				}
				Point l = new Point();
				toolBar.getLocation(l);
				constraintBeforeFloating = calculateConstraint(dockingSource, l);
				if (propertyListener != null)
					UIManager.addPropertyChangeListener(propertyListener);
				if (floatingToolBar == null)
					floatingToolBar = createFloatingWindow(toolBar);
				floatingToolBar.getContentPane().add(toolBar,
						BorderLayout.CENTER);
				setOrientation(JToolBar.HORIZONTAL);
				if (floatingToolBar instanceof Window)
					((Window) floatingToolBar).pack();
				if (floatingToolBar instanceof Window)
					((Window) floatingToolBar)
							.setLocation(floatingX, floatingY);
				if (floatingToolBar instanceof Window)
					((Window) floatingToolBar).show();
			} else {
				if (floatingToolBar == null)
					floatingToolBar = createFloatingWindow(toolBar);
				if (floatingToolBar instanceof Window)
					((Window) floatingToolBar).setVisible(false);
				floatingToolBar.getContentPane().remove(toolBar);
				String constraint = getDockingConstraint(dockingSource, p);
				int orientation = mapConstraintToOrientation(constraint);
				setOrientation(orientation);
				if (dockingSource == null)
					dockingSource = toolBar.getParent();
				if (propertyListener != null)
					UIManager.removePropertyChangeListener(propertyListener);
				dockingSource.add(constraint, toolBar);
			}
			dockingSource.invalidate();
			Container dockingSourceParent = dockingSource.getParent();
			if (dockingSourceParent != null)
				dockingSourceParent.validate();
			dockingSource.repaint();
		}
	}

	private int mapConstraintToOrientation(String constraint) {
		int orientation = toolBar.getOrientation();

		if (constraint != null) {
			if (constraint.equals(BorderLayout.EAST)
					|| constraint.equals(BorderLayout.WEST))
				orientation = JToolBar.VERTICAL;
			else if (constraint.equals(BorderLayout.NORTH)
					|| constraint.equals(BorderLayout.SOUTH))
				orientation = JToolBar.HORIZONTAL;
		}

		return orientation;
	}

	public void setOrientation(int orientation) {
		toolBar.setOrientation(orientation);

		if (toolBar.getOrientation() == JToolBar.HORIZONTAL)
			toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		else
			toolBar.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0,
					0));

		if (dragWindow != null)
			dragWindow.setOrientation(orientation);
	}

	/**
	 * Gets the color displayed when over a docking area
	 */
	public Color getDockingColor() {
		return dockingColor;
	}

	/**
	 * Sets the color displayed when over a docking area
	 */
	public void setDockingColor(Color c) {
		this.dockingColor = c;
	}

	/**
	 * Gets the color displayed when over a floating area
	 */
	public Color getFloatingColor() {
		return floatingColor;
	}

	/**
	 * Sets the color displayed when over a floating area
	 */
	public void setFloatingColor(Color c) {
		this.floatingColor = c;
	}

	public boolean canDock(Component c, Point p) {
		boolean b = false;
		if (c.contains(p)) {
			dockingSensitivity = (toolBar.getOrientation() == JToolBar.HORIZONTAL) ? toolBar
					.getSize().height : toolBar.getSize().width;
			// North
			if (p.y < dockingSensitivity)
				b = true;
			// South
			if (p.y > c.getSize().height - dockingSensitivity)
				b = true;
			// West (Base distance on height for now!)
			if (p.x < dockingSensitivity)
				b = true;
			// East (Base distance on height for now!)
			if (p.x > c.getSize().width - dockingSensitivity)
				b = true;
		}
		return b;
	}

	private String calculateConstraint(Component c, Point p) {
		if (p == null)
			return constraintBeforeFloating;
		String s = BorderLayout.NORTH;
		if (c.contains(p)) {
			dockingSensitivity = (toolBar.getOrientation() == JToolBar.HORIZONTAL) ? toolBar
					.getSize().height : toolBar.getSize().width;
			if (p.y >= dockingSource.getSize().height - dockingSensitivity)
				s = BorderLayout.SOUTH;
			// West (Base distance on height for now!)
			else if (p.x < dockingSensitivity
					&& (toolBar.getOrientation() == JToolBar.VERTICAL))
				s = BorderLayout.WEST;
			// East (Base distance on height for now!)
			else if (p.x >= dockingSource.getSize().width - dockingSensitivity
					&& (toolBar.getOrientation() == JToolBar.VERTICAL))
				s = BorderLayout.EAST;
			// North (Base distance on height for now!)
			else if (p.y < dockingSensitivity)
				s = BorderLayout.NORTH;
		}
		return s;
	}

	private String getDockingConstraint(Component c, Point p) {
		if (p == null)
			return constraintBeforeFloating;
		String s = BorderLayout.NORTH;
		if (c.contains(p)) {
			dockingSensitivity = (toolBar.getOrientation() == JToolBar.HORIZONTAL) ? toolBar
					.getSize().height : toolBar.getSize().width;
			if (p.y >= dockingSource.getSize().height - dockingSensitivity)
				s = BorderLayout.SOUTH;
			// West (Base distance on height for now!)
			if (p.x < dockingSensitivity)
				s = BorderLayout.WEST;
			// East (Base distance on height for now!)
			if (p.x >= dockingSource.getSize().width - dockingSensitivity)
				s = BorderLayout.EAST;
			// North (Base distance on height for now!)
			if (p.y < dockingSensitivity)
				s = BorderLayout.NORTH;
		}
		return s;
	}

	protected void dragTo(Point position, Point origin) {
		if (toolBar.isFloatable() == true) {
			try {
				if (dragWindow == null)
					dragWindow = createMyDragWindow(toolBar);
				Point offset = dragWindow.getOffset();
				if (offset == null) {
					Dimension size = toolBar.getPreferredSize();
					offset = new Point(size.width / 2, size.height / 2);
					dragWindow.setOffset(offset);
				}
				Point global = new Point(origin.x + position.x, origin.y
						+ position.y);
				Point dragPoint = new Point(global.x - offset.x, global.y
						- offset.y);
				if (dockingSource == null)
					dockingSource = toolBar.getParent();

				Point p = new Point(origin);
				SwingUtilities.convertPointFromScreen(p, toolBar.getParent());
				constraintBeforeFloating = calculateConstraint(dockingSource, p);
				Point dockingPosition = dockingSource.getLocationOnScreen();
				Point comparisonPoint = new Point(global.x - dockingPosition.x,
						global.y - dockingPosition.y);
				if (canDock(dockingSource, comparisonPoint)) {
					dragWindow.setBackground(getDockingColor());
					String constraint = getDockingConstraint(dockingSource,
							comparisonPoint);
					int orientation = mapConstraintToOrientation(constraint);
					dragWindow.setOrientation(orientation);
					dragWindow.setBorderColor(dockingBorderColor);
				} else {
					dragWindow.setBackground(getFloatingColor());
					dragWindow.setOrientation(JToolBar.HORIZONTAL);
					dragWindow.setBorderColor(floatingBorderColor);
				}

				dragWindow.setLocation(dragPoint.x, dragPoint.y);
				if (dragWindow.isVisible() == false) {
					Dimension size = toolBar.getPreferredSize();
					dragWindow.setSize(size.width, size.height);
					dragWindow.show();
				}
			} catch (IllegalComponentStateException e) {
			}
		}
	}

	protected void floatAt(Point position, Point origin) {
		if (toolBar.isFloatable() == true) {
			try {
				Point offset = dragWindow.getOffset();
				if (offset == null) {
					offset = position;
					dragWindow.setOffset(offset);
				}
				Point global = new Point(origin.x + position.x, origin.y
						+ position.y);
				setFloatingLocation(global.x - offset.x, global.y - offset.y);
				if (dockingSource != null) {
					Point dockingPosition = dockingSource.getLocationOnScreen();
					Point comparisonPoint = new Point(global.x
							- dockingPosition.x, global.y - dockingPosition.y);
					if (canDock(dockingSource, comparisonPoint)) {
						setFloating(false, comparisonPoint);
					} else {
						setFloating(true, null);
					}
				} else {
					setFloating(true, null);
				}
				dragWindow.setOffset(null);
			} catch (IllegalComponentStateException e) {
			}
		}
	}

	protected ContainerListener createToolBarContListener() {
		return new ToolBarContListener();
	}

	protected FocusListener createToolBarFocusListener() {
		return new ToolBarFocusListener();
	}

	protected PropertyChangeListener createPropertyListener() {
		return new PropertyListener();
	}

	protected MouseInputListener createDockingListener() {
		return new DockingListener(toolBar);
	}

	protected WindowListener createFrameListener() {
		return new FrameListener();
	}

	// The private inner classes below should be changed to protected the
	// next time API changes are allowed.

	private static abstract class KeyAction extends AbstractAction {
		public boolean isEnabled() {
			return true;
		}
	};

	private static class RightAction extends KeyAction {
		public void actionPerformed(ActionEvent e) {
			JToolBar toolBar = (JToolBar) e.getSource();
			ToolBarUI ui = (ToolBarUI) toolBar.getUI();
			ui.navigateFocusedComp(EAST);
		}
	};

	private static class LeftAction extends KeyAction {
		public void actionPerformed(ActionEvent e) {
			JToolBar toolBar = (JToolBar) e.getSource();
			ToolBarUI ui = (ToolBarUI) toolBar.getUI();
			ui.navigateFocusedComp(WEST);
		}
	};

	private static class UpAction extends KeyAction {
		public void actionPerformed(ActionEvent e) {
			JToolBar toolBar = (JToolBar) e.getSource();
			ToolBarUI ui = (ToolBarUI) toolBar.getUI();
			ui.navigateFocusedComp(NORTH);
		}
	};

	private static class DownAction extends KeyAction {
		public void actionPerformed(ActionEvent e) {
			JToolBar toolBar = (JToolBar) e.getSource();
			ToolBarUI ui = (ToolBarUI) toolBar.getUI();
			ui.navigateFocusedComp(SOUTH);
		}
	};

	protected class FrameListener extends WindowAdapter {
		public void windowClosing(WindowEvent w) {
			if (toolBar.isFloatable() == true) {
				if (dragWindow != null)
					dragWindow.setVisible(false);
				floating = false;
				if (floatingToolBar == null)
					floatingToolBar = createFloatingWindow(toolBar);
				if (floatingToolBar instanceof Window)
					((Window) floatingToolBar).setVisible(false);
				floatingToolBar.getContentPane().remove(toolBar);
				String constraint = constraintBeforeFloating;
				int orientation = mapConstraintToOrientation(constraint);
				setOrientation(orientation);
				if (dockingSource == null)
					dockingSource = toolBar.getParent();
				if (propertyListener != null)
					UIManager.removePropertyChangeListener(propertyListener);
				dockingSource.add(constraint, toolBar);
				dockingSource.invalidate();
				Container dockingSourceParent = dockingSource.getParent();
				if (dockingSourceParent != null)
					dockingSourceParent.validate();
				dockingSource.repaint();
				setFloating(false, null);
			}
		}

	}

	protected class ToolBarContListener implements ContainerListener {
		public void componentAdded(ContainerEvent e) {
			Component c = e.getChild();

			if (toolBarFocusListener != null) {
				c.addFocusListener(toolBarFocusListener);
			}

			setButtonSize(c);

			if (isRolloverBorders()) {
				setBorderToRollover(c);
			} else {
				setBorderToNonRollover(c);
			}
		}

		public void componentRemoved(ContainerEvent e) {
			Component c = e.getChild();

			if (toolBarFocusListener != null) {
				c.removeFocusListener(toolBarFocusListener);
			}

			// Revert the button border
			setBorderToNormal(c);
		}

	} // end class ToolBarContListener

	protected void setButtonSize(Component c) {
		if (c instanceof AbstractButton) {
			// ((AbstractButton)c).setPreferredSize(new
			// Dimension(c.getPreferredSize().width+4,
			// c.getPreferredSize().height+4));
			((AbstractButton) c).setRolloverEnabled(true);
			((AbstractButton) c).setBackground(toolBar.getBackground());
		}
	}

	protected class ToolBarFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {
			Component c = e.getComponent();

			focusedCompIndex = toolBar.getComponentIndex(c);
		}

		public void focusLost(FocusEvent e) {
		}

	} // end class ToolBarFocusListener

	protected class PropertyListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (propertyName.equals("lookAndFeel")) {
				toolBar.updateUI();

			} else if (propertyName.equals("orientation")) {
				setOrientedBorder();

				// Search for JSeparator components and change it's orientation
				// to match the toolbar and flip it's orientation.
				Component[] components = toolBar.getComponents();
				int orientation = ((Integer) e.getNewValue()).intValue();
				JToolBar.Separator separator;

				for (int i = 0; i < components.length; ++i) {
					if (components[i] instanceof JToolBar.Separator) {
						separator = (JToolBar.Separator) components[i];
						separator.setOrientation(orientation);
						Dimension size = separator.getSize();
						if (size.width != size.height) {
							// Flip the orientation.
							Dimension newSize = new Dimension(size.height,
									size.width);
							separator.setSeparatorSize(newSize);
						}
					}
				}
			} else if (propertyName.equals(IS_ROLLOVER)) {
				setRolloverBorders(((Boolean) e.getNewValue()).booleanValue());
			}
		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug. This
	 * class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of BasicToolBarUI.
	 */
	public class DockingListener implements MouseInputListener {
		protected JToolBar toolBar;
		protected boolean isDragging = false;
		protected Point origin = null;

		public DockingListener(JToolBar t) {
			this.toolBar = t;
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (!toolBar.isEnabled()) {
				return;
			}
			isDragging = false;
		}

		public void mouseReleased(MouseEvent e) {
			if (!toolBar.isEnabled()) {
				return;
			}
			if (isDragging == true) {
				Point position = e.getPoint();
				if (origin == null)
					origin = e.getComponent().getLocationOnScreen();
				floatAt(position, origin);
			}
			origin = null;
			isDragging = false;
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseDragged(MouseEvent e) {
			if (!toolBar.isEnabled()) {
				return;
			}
			isDragging = true;
			Point position = e.getPoint();
			if (origin == null)
				origin = e.getComponent().getLocationOnScreen();
			dragTo(position, origin);
		}

		public void mouseMoved(MouseEvent e) {
		}
	}

	protected class DragWindow extends Window {
		Color borderColor = Color.gray;
		int orientation = toolBar.getOrientation();
		Point offset; // offset of the mouse cursor inside the DragWindow

		DragWindow(Window w) {
			super(w);
		}

		public void setOrientation(int o) {
			if (isShowing()) {
				if (o == this.orientation)
					return;
				this.orientation = o;
				Dimension size = getSize();
				setSize(new Dimension(size.height, size.width));
				if (offset != null) {
					if (TonicUtils.isLeftToRight(toolBar)) {
						setOffset(new Point(offset.y, offset.x));
					} else if (o == JToolBar.HORIZONTAL) {
						setOffset(new Point(size.height - offset.y, offset.x));
					} else {
						setOffset(new Point(offset.y, size.width - offset.x));
					}
				}
				repaint();
			}
		}

		public Point getOffset() {
			return offset;
		}

		public void setOffset(Point p) {
			this.offset = p;
		}

		public void setBorderColor(Color c) {
			if (this.borderColor == c)
				return;
			this.borderColor = c;
			repaint();
		}

		public Color getBorderColor() {
			return this.borderColor;
		}

		public void paint(Graphics g) {
			Color temp = g.getColor();
			g.setColor(getBackground());
			Dimension size = getSize();
			g.fillRect(0, 0, size.width, size.height);
			g.setColor(UIManager.getColor("Button.borderColor"));

			switch (orientation) {
			case WEST:
			case NORTH:
				g.drawLine(0, size.height - 1, size.width - 1, size.height - 1);
				break;

			case SOUTH:
			case EAST:
			}

			// super.paint(g);
		}

		public Insets getInsets() {
			return new Insets(1, 1, 1, 1);
		}
	}
}
