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
 * TAMtProgressBarTableCellRenderer.java
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
package nl.tue.gale.tools.amt;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A TableCellRenderer that displays a JProgressBar as its component.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class TAMtProgressBarTableCellRenderer extends JProgressBar implements
		TableCellRenderer {

	/**
	 * Constructor
	 */
	public TAMtProgressBarTableCellRenderer() {
	}

	/**
	 * Renders a cell.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (value instanceof JProgressBar) {
			// System.out.println("drawing progress bar");
			JProgressBar bar = (JProgressBar) value;
			setMinimum(bar.getMinimum());
			setMaximum(bar.getMaximum());
			setValue(bar.getValue());
			return this;
		} else {
			DefaultTableCellRenderer tr = new DefaultTableCellRenderer();
			return tr.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
		}
	}
}