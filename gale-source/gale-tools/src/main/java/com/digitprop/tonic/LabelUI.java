package com.digitprop.tonic;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLabelUI;

/**
 * UI delegate for JLabels.
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
public class LabelUI extends MetalLabelUI {
	protected static LabelUI labelUI = new LabelUI();

	public static ComponentUI createUI(JComponent c) {
		return labelUI;
	}
}
