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
 * RELConceptRelations.java
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
package nl.tue.gale.tools.graphauthor.data;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * RELconceptRelations data class to store the conceptRelationList and the
 * divideInformation.
 * 
 */
public class RELConceptRelations {
	public LinkedList conceptRelationList;
	public Hashtable divideInformation;

	/**
	 * default constructor.
	 */
	public RELConceptRelations() {
		conceptRelationList = new LinkedList();
		divideInformation = new Hashtable();
	}

	public ConceptDivideInfo GetTreeInfo(String conceptName) {
		LinkedList llInfo = new LinkedList();
		CRTConceptRelationType crt = AuthorSTATIC.trel;
		ConceptDivideInfo cDivInfo = new ConceptDivideInfo();
		String crtName = crt.name.trim();
		int sourceCount = 0;
		int destinationCount = 0;

		for (Iterator j = AuthorSTATIC.relaha.conceptRelationList.iterator(); j
				.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) j.next();

			if (crel.relationType.equals("tree")) {
				if (crel.sourceConceptName.trim().equals(conceptName)) {
					sourceCount = sourceCount + 1;
				} else if (crel.destinationConceptName.trim().equals(
						conceptName)) {
					destinationCount = destinationCount + 1;
				}
			}
		}

		cDivInfo.crtName = "tree";
		cDivInfo.source = sourceCount;
		cDivInfo.destination = destinationCount;
		llInfo.add(cDivInfo);

		return cDivInfo;
	}

	public void AddDivideInformation() {
		Hashtable conceptNames = this.GetConceptNames();

		for (Enumeration e = conceptNames.elements(); e.hasMoreElements();) {
			String conceptName = ((String) e.nextElement()).trim();
			LinkedList llInfo = new LinkedList();

			// the tree crt
			for (Iterator i = AuthorSTATIC.CRTList.iterator(); i.hasNext();) {
				CRTConceptRelationType crt = (CRTConceptRelationType) i.next();
				ConceptDivideInfo cDivInfo = new ConceptDivideInfo();
				String crtName = crt.name.trim();
				int sourceCount = 0;
				int destinationCount = 0;
				for (Iterator j = AuthorSTATIC.relaha.conceptRelationList
						.iterator(); j.hasNext();) {
					RELConceptRelation crel = (RELConceptRelation) j.next();
					if (crel.relationType.trim().equals(crtName)) {
						if (crel.sourceConceptName.trim().equals(conceptName)) {
							sourceCount = sourceCount + 1;
						} else if (crel.destinationConceptName.trim().equals(
								conceptName)) {
							destinationCount = destinationCount + 1;
						}
					}
				}

				cDivInfo.crtName = crtName;
				cDivInfo.source = sourceCount;
				cDivInfo.destination = destinationCount;
				llInfo.add(cDivInfo);
			}

			llInfo.add(this.GetTreeInfo(conceptName));
			this.divideInformation.put(conceptName, llInfo);
		}
	}

	public Hashtable GetConceptNames() {
		Hashtable conceptNames = new Hashtable();

		for (Iterator i = conceptRelationList.iterator(); i.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) i.next();
			conceptNames.put(crel.sourceConceptName.trim(),
					crel.sourceConceptName.trim());
			conceptNames.put(crel.destinationConceptName.trim(),
					crel.destinationConceptName.trim());
		}

		return conceptNames;
	}

	public LinkedList SplitConceptRelations() {
		if (conceptRelationList.isEmpty()) {
			return new LinkedList();
		}

		this.SortRelConceptRelations();

		LinkedList returnlist = new LinkedList();
		LinkedList sublist = new LinkedList();
		String relationname;

		RELConceptRelation conceptrel = (RELConceptRelation) conceptRelationList
				.getFirst();
		relationname = conceptrel.relationType;

		for (Iterator i = conceptRelationList.iterator(); i.hasNext();) {
			conceptrel = (RELConceptRelation) i.next();

			if (conceptrel.relationType.equals(relationname)) {
				sublist.add(conceptrel);
			} else {
				returnlist.add(sublist);
				sublist = new LinkedList();
				relationname = conceptrel.relationType;
				sublist.add(conceptrel);
			}
		}

		// add last sublist to returnlist
		returnlist.add(sublist);

		return returnlist;
	}

	public void SortRelConceptRelations() {
		// if (conceptRelationList.isEmpty()) { return; }
		if (conceptRelationList.size() < 2) {
			return;
		}

		LinkedList sortedlist = new LinkedList();
		String relationname;

		while (conceptRelationList.isEmpty() == false) {
			RELConceptRelation conceptrel = (RELConceptRelation) conceptRelationList
					.getFirst();
			relationname = conceptrel.relationType;

			for (Iterator i = conceptRelationList.iterator(); i.hasNext();) {
				conceptrel = (RELConceptRelation) i.next();

				if (conceptrel.relationType.equals(relationname)) {
					sortedlist.add(conceptrel);
					i.remove();
				}
			}
		}

		conceptRelationList = sortedlist;
	}
}