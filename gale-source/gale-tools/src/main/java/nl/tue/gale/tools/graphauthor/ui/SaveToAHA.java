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
 * SaveToAHA.java
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

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.tue.gale.tools.graphauthor.data.AHAOutConcept;
import nl.tue.gale.tools.graphauthor.data.AuthorSTATIC;
import nl.tue.gale.tools.graphauthor.data.AuthorToAha;
import nl.tue.gale.tools.graphauthor.data.ConceptTemplate;
import nl.tue.gale.tools.graphauthor.data.RELConceptRelation;
import nl.tue.gale.tools.graphauthor.data.ReadAuthorREL;
import nl.tue.gale.tools.graphauthor.data.WriteAHAXML;

/**
 * This class saves the graph in AHA format.
 * 
 */
public class SaveToAHA {
	public URL home;
	public boolean sysOutput;

	public SaveToAHA(URL base, boolean noOutput) {
		sysOutput = noOutput;
		home = base;
		// filename is projectname + extention
		String fname = GraphAuthor.projectName + ".gaf";
		this.ReadAuthor(home, fname);
	}

	public void writeElementRelations(DefaultMutableTreeNode element) {
		if (element == null) {
			return;
		}

		String course = GraphAuthor.projectName.trim();

		for (Enumeration i = element.children(); i.hasMoreElements();) {
			RELConceptRelation trel = new RELConceptRelation();
			DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) i
					.nextElement();
			trel.destinationConceptName = (course + "." + element.toString())
					.trim();
			trel.sourceConceptName = (course + "." + tnode.toString()).trim();
			trel.relationType = "tree";
			trel.label = "";
			this.writeElementRelations(tnode);
			AuthorSTATIC.relaha.conceptRelationList.add(trel);

		}
	}

	public void WriteTreeRelations() {
		// pre: GraphAutho.sharedConceptree must have the current tree
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) GraphAuthor.sharedConceptTree
				.getModel().getRoot();
		// for (Enumeration i = root.children(); i.hasMoreElements();) {
		// this.writeElementRelations((DefaultMutableTreeNode) i.nextElement());
		// }
		// fixed by Paul De Bra and Natalia Stash, 29-07-2003
		this.writeElementRelations(root);
	}

	public void WriteTemplateRelations() {
		for (Iterator i = GraphAuthor.conceptList.iterator(); i.hasNext();) {
			AHAOutConcept concept = (AHAOutConcept) i.next();

			for (Iterator j = AuthorSTATIC.templateList.iterator(); j.hasNext();) {
				ConceptTemplate cTemp = (ConceptTemplate) j.next();

				if (concept.template.equals(cTemp.name)) {
					for (Iterator k = cTemp.conceptRelations.entrySet()
							.iterator(); k.hasNext();) {
						Map.Entry m = (Map.Entry) k.next();
						RELConceptRelation temprel = new RELConceptRelation();
						temprel.sourceConceptName = GraphAuthor.projectName
								+ "." + concept.name;
						temprel.destinationConceptName = GraphAuthor.projectName
								+ "." + concept.name;
						temprel.relationType = ((String) m.getKey()).trim();
						temprel.label = ((String) m.getValue()).trim();
						AuthorSTATIC.relaha.conceptRelationList.add(temprel);
						// System.out.println("added template relation: " +
						// temprel.relationType);
					}

				}

			}

		}

	}

	public void ReadAuthor(URL home, String fileName) {

		ReadAuthorREL rrel = new ReadAuthorREL(home, fileName,
				GraphAuthor.projectName);
		// convert the emplate relation into author relations
		this.WriteTemplateRelations();
		// read the tree view as relations
		this.WriteTreeRelations();

		/*
		 * // read the concept relation types RELConceptRelation crel = null;
		 * try { crel = (RELConceptRelation)
		 * AuthorSTATIC.relaha.conceptRelationList.getFirst(); } catch
		 * (Exception e) { System.out.println("exception crel empty!"); return;
		 * }
		 */

		AuthorSTATIC.conceptList = GraphAuthor.conceptList;
		AuthorSTATIC.projectName = GraphAuthor.projectName;
		// change the author relation to aha code
		AuthorToAha outaha = new AuthorToAha();
		String saveToAha = GraphAuthor.projectName + ".aha";
		// saves it to the server
		WriteAHAXML waha = new WriteAHAXML(AuthorSTATIC.ahaOut, home,
				saveToAha, GraphAuthor.projectName, sysOutput);
	}

}