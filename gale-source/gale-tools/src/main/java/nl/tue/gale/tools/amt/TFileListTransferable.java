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
 * TFileListTransferable.java
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
package nl.tue.gale.tools.amt;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * Support class for enabling the drag-transfer of items from a JTree for items
 * of type File and TAMtFile
 */
public class TFileListTransferable implements Transferable {

	/**
	 * Default Constructor Sets the supported dataflavors as
	 * DataFlavor.javaFileListFlavor and TAMtFileListFlavor
	 */
	public TFileListTransferable() {
		FList = null;
		FAMtArray = null;
		flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor,
				TAMtFileListFlavor };
	}

	/**
	 * Constructor Sets the supported flavors as DataFlavor.javaFileListFlavor
	 * 
	 * @param lst
	 *            the filelist to store in this transferable
	 */
	public TFileListTransferable(java.util.List lst) {
		FList = lst;
		FAMtArray = null;
		flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor };
	}

	/**
	 * Constructor Sets the supported flavors as TAMtFileListFlavor
	 * 
	 * @param arr
	 *            the array of TAMtFiles to store in this transferable
	 */
	public TFileListTransferable(TAMtFile[] arr) {
		FAMtArray = arr;
		FList = null;
		flavors = new DataFlavor[] { DataFlavor.javaFileListFlavor,
				TAMtFileListFlavor };
	}

	/**
	 * Retrieves all dataflavors that this transferable supports
	 * 
	 * @return all supported dataflavors
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/**
	 * Requests if a certain dataflavor is supported by this transferable
	 * 
	 * @param fl
	 *            a dataflavor
	 * @return <Code>true</Code> if and only if <Code>fl</Code> is supported
	 */
	public boolean isDataFlavorSupported(DataFlavor fl) {
		for (int i = 0; i < flavors.length; i++) {
			if (fl.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the data contained in this transferable, based on the requested
	 * dataflavor.
	 * 
	 * @param fl
	 *            the dataflavor to retrieve the data for
	 * @return the data contained in this transferable, of the type requested by
	 *         <Code>fl</Code>.
	 */
	public Object getTransferData(DataFlavor fl) {
		// System.out.println("getTransferData");
		if (!isDataFlavorSupported(fl)) {
			return null;
		} else if (fl.equals(DataFlavor.javaFileListFlavor)) {
			// System.out.println("getTransferData: filelist");
			if (FList != null) {
				// System.out.println("Flist not null");
				return FList;
			} else {
				// System.out.println("FList null");
				// todo: add support for downloading AMtFiles
				return null;
			}
		} else if (fl.equals(TAMtFileListFlavor)) {
			// System.out.println("getTransferData: amtfilelist");
			return FAMtArray;
		}
		return null;
	}

	public static final DataFlavor TAMtFileListFlavor = new DataFlavor(
			TAMtFile[].class, "AMtFile");

	private List FList; // javaFileListFlavor data
	private TAMtFile[] FAMtArray; // TAMtFileListFlavor data
	private DataFlavor[] flavors = null; // all supported dataflavors

}