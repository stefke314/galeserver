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
 * LoginDialog.java
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
package nl.tue.gale.tools.util;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Dialogbox for AHA!-author authorization.
 * 
 */
public class LoginDialog extends JDialog {
	private JPanel jPanel1 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JButton jButton1 = new JButton();
	public JDialog frame;
	public boolean id = false;
	private JButton Cancel_button = new JButton();
	private JPanel jPanel2 = new JPanel();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	public JTextField username = new JTextField();
	public JPasswordField password = new JPasswordField();

	public LoginDialog(JFrame parent) {
		super(parent, "Authorization", true);
		try {
			jbInit(parent);
			this.setSize(430, 250);
			this.setResizable(false);
			this.setLocation(100, 100);
			this.getContentPane().add(jPanel1);
			this.setVisible(true);
		} catch (Exception e) {
		}

	}

	private void jbInit(JFrame parent) throws Exception {
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 16));
		jLabel1.setBorder(BorderFactory.createLoweredBevelBorder());
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText("AHA! Authorization");
		jLabel1.setBounds(new Rectangle(10, 5, 407, 44));
		jPanel1.setLayout(null);
		jButton1.setBounds(new Rectangle(197, 160, 102, 31));
		jButton1.setText("Ok");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		this.getRootPane().setDefaultButton(jButton1);
		Cancel_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Cancel_button_actionPerformed(e);
			}
		});
		Cancel_button.setText("Cancel");
		Cancel_button.setBounds(new Rectangle(305, 160, 100, 31));

		jPanel2.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel2.setBounds(new Rectangle(11, 52, 407, 100));
		jPanel2.setLayout(null);
		jLabel2.setText("Password:");
		jLabel2.setBounds(new Rectangle(19, 51, 63, 17));
		jLabel3.setBounds(new Rectangle(20, 17, 41, 17));
		jLabel3.setText("User:");
		username.setText("");
		username.setBounds(new Rectangle(86, 15, 295, 25));
		password.setText("");
		password.setBounds(new Rectangle(86, 46, 295, 25));
		jPanel1.add(jLabel1, null);
		jPanel1.add(jButton1, null);
		jPanel1.add(Cancel_button, null);
		jPanel1.add(jPanel2, null);
		jPanel2.add(jLabel3, null);
		jPanel2.add(jLabel2, null);
		jPanel2.add(username, null);
		jPanel2.add(password, null);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		id = true;
		this.setVisible(false);
		this.dispose();

	}

	void Cancel_button_actionPerformed(ActionEvent e) {
		id = false;
		this.setVisible(false);
		this.dispose();

	}
}