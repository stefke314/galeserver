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
 * Attributes.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nl.tue.gale.tools.graphauthor.data.AHAOutAttribute;
import nl.tue.gale.tools.graphauthor.data.AHAOutConcept;

/**
 * Dialog for adding and editing an attribute of a concept
 * 
 */
public class Attributes {
	private LinkedList conceptlist;
	private AHAOutConcept concept;
	private HashMap attributeMap = new HashMap();

	private JDialog frame;
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JButton oKButton = new JButton("OK");
	private JButton addButton = new JButton("Add");
	private JButton editButton = new JButton("Edit");
	private JButton removeButton = new JButton("Remove");
	private JList attributeList = new JList(new DefaultListModel());
	private JLabel lblFrameTitle = new JLabel("Attributes");
	private JLabel lblAttributes = new JLabel("Attributes");
	private JFrame parentFrame;

	public Attributes(JFrame parentFrame, AHAOutConcept conceptParam) {
		this.concept = conceptParam;
		this.parentFrame = parentFrame;
		try {
			// init frame
			// fill the list with attributes
			DefaultListModel attributeHolder = (DefaultListModel) this.attributeList
					.getModel();
			attributeHolder = getAttributesListItems(attributeHolder);
			this.attributeList.setVisibleRowCount(5);

			jbInit();

			frame = new JDialog(parentFrame, "Attributes", true);
			frame.setSize(635, 340);
			frame.setLocation(100, 100);
			frame.getContentPane().add(jPanel1);
			frame.getRootPane().setDefaultButton(oKButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void show() {
		try {
			frame.setVisible(true);
		} catch (Exception e) {
			System.out
					.println("Attributes: show: Failed to initialize the attributes dialog. Exception: "
							+ e.toString());
		}
	}

	private void jbInit() throws Exception {

		jPanel1.setLayout(null);
		jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
		jPanel1.setMinimumSize(new Dimension(400, 400));

		jPanel2.setLayout(null);
		jPanel2.setBounds(new Rectangle(24, 41, 584, 199));
		// jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
		jPanel2.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel2.setMinimumSize(new Dimension(400, 400));
		jPanel1.add(jPanel2);

		lblFrameTitle.setFont(new java.awt.Font("Dialog", 1, 16));
		lblFrameTitle.setBounds(new Rectangle(227, 9, 148, 27));
		jPanel1.add(lblFrameTitle);

		// lblAttributes.setFont(new java.awt.Font("Dialog", 1, 16));
		lblAttributes.setBounds(new Rectangle(21, 12, 74, 18));
		jPanel2.add(lblAttributes);

		// attribute listbox
		JScrollPane attributeHolder = new JScrollPane(this.attributeList);
		attributeHolder.setBounds(new Rectangle(20, 35, 544, 154));
		// attributeHolder.setPreferredSize(new Dimension(300, 40));

		jPanel2.add(attributeHolder);

		addButton.setBounds(new Rectangle(23, 248, 124, 25));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addButton_actionPerformed(e);
			}
		});
		jPanel1.add(addButton);

		editButton.setBounds(new Rectangle(162, 248, 124, 25));
		editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editButton_actionPerformed(e);
			}
		});
		jPanel1.add(editButton);

		removeButton.setBounds(new Rectangle(301, 248, 124, 25));
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeButton_actionPerformed(e);
			}
		});
		jPanel1.add(removeButton);

		oKButton.setText("Ok");
		oKButton.setBounds(new Rectangle(440, 248, 124, 25));
		oKButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				oKButton_actionPerformed(e);
			}
		});
		jPanel1.add(oKButton);
	}

	private void addButton_actionPerformed(ActionEvent e) {
		AHAOutAttribute newAttribute = new AHAOutAttribute();
		concept.attributeList.add(newAttribute);
		AttributeEditor atrEditor = new AttributeEditor(parentFrame, concept,
				newAttribute, true);
		atrEditor.show();
		// refresh list
		DefaultListModel attributeHolder = (DefaultListModel) this.attributeList
				.getModel();
		attributeHolder = getAttributesListItems(attributeHolder);
	}

	private void editButton_actionPerformed(ActionEvent e) {
		try {
			AHAOutAttribute attributeListItem = getAttributesListItem(attributeList
					.getSelectedValue());
			AttributeEditor atrEditor = new AttributeEditor(parentFrame,
					concept, attributeListItem, false);
			atrEditor.show();
		} catch (Exception exc) {
		}
	}

	private void removeButton_actionPerformed(ActionEvent e) {
		try {
			// get the selected item.
			AHAOutAttribute attributeListItem = getAttributesListItem(attributeList
					.getSelectedValue());
			// remove the item
			concept.attributeList.remove(attributeListItem);

			// reload the JList.
			DefaultListModel dataHolder = (DefaultListModel) attributeList
					.getModel();
			dataHolder = getAttributesListItems(dataHolder);
		} catch (Exception exc) {
		}
	}

	private void oKButton_actionPerformed(ActionEvent e) {
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}

	// Added by @Bart @ 31-03-2003
	public AHAOutAttribute getAttributesListItem(Object key) {
		return (AHAOutAttribute) attributeMap.get(key);
	}

	// Added by @Bart @ 31-03-2003
	public DefaultListModel getAttributesListItems(DefaultListModel dataHolder) {

		dataHolder.clear();
		this.attributeMap.clear();

		try {
			for (Iterator i = concept.attributeList.iterator(); i.hasNext();) {
				AHAOutAttribute tmpAttribute = (AHAOutAttribute) i.next();
				dataHolder.addElement(tmpAttribute.name);
				attributeMap.put(tmpAttribute.name, tmpAttribute);
			}
		} catch (Exception e) {
			System.out
					.println("Attributes: getAttributesListItems: exception: "
							+ e.toString());
		}
		return dataHolder;
	}

}
