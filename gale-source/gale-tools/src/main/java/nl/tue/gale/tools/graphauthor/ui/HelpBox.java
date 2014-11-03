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
 * HelpBox.java
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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * This class displays the helpbox.
 * 
 */
public class HelpBox {
	private JPanel jPanel1 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private TitledBorder titledBorder1;
	public URL home = null;
	public JDialog frame;
	JButton jButton1 = new JButton();
	JScrollPane jScrollPane1 = new JScrollPane();
	JEditorPane jEditorPane1 = new JEditorPane();

	public HelpBox(JFrame parentFrame, URL base) {
		home = base;

		try {
			jbInit();

			frame = new JDialog(parentFrame, "Load Graph Author application",
					true);
			frame.setSize(600, 450);
			frame.setLocation(100, 100);
			frame.getContentPane().add(jPanel1);
			frame.getRootPane().setDefaultButton(jButton1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		titledBorder1 = new TitledBorder("");
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 18));
		jLabel1.setBorder(titledBorder1);
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText("Help Graph Author");
		jLabel1.setBounds(new Rectangle(11, 7, 549, 47));
		jPanel1.setLayout(null);

		Date datum = new Date();
		jButton1.setBounds(new Rectangle(246, 347, 73, 29));
		jButton1.setText("Ok");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jScrollPane1.setBounds(new Rectangle(11, 60, 552, 279));
		jEditorPane1
				.setText("For more information look at the AHA! website: http://aha.win.tue.nl/");
		jPanel1.add(jLabel1, null);
		jPanel1.add(jButton1, null);
		jPanel1.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(jEditorPane1, null);
	}

	public void show() {
		frame.setVisible(true);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		frame.setVisible(false);
	}
}