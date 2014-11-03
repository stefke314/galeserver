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
 * AboutBox.java
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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * This class defines the aboutbox dialog.
 * 
 */
public class AboutBox {
	private JPanel jPanel1 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private TitledBorder titledBorder1;
	private JPanel jPanel2 = new JPanel();
	public URL home = null;
	public JDialog frame;
	private JPanel jPanel3 = new JPanel();
	private JTextArea jTextArea1 = new JTextArea();
	JButton jButton1 = new JButton();

	public AboutBox(JFrame parentFrame, URL base) {
		home = base;

		try {
			jbInit();

			frame = new JDialog(parentFrame, "Load Graph Author application",
					true);
			frame.setSize(500, 430);
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
		jLabel1.setText("About Graph Author");
		jLabel1.setBounds(new Rectangle(11, 7, 463, 47));
		jPanel1.setLayout(null);

		jPanel2.setBorder(BorderFactory.createEtchedBorder());
		jPanel2.setBounds(new Rectangle(12, 61, 464, 139));
		jPanel2.setLayout(null);

		JButton buttonGif = new JButton(new ImageIcon(getClass().getResource(
				GraphAuthor.iconpath + "nlnet-tue.gif")));
		buttonGif.setBounds(new Rectangle(1, 1, 461, 131));
		buttonGif.setBorder(null);
		jPanel3.setBorder(BorderFactory.createEtchedBorder());
		jPanel3.setBounds(new Rectangle(12, 206, 465, 99));
		jPanel3.setLayout(null);

		jTextArea1
				.setText("Graph Author v 3.0 \n Design by: Paul De Bra, Ad Aerts & Brendan Rousseau"
						+ " \n Programmer: Brendan Rousseau, Bart Berden \n Build date: "
						+ "20030606");
		jTextArea1.setBounds(new Rectangle(5, 6, 455, 84));
		jButton1.setBounds(new Rectangle(186, 315, 73, 29));
		jButton1.setText("Ok");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jPanel1.add(jLabel1, null);
		jPanel1.add(jPanel2, null);
		jPanel2.add(buttonGif);
		jPanel1.add(jPanel3, null);
		jPanel3.add(jTextArea1, null);
		jPanel1.add(jButton1, null);
	}

	public void show() {
		frame.setVisible(true);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		frame.setVisible(false);
	}

}