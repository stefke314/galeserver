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
 * CheckCycle.java
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

import java.util.Iterator;
import java.util.LinkedList;

public class CheckCycle {
	public boolean cycle;
	public String startCycle = "";
	public LinkedList currentPath = new LinkedList();

	public CheckCycle() {
		cycle = false;
	}

	public void FindCycle() {
		LinkedList groupedRelations = AuthorSTATIC.relaha
				.SplitConceptRelations();

		for (Iterator i = groupedRelations.iterator(); i.hasNext();) {
			LinkedList groupedRel = (LinkedList) i.next();

			if (cycle == false) {
				this.CheckRelation(groupedRel);
			}
		}
	}

	public void CheckRelation(LinkedList crelList) {
		// if de crt is not acyclic then return
		RELConceptRelation crel = (RELConceptRelation) crelList.getFirst();
		String relType = crel.relationType.trim();

		for (Iterator j = AuthorSTATIC.CRTList.iterator(); j.hasNext();) {
			CRTConceptRelationType relConceptType = (CRTConceptRelationType) j
					.next();

			if (relConceptType.name.equals(relType)) {

				if (relConceptType.properties.acyclic.booleanValue() == false) {

					return;
				}
			}
		}

		for (Iterator i = crelList.iterator(); i.hasNext();) {
			crel = (RELConceptRelation) i.next();

			String root = crel.sourceConceptName;
			this.currentPath = new LinkedList();
			currentPath.add(root);

			String destination = crel.destinationConceptName;
			this.currentPath.add(destination);

			LinkedList destinationList = this.findNext(crelList, destination);

			while (!destinationList.isEmpty()) {
				for (Iterator j = destinationList.iterator(); j.hasNext();) {
					String element = (String) j.next();
					this.currentPath.add(element);

					if (this.TestForCycle(this.currentPath, element)) {

						return;
					}
				}

				if (findElement(destinationList, root)) {
					this.cycle = true;
					this.startCycle = root;

					return;
				}

				try {
					System.in.read();
				} catch (Exception e) {
					System.out.println("in exception");
				}

			}
		}
	}

	public boolean TestForCycle(LinkedList clist, String lastElement) {
		int n = 0;

		for (Iterator i = clist.iterator(); i.hasNext();) {
			String element = (String) i.next();

			if (element.equals(lastElement)) {
				n++;
			}
		}

		if (n > 1) {
			return true;
		}

		return false;
	}

	public boolean findElement(LinkedList destList, String element) {
		for (Iterator i = destList.iterator(); i.hasNext();) {
			String concept = (String) i.next();

			if (concept.equals(element)) {

				return true;
			}
		}

		return false;
	}

	public LinkedList findNext(LinkedList crelList, String element) {
		LinkedList returnList = new LinkedList();

		for (Iterator i = crelList.iterator(); i.hasNext();) {
			RELConceptRelation crel = (RELConceptRelation) i.next();

			if (element.equals(crel.sourceConceptName)) {
				returnList.add(crel.destinationConceptName);
			}
		}

		return returnList;
	}

}