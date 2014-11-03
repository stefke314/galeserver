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
 * UnaryDialog.java
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import nl.tue.gale.tools.graphauthor.data.AuthorSTATIC;
import nl.tue.gale.tools.graphauthor.data.CRTConceptRelationType;

/**
 * This class displays the UnaryDailog.
 * 
 */
public class UnaryDialog {
	public JDialog frame;
	private JPanel jPanel1 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JPanel jPanel2 = new JPanel();
	private JButton addOne = new JButton();
	private JButton removeOne = new JButton();
	private JButton cancelButton = new JButton();
	private JButton okButton = new JButton();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	public boolean cancelled;
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList crtList = new JList();
	private JScrollPane jScrollPane2 = new JScrollPane();
	private JTextField labelValue = new JTextField();
	private JLabel jLabel4 = new JLabel();
	public Hashtable unaryRel = new Hashtable();
	public Vector crtData = new Vector();
	public Vector ucrtData = new Vector();
	private JList ucrtList = new JList();

	public UnaryDialog(JFrame parentFrame, Hashtable uRel) {
		unaryRel = uRel;

		try {
			cancelled = false;
			jbInit();
			this.initExtraComponents();
			frame = new JDialog(parentFrame, "Unary Relations", true);
			frame.setSize(560, 440);
			frame.setLocation(100, 100);
			frame.getContentPane().add(jPanel1);
			frame.getRootPane().setDefaultButton(okButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void show() {
		frame.setVisible(true);
	}

	private void jbInit() throws Exception {
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 16));
		jLabel1.setBorder(BorderFactory.createLoweredBevelBorder());
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText("Unary Relations");
		jLabel1.setBounds(new Rectangle(11, 9, 523, 56));
		jPanel1.setLayout(null);
		jPanel2.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel2.setBounds(new Rectangle(14, 83, 522, 289));
		jPanel2.setLayout(null);
		addOne.setBounds(new Rectangle(234, 144, 48, 27));
		addOne.setText(">");
		addOne.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOne_actionPerformed(e);
			}
		});
		removeOne.setBounds(new Rectangle(235, 177, 48, 27));
		removeOne.setText("<");
		removeOne.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeOne_actionPerformed(e);
			}
		});
		cancelButton.setBounds(new Rectangle(388, 250, 125, 29));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		okButton.setBounds(new Rectangle(244, 251, 132, 29));
		okButton.setText("Ok");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton_actionPerformed(e);
			}
		});
		jLabel2.setText("Available Unary Relations");
		jLabel2.setBounds(new Rectangle(49, 16, 150, 25));
		jLabel3.setBounds(new Rectangle(336, 13, 148, 25));
		jLabel3.setText("Applied Unary Relations");
		jScrollPane1.setBounds(new Rectangle(20, 44, 202, 168));
		jScrollPane2.setBounds(new Rectangle(297, 44, 211, 163));
		labelValue.setBounds(new Rectangle(298, 216, 210, 24));
		jLabel4.setText("Label:");
		jLabel4.setBounds(new Rectangle(249, 219, 41, 18));
		jPanel2.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(crtList, null);
		jPanel2.add(jScrollPane2, null);
		jScrollPane2.getViewport().add(ucrtList, null);
		jPanel2.add(jLabel2, null);
		jPanel2.add(jLabel3, null);
		jPanel2.add(labelValue, null);
		jPanel2.add(okButton, null);
		jPanel2.add(cancelButton, null);
		jPanel2.add(jLabel4, null);
		jPanel2.add(removeOne, null);
		jPanel2.add(addOne, null);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jPanel2, null);
	}

	void initExtraComponents() {
		if (this.unaryRel != null) {
			for (Iterator j = this.unaryRel.entrySet().iterator(); j.hasNext();) {
				Map.Entry m = (Map.Entry) j.next();
				String sLabel = (String) m.getValue();
				String ucrt = (String) m.getKey();
				ucrt = ucrt + "#" + sLabel;
				this.ucrtData.add(ucrt);
			}
		} else {
			this.unaryRel = new Hashtable();
		}

		for (Iterator i = AuthorSTATIC.CRTList.iterator(); i.hasNext();) {
			CRTConceptRelationType crt = (CRTConceptRelationType) i.next();

			if (crt.properties.unary.booleanValue() == true) {
				// crt may be unary
				if ((this.unaryRel == null)
						|| (this.unaryRel.containsKey(crt.name) == false)) {
					// there is no unary relation defined for this type
					this.crtData.add(crt.name);
				}
			}
		}

		this.crtList.setListData(this.crtData);
		this.ucrtList.setListData(this.ucrtData);
	}

	void addOne_actionPerformed(ActionEvent e) {
		try {
			String selectedItem = (String) this.crtList.getSelectedValue();
			this.crtData.remove(selectedItem);

			this.unaryRel.put(selectedItem, this.labelValue.getText());
			selectedItem = selectedItem + "#" + this.labelValue.getText();
			this.ucrtData.add(selectedItem);
			this.crtList.setListData(this.crtData);
			this.ucrtList.setListData(this.ucrtData);
			this.labelValue.setText("");
		} catch (Exception e1) {
			System.out.println("exception while adding");
			e1.printStackTrace();
		}
	}

	void removeOne_actionPerformed(ActionEvent e) {
		try {
			String selectedItem = (String) this.ucrtList.getSelectedValue();
			this.ucrtData.remove(selectedItem);
			int index = selectedItem.indexOf("#");
			selectedItem = selectedItem.substring(0, index);
			this.unaryRel.remove(selectedItem);
			this.crtData.add(selectedItem);
			this.ucrtList.setListData(this.ucrtData);
			this.crtList.setListData(this.crtData);
			this.labelValue.setText("");
		} catch (Exception e1) {
			System.out.println("exception while removing");
			e1.printStackTrace();
		}
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		cancelled = true;
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}

	void okButton_actionPerformed(ActionEvent e) {
		frame.setVisible(false);
	}
}