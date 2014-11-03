package com.digitprop.tonic;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;

/**
 * A border which is like a Margin border but it will only honor the margin if
 * the margin has been explicitly set by the developer.
 */
class ToolBarMarginBorder extends EmptyBorder {

	public ToolBarMarginBorder() {
		super(3, 3, 3, 3); // hardcoded margin for JLF requirements.
	}

	public Insets getBorderInsets(Component c) {
		return getBorderInsets(c, new Insets(0, 0, 0, 0));
	}

	public Insets getBorderInsets(Component c, Insets insets) {
		Insets margin = null;

		if (c instanceof AbstractButton) {
			margin = ((AbstractButton) c).getMargin();
		}
		if (margin == null || margin instanceof UIResource) {
			// default margin so replace
			insets.left = left;
			insets.top = top;
			insets.right = right;
			insets.bottom = bottom;
		} else {
			// Margin which has been explicitly set by the user.
			insets.left = margin.left;
			insets.top = margin.top;
			insets.right = margin.right;
			insets.bottom = margin.bottom;
		}
		return insets;
	}
}