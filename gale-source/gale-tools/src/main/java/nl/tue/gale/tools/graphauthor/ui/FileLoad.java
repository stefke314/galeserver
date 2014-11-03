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
 * FileLoad.java
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import nl.tue.gale.tools.graphauthor.data.AuthorSTATIC;

/**
 * This class loads a saved xml file into memory and shows the relations on the
 * graph.
 * 
 */

public class FileLoad {
	private JPanel jPanel1 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JButton jButton1 = new JButton();
	public JDialog frame;
	public String fileName;
	public boolean cancelled;
	public URL home;
	public String dirname;

	private JButton Cancel_button = new JButton();
	private JComboBox jComboBox1 = new JComboBox();

	public FileLoad(URL base, JFrame parentFrame) {
		cancelled = false;
		home = base;
		String path = home.getPath();
		String pathttemp = path.substring(1, path.length());
		int index = pathttemp.indexOf("/");
		index++;
		dirname = path.substring(0, index);
		if (dirname.equals("/GraphAuthor")) {
			dirname = "";
		}
		try {
			jbInit();
			fillList();
			frame = new JDialog(parentFrame, "Load Graph Author application",
					true);
			frame.setSize(600, 200);
			frame.setLocation(100, 100);
			frame.getContentPane().add(jPanel1);
			frame.getRootPane().setDefaultButton(jButton1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fillList() {

		try {

			URL url = new URL("http://" + home.getHost() + ":" + home.getPort()
					+ dirname + "/authorservlets/ListFiles?extention=" + ".gaf"
					+ "&userName=" + AuthorSTATIC.authorName);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String sFile = "";

			do {
				sFile = in.readLine();
				if (sFile != null) {
					this.jComboBox1.addItem(sFile.trim());
				}

			} while (sFile != null);
			in.close();

		} catch (Exception e) {
			System.out.println("error");
		}
	}

	private void jbInit() throws Exception {
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 16));
		jLabel1.setBorder(BorderFactory.createLoweredBevelBorder());
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText("File Load Dialog");
		jLabel1.setBounds(new Rectangle(12, 9, 557, 44));
		jPanel1.setLayout(null);
		jButton1.setBounds(new Rectangle(286, 101, 138, 36));
		jButton1.setSelected(true);
		jButton1.setText("Load");
		jButton1.setDefaultCapable(true);
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		Cancel_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Cancel_button_actionPerformed(e);
			}
		});
		Cancel_button.setText("Cancel");
		Cancel_button.setBounds(new Rectangle(432, 100, 138, 36));
		jComboBox1.setEditable(true);
		jComboBox1.setBounds(new Rectangle(11, 61, 558, 30));
		jPanel1.add(jLabel1, null);
		jPanel1.add(jComboBox1, null);
		jPanel1.add(Cancel_button, null);
		jPanel1.add(jButton1, null);

	}

	public void show() {
		frame.setVisible(true);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		fileName = ((String) this.jComboBox1.getSelectedItem()).trim();
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}

	void Cancel_button_actionPerformed(ActionEvent e) {

		cancelled = true;
		frame.setVisible(false);
		frame.dispose();
		frame = null;

	}
}