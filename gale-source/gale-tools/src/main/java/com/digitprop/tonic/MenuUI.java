package com.digitprop.tonic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;

/**
 * UI delegate for JMenus.
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
public class MenuUI extends MenuItemUI {
	/** Associated change listener */
	protected ChangeListener changeListener;

	/** Associated property change listener */
	protected PropertyChangeListener propertyChangeListener;

	/** Associated menu listener */
	protected MenuListener menuListener;

	/** Shared instance of the menuListener */
	private static MenuListener sharedMenuListener;

	/** Last used mnemonic */
	private int lastMnemonic = 0;

	/** Uses as the parent of the windowInputMap when selected. */
	private InputMap selectedWindowInputMap;

	/** Diagnostic aids -- should be false for production builds. */
	private static final boolean TRACE = false; // trace creates and disposes

	/** Diagnostic aids -- should be false for production builds. */
	private static final boolean VERBOSE = false; // show reuse hits/misses

	/** Diagnostic aids -- should be false for production builds. */
	private static final boolean DEBUG = false; // show bad params, misc.

	/** Creates and returns a UI delegate for the specified component */
	public static ComponentUI createUI(JComponent x) {
		return new MenuUI();
	}

	protected void installDefaults() {
		super.installDefaults();
		((JMenu) menuItem).setDelay(200);
	}

	protected String getPropertyPrefix() {
		return "Menu";
	}

	protected void installListeners() {
		super.installListeners();

		if (changeListener == null)
			changeListener = createChangeListener(menuItem);

		if (changeListener != null)
			menuItem.addChangeListener(changeListener);

		if (propertyChangeListener == null)
			propertyChangeListener = createPropertyChangeListener(menuItem);

		if (propertyChangeListener != null)
			menuItem.addPropertyChangeListener(propertyChangeListener);

		if (menuListener == null)
			menuListener = createMenuListener(menuItem);

		if (menuListener != null)
			((JMenu) menuItem).addMenuListener(menuListener);
	}

	protected void installKeyboardActions() {
		super.installKeyboardActions();
		updateMnemonicBinding();
	}

	void updateMnemonicBinding() {
		int mnemonic = menuItem.getModel().getMnemonic();
		int[] shortcutKeys = (int[]) UIManager.get("Menu.shortcutKeys");
		if (mnemonic == lastMnemonic || shortcutKeys == null) {
			return;
		}
		if (lastMnemonic != 0 && windowInputMap != null) {
			for (int i = 0; i < shortcutKeys.length; i++) {
				windowInputMap.remove(KeyStroke.getKeyStroke(lastMnemonic,
						shortcutKeys[i], false));
			}
		}
		if (mnemonic != 0) {
			if (windowInputMap == null) {
				windowInputMap = createMyInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				SwingUtilities.replaceUIInputMap(menuItem,
						JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
			}
			for (int i = 0; i < shortcutKeys.length; i++) {
				windowInputMap.put(KeyStroke.getKeyStroke(mnemonic,
						shortcutKeys[i], false), "selectMenu");
			}
		}
		lastMnemonic = mnemonic;
	}

	protected void uninstallKeyboardActions() {
		super.uninstallKeyboardActions();
	}

	/**
	 * The ActionMap for BasicMenUI can not be shared, this is subclassed to
	 * create a new one for each invocation.
	 */
	ActionMap getMyActionMap() {
		return createMyActionMap();
	}

	/**
	 * Invoked to create the ActionMap.
	 */
	ActionMap createMyActionMap() {
		ActionMap am = super.createMyActionMap();
		if (am != null) {
			am.put("selectMenu", new PostAction((JMenu) menuItem, true));
		}
		return am;
	}

	protected MouseInputListener createMouseInputListener(JComponent c) {
		return new MouseInputHandler();
	}

	protected MenuListener createMenuListener(JComponent c) {
		if (sharedMenuListener == null) {
			sharedMenuListener = new MenuHandler();
		}
		return sharedMenuListener;
	}

	protected ChangeListener createChangeListener(JComponent c) {
		return null;
	}

	protected PropertyChangeListener createPropertyChangeListener(JComponent c) {
		return new PropertyChangeHandler();
	}

	protected void uninstallDefaults() {
		menuItem.setArmed(false);
		menuItem.setSelected(false);
		menuItem.resetKeyboardActions();
		super.uninstallDefaults();
	}

	protected void uninstallListeners() {
		super.uninstallListeners();

		if (changeListener != null)
			menuItem.removeChangeListener(changeListener);

		if (propertyChangeListener != null)
			menuItem.removePropertyChangeListener(propertyChangeListener);

		if (menuListener != null)
			((JMenu) menuItem).removeMenuListener(menuListener);

		changeListener = null;
		propertyChangeListener = null;
		menuListener = null;
	}

	protected MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
		return new MenuDragMouseHandler();
	}

	protected MenuKeyListener createMenuKeyListener(JComponent c) {
		return new MenuKeyHandler();
	}

	/** Returns the maximum size for the specified component */
	public Dimension getMaximumSize(JComponent c) {
		if (((JMenu) menuItem).isTopLevelMenu() == true) {
			Dimension d = c.getPreferredSize();
			return new Dimension(d.width, Short.MAX_VALUE);
		}
		return null;
	}

	protected void setupPostTimer(JMenu menu) {
		Timer timer = new Timer(menu.getDelay(), new PostAction(menu, false));
		timer.setRepeats(false);
		timer.start();
	}

	private static void appendPath(MenuElement[] path, MenuElement elem) {
		MenuElement newPath[] = new MenuElement[path.length + 1];
		System.arraycopy(path, 0, newPath, 0, path.length);
		newPath[path.length] = elem;
		MenuSelectionManager.defaultManager().setSelectedPath(newPath);
	}

	private static class PostAction extends AbstractAction {
		JMenu menu;
		boolean force = false;

		PostAction(JMenu menu, boolean shouldForce) {
			this.menu = menu;
			this.force = shouldForce;
		}

		public void actionPerformed(ActionEvent e) {
			final MenuSelectionManager defaultManager = MenuSelectionManager
					.defaultManager();
			if (force) {
				Container cnt = menu.getParent();
				if (cnt != null && cnt instanceof JMenuBar) {
					MenuElement me[];
					MenuElement subElements[];

					subElements = menu.getPopupMenu().getSubElements();
					if (subElements.length > 0) {
						me = new MenuElement[4];
						me[0] = (MenuElement) cnt;
						me[1] = (MenuElement) menu;
						me[2] = (MenuElement) menu.getPopupMenu();
						me[3] = subElements[0];
					} else {
						me = new MenuElement[3];
						me[0] = (MenuElement) cnt;
						me[1] = menu;
						me[2] = (MenuElement) menu.getPopupMenu();
					}
					defaultManager.setSelectedPath(me);
				}
			} else {
				MenuElement path[] = defaultManager.getSelectedPath();
				if (path.length > 0 && path[path.length - 1] == menu) {
					appendPath(path, menu.getPopupMenu());
				}
			}
		}

		public boolean isEnabled() {
			return menu.getModel().isEnabled();
		}
	}

	private class PropertyChangeHandler implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e) {
			String prop = e.getPropertyName();
			if (prop.equals(AbstractButton.MNEMONIC_CHANGED_PROPERTY)) {
				updateMnemonicBinding();
			}
		}
	}

	/**
	 * Instantiated and used by a menu item to handle the current menu selection
	 * from mouse events. A MouseInputHandler processes and forwards all mouse
	 * events to a shared instance of the MenuSelectionManager.
	 * <p>
	 * This class is protected so that it can be subclassed by other look and
	 * feels to implement their own mouse handling behavior. All overridden
	 * methods should call the parent methods so that the menu selection is
	 * correct.
	 * 
	 * @see javax.swing.MenuSelectionManager
	 * @since 1.4
	 */
	protected class MouseInputHandler implements MouseInputListener {
		public void mouseClicked(MouseEvent e) {
		}

		/**
		 * Invoked when the mouse has been clicked on the menu. This method
		 * clears or sets the selection path of the MenuSelectionManager.
		 * 
		 * @param e
		 *            the mouse event
		 */
		public void mousePressed(MouseEvent e) {
			JMenu menu = (JMenu) menuItem;
			if (!menu.isEnabled())
				return;

			MenuSelectionManager manager = MenuSelectionManager
					.defaultManager();
			if (menu.isTopLevelMenu()) {
				if (menu.isSelected()) {
					manager.clearSelectedPath();
				} else {
					Container cnt = menu.getParent();
					if (cnt != null && cnt instanceof JMenuBar) {
						MenuElement me[] = new MenuElement[2];
						me[0] = (MenuElement) cnt;
						me[1] = menu;
						manager.setSelectedPath(me);
					}
				}
			}

			MenuElement selectedPath[] = manager.getSelectedPath();
			if (selectedPath.length > 0
					&& selectedPath[selectedPath.length - 1] != menu
							.getPopupMenu()) {

				if (menu.isTopLevelMenu() || menu.getDelay() == 0) {
					appendPath(selectedPath, menu.getPopupMenu());
				} else {
					setupPostTimer(menu);
				}
			}
		}

		/**
		 * Invoked when the mouse has been released on the menu. Delegates the
		 * mouse event to the MenuSelectionManager.
		 * 
		 * @param e
		 *            the mouse event
		 */
		public void mouseReleased(MouseEvent e) {
			JMenu menu = (JMenu) menuItem;
			if (!menu.isEnabled())
				return;
			MenuSelectionManager manager = MenuSelectionManager
					.defaultManager();
			manager.processMouseEvent(e);
			if (!e.isConsumed())
				manager.clearSelectedPath();
		}

		/**
		 * Invoked when the cursor enters the menu. This method sets the
		 * selected path for the MenuSelectionManager and handles the case in
		 * which a menu item is used to pop up an additional menu, as in a
		 * hierarchical menu system.
		 * 
		 * @param e
		 *            the mouse event; not used
		 */
		public void mouseEntered(MouseEvent e) {
			JMenu menu = (JMenu) menuItem;
			if (!menu.isEnabled())
				return;

			MenuSelectionManager manager = MenuSelectionManager
					.defaultManager();
			MenuElement selectedPath[] = manager.getSelectedPath();
			if (!menu.isTopLevelMenu()) {
				if (!(selectedPath.length > 0 && selectedPath[selectedPath.length - 1] == menu
						.getPopupMenu())) {
					if (menu.getDelay() == 0) {
						appendPath(getPath(), menu.getPopupMenu());
					} else {
						manager.setSelectedPath(getPath());
						setupPostTimer(menu);
					}
				}
			} else {
				if (selectedPath.length > 0
						&& selectedPath[0] == menu.getParent()) {
					MenuElement newPath[] = new MenuElement[3];
					// A top level menu's parent is by definition
					// a JMenuBar
					newPath[0] = (MenuElement) menu.getParent();
					newPath[1] = menu;
					newPath[2] = menu.getPopupMenu();
					manager.setSelectedPath(newPath);
				}
			}
		}

		public void mouseExited(MouseEvent e) {
		}

		/**
		 * Invoked when a mouse button is pressed on the menu and then dragged.
		 * Delegates the mouse event to the MenuSelectionManager.
		 * 
		 * @param e
		 *            the mouse event
		 * @see java.awt.event.MouseMotionListener#mouseDragged
		 */
		public void mouseDragged(MouseEvent e) {
			JMenu menu = (JMenu) menuItem;
			if (!menu.isEnabled())
				return;
			MenuSelectionManager.defaultManager().processMouseEvent(e);
		}

		public void mouseMoved(MouseEvent e) {
		}
	}

	private static class MenuHandler implements MenuListener {
		public void menuSelected(MenuEvent e) {
		}

		public void menuDeselected(MenuEvent e) {
		}

		public void menuCanceled(MenuEvent e) {
			JMenu m = (JMenu) e.getSource();
			MenuSelectionManager manager = MenuSelectionManager
					.defaultManager();
			if (manager.isComponentPartOfCurrentMenu(m))
				MenuSelectionManager.defaultManager().clearSelectedPath();
		}

	}

	/**
	 * As of Java 2 platform 1.4, this previously undocumented class is now
	 * obsolete. KeyBindings are now managed by the popup menu.
	 */
	public class ChangeHandler implements ChangeListener {
		public JMenu menu;
		public MenuUI ui;
		public boolean isSelected = false;
		public Component wasFocused;

		public ChangeHandler(JMenu m, MenuUI ui) {
			menu = m;
			this.ui = ui;
		}

		public void stateChanged(ChangeEvent e) {
		}
	}

	private class MenuDragMouseHandler implements MenuDragMouseListener {
		public void menuDragMouseEntered(MenuDragMouseEvent e) {
		}

		public void menuDragMouseDragged(MenuDragMouseEvent e) {
			if (menuItem.isEnabled() == false)
				return;

			MenuSelectionManager manager = e.getMenuSelectionManager();
			MenuElement path[] = e.getPath();

			Point p = e.getPoint();
			if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0
					&& p.y < menuItem.getHeight()) {
				JMenu menu = (JMenu) menuItem;
				MenuElement selectedPath[] = manager.getSelectedPath();
				if (!(selectedPath.length > 0 && selectedPath[selectedPath.length - 1] == menu
						.getPopupMenu())) {
					if (menu.isTopLevelMenu() || menu.getDelay() == 0
							|| e.getID() == MouseEvent.MOUSE_DRAGGED) {
						appendPath(path, menu.getPopupMenu());
					} else {
						manager.setSelectedPath(path);
						setupPostTimer(menu);
					}
				}
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				Component comp = manager.componentForPoint(e.getComponent(),
						e.getPoint());
				if (comp == null)
					manager.clearSelectedPath();
			}

		}

		public void menuDragMouseExited(MenuDragMouseEvent e) {
		}

		public void menuDragMouseReleased(MenuDragMouseEvent e) {
		}
	}

	private class MenuKeyHandler implements MenuKeyListener {
		public void menuKeyTyped(MenuKeyEvent e) {
			int key = menuItem.getMnemonic();
			if (key == 0)
				return;
			MenuElement path[] = e.getPath();
			if (lower(key) == lower((int) (e.getKeyChar()))) {
				JPopupMenu popupMenu = ((JMenu) menuItem).getPopupMenu();
				MenuElement sub[] = popupMenu.getSubElements();
				if (sub.length > 0) {
					MenuSelectionManager manager = e.getMenuSelectionManager();
					MenuElement newPath[] = new MenuElement[path.length + 2];
					System.arraycopy(path, 0, newPath, 0, path.length);
					newPath[path.length] = popupMenu;
					newPath[path.length + 1] = sub[0];
					manager.setSelectedPath(newPath);
				}
				e.consume();
			}
		}

		public void menuKeyPressed(MenuKeyEvent e) {
		}

		public void menuKeyReleased(MenuKeyEvent e) {
		}

		private int lower(int ascii) {
			if (ascii >= 'A' && ascii <= 'Z')
				return ascii + 'a' - 'A';
			else
				return ascii;
		}

	}
}
