package com.digitprop.tonic;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;

/**
 * The Swing DropTarget implementation supports multicast notification to
 * listeners, so this implementation is used as an additional listener that
 * extends the primary drop target functionality (i.e. linkage to the
 * TransferHandler) to include autoscroll and establish an insertion point for
 * the drop. This is used by the ComponentUI of components supporting a
 * selection mechanism, which have a way of indicating a location within their
 * model.
 * <p>
 * 
 * The autoscroll functionality is based upon the Swing scrolling mechanism of
 * the Scrollable interface. The unit scroll increment is used to as the scroll
 * amount, and the scrolling is based upon JComponent.getVisibleRect and
 * JComponent.scrollRectToVisible. The band of area around the visible rectangle
 * used to invoke autoscroll is based upon the unit scroll increment as that is
 * assumed to represent the last possible item in the visible region.
 * <p>
 * 
 * The subclasses are expected to implement the following methods to manage the
 * insertion location via the components selection mechanism.
 * 
 * <ul>
 * <li>saveComponentState
 * <li>restoreComponentState
 * <li>restoreComponentStateForDrop
 * <li>updateInsertionLocation
 * </ul>
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
class TonicDropTargetListener implements DropTargetListener, UIResource,
		ActionListener {

	/**
	 * construct a DropTargetAutoScroller
	 * <P>
	 * 
	 * @param c
	 *            the <code>Component</code>
	 * @param p
	 *            the <code>Point</code>
	 */
	protected TonicDropTargetListener() {
	}

	/**
	 * called to save the state of a component in case it needs to be restored
	 * because a drop is not performed.
	 */
	protected void saveComponentState(JComponent c) {
	}

	/**
	 * called to restore the state of a component in case a drop is not
	 * performed.
	 */
	protected void restoreComponentState(JComponent c) {
	}

	/**
	 * called to restore the state of a component in case a drop is performed.
	 */
	protected void restoreComponentStateForDrop(JComponent c) {
	}

	/**
	 * called to set the insertion location to match the current mouse pointer
	 * coordinates.
	 */
	protected void updateInsertionLocation(JComponent c, Point p) {
	}

	/**
	 * Update the geometry of the autoscroll region. The geometry is maintained
	 * as a pair of rectangles. The region can cause a scroll if the pointer
	 * sits inside it for the duration of the timer. The region that causes the
	 * timer countdown is the area between the two rectangles.
	 * <p>
	 * This is implemented to use the visible area of the component as the outer
	 * rectangle and the insets are based upon the Scrollable information (if
	 * any). If the Scrollable is scrollable along an axis, the step increment
	 * is used as the autoscroll inset. If the component is not scrollable, the
	 * insets will be zero (i.e. autoscroll will not happen).
	 */
	void updateAutoscrollRegion(JComponent c) {
		// compute the outer
		Rectangle visible = c.getVisibleRect();
		outer.reshape(visible.x, visible.y, visible.width, visible.height);

		// compute the insets
		// TBD - the thing with the scrollable
		Insets i = new Insets(0, 0, 0, 0);
		if (c instanceof Scrollable) {
			Scrollable s = (Scrollable) c;
			i.left = s.getScrollableUnitIncrement(visible,
					SwingConstants.HORIZONTAL, 1);
			i.top = s.getScrollableUnitIncrement(visible,
					SwingConstants.VERTICAL, 1);
			i.right = s.getScrollableUnitIncrement(visible,
					SwingConstants.HORIZONTAL, -1);
			i.bottom = s.getScrollableUnitIncrement(visible,
					SwingConstants.VERTICAL, -1);
		}

		// set the inner from the insets
		inner.reshape(visible.x + i.left, visible.y + i.top, visible.width
				- (i.left + i.right), visible.height - (i.top + i.bottom));
	}

	/**
	 * Perform an autoscroll operation. This is implemented to scroll by the
	 * unit increment of the Scrollable using scrollRectToVisible. If the cursor
	 * is in a corner of the autoscroll region, more than one axis will scroll.
	 */
	void autoscroll(JComponent c, Point pos) {
		if (c instanceof Scrollable) {
			Scrollable s = (Scrollable) c;
			if (pos.y < inner.y) {
				// scroll top downward
				int dy = s.getScrollableUnitIncrement(outer,
						SwingConstants.VERTICAL, 1);
				Rectangle r = new Rectangle(inner.x, outer.y - dy, inner.width,
						dy);
				c.scrollRectToVisible(r);
			} else if (pos.y > (inner.y + inner.height)) {
				// scroll bottom upward
				int dy = s.getScrollableUnitIncrement(outer,
						SwingConstants.VERTICAL, -1);
				Rectangle r = new Rectangle(inner.x, outer.y + outer.height,
						inner.width, dy);
				c.scrollRectToVisible(r);
			}

			if (pos.x < inner.x) {
				// scroll left side to the right
				int dx = s.getScrollableUnitIncrement(outer,
						SwingConstants.HORIZONTAL, 1);
				Rectangle r = new Rectangle(outer.x - dx, inner.y, dx,
						inner.height);
				c.scrollRectToVisible(r);
			} else if (pos.x > (inner.x + inner.width)) {
				// scroll right side to the left
				int dx = s.getScrollableUnitIncrement(outer,
						SwingConstants.HORIZONTAL, -1);
				Rectangle r = new Rectangle(outer.x + outer.width, inner.y, dx,
						inner.height);
				c.scrollRectToVisible(r);
			}
		}
	}

	/**
	 * Initializes the internal properties if they haven't been already inited.
	 * This is done lazily to avoid loading of desktop properties.
	 */
	private void initPropertiesIfNecessary() {
		if (timer == null) {
			Toolkit t = Toolkit.getDefaultToolkit();
			Integer initial = new Integer(100);
			Integer interval = new Integer(100);

			try {
				initial = (Integer) t
						.getDesktopProperty("DnD.Autoscroll.initialDelay");
			} catch (Exception e) {
				// ignore
			}
			try {
				interval = (Integer) t
						.getDesktopProperty("DnD.Autoscroll.interval");
			} catch (Exception e) {
				// ignore
			}
			timer = new Timer(interval.intValue(), this);

			timer.setCoalesce(true);
			timer.setInitialDelay(initial.intValue());

			try {
				hysteresis = ((Integer) t
						.getDesktopProperty("DnD.Autoscroll.cursorHysteresis"))
						.intValue();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	static JComponent getComponent(DropTargetEvent e) {
		DropTargetContext context = e.getDropTargetContext();
		return (JComponent) context.getComponent();
	}

	// --- ActionListener methods --------------------------------------

	/**
	 * The timer fired, perform autoscroll if the pointer is within the
	 * autoscroll region.
	 * <P>
	 * 
	 * @param e
	 *            the <code>ActionEvent</code>
	 */
	public synchronized void actionPerformed(ActionEvent e) {
		updateAutoscrollRegion(component);
		if (outer.contains(lastPosition) && !inner.contains(lastPosition)) {
			autoscroll(component, lastPosition);
		}
	}

	// --- DropTargetListener methods -----------------------------------

	public void dragEnter(DropTargetDragEvent e) {
		component = getComponent(e);
		TransferHandler th = component.getTransferHandler();
		canImport = th.canImport(component, e.getCurrentDataFlavors());
		if (canImport) {
			saveComponentState(component);
			lastPosition = e.getLocation();
			updateAutoscrollRegion(component);
			initPropertiesIfNecessary();
		}
	}

	public void dragOver(DropTargetDragEvent e) {
		if (canImport) {
			Point p = e.getLocation();
			updateInsertionLocation(component, p);

			// check autoscroll
			synchronized (this) {
				if (Math.abs(p.x - lastPosition.x) > hysteresis
						|| Math.abs(p.y - lastPosition.y) > hysteresis) {
					// no autoscroll
					if (timer.isRunning())
						timer.stop();
				} else {
					if (!timer.isRunning())
						timer.start();
				}
				lastPosition = p;
			}
		}
	}

	public void dragExit(DropTargetEvent e) {
		if (canImport) {
			restoreComponentState(component);
		}
		cleanup();
	}

	public void drop(DropTargetDropEvent e) {
		if (canImport) {
			restoreComponentStateForDrop(component);
		}
		cleanup();
	}

	public void dropActionChanged(DropTargetDragEvent e) {
	}

	/**
	 * Cleans up internal state after the drop has finished (either succeeded or
	 * failed).
	 */
	private void cleanup() {
		if (timer != null) {
			timer.stop();
		}
		component = null;
		lastPosition = null;
	}

	// --- fields --------------------------------------------------

	private Timer timer;
	private Point lastPosition;
	private Rectangle outer = new Rectangle();
	private Rectangle inner = new Rectangle();
	private int hysteresis = 10;
	private boolean canImport;

	/**
	 * The current component. The value is cached from the drop events and used
	 * by the timer. When a drag exits or a drop occurs, this value is cleared.
	 */
	private JComponent component;

}
