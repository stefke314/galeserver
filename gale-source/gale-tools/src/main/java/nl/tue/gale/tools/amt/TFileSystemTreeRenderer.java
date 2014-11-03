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
 * TFileSystemTreeRenderer.java
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

import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Type representing the renderer for a filesystem tree. The renderer can render
 * files of type File and TAMtFile for usage in AMt. For File objects, the
 * default system icon is retrieved. For TAMtFile icons, applications and files
 * ending with ".aha", ".gaf" and ".frm" are assigned special icons, where other
 * file or folders are given DefaultTreeCellRenderer icons.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 * @see File
 * @see TAMtFile
 * @see DefaultTreeCellRenderer
 */
public class TFileSystemTreeRenderer extends DefaultTreeCellRenderer {

	private FileSystemView fsv;
	private JFileChooser fc;
	private Icon fileicon;
	private Icon foldericon;
	private Icon appicon;
	private Icon ahafileicon;
	private Icon gaffileicon;
	private Icon frmfileicon;

	/**
	 * Constructor Provides support for rendering of both TAMtFile and File
	 * nodes
	 */
	public TFileSystemTreeRenderer() {
		super();

		// get filesystem
		String homedir = System.getProperty("user.home");
		fc = new JFileChooser(homedir);
		fsv = fc.getFileSystemView();

		setImages();
	}

	/**
	 * Constructor Provides support for files based on the <Code>AMtOnly</Code>
	 * parameter
	 * 
	 * @param AMtOnly
	 *            indicates which types of files need to be rendered with this
	 *            treerenderer. If true, it is able to render only nodes of type
	 *            TAMtfile, otherwise, it is able to handle both TAMtFile and
	 *            File nodes
	 */
	public TFileSystemTreeRenderer(boolean AMtOnly) {
		super();

		if (!AMtOnly) {
			// get filesystem
			String homedir = System.getProperty("user.home");
			fc = new JFileChooser(homedir);
			fsv = fc.getFileSystemView();
		}

		setImages();
	}

	/**
	 * Renders a node.
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		// System.out.println("Rendering node");

		if (obj instanceof File) {// render file
			return getFileRendererComponent(node);
		} else if (obj instanceof TAMtFile) {// render TAMtFile
			return getAMtFileRendererComponent(node);
		} else {
			System.out.println("Can not render node");
			System.out.println(node.getUserObject().getClass().toString());
			// System.out.println(node.getUserObject().toString());
			return this;
		}
	}

	/**
	 * Renders a node of type java.io.File
	 * 
	 * @param node
	 *            the node to render. UserObject of this node is of type
	 *            java.io.File
	 */
	private Component getFileRendererComponent(DefaultMutableTreeNode node) {
		File f = (File) node.getUserObject();

		try {
			if (!f.exists() || fsv == null)
				return this;
		} catch (Exception e) {
			return this;
		}

		// set Icon
		Icon icn = getIcon(f);
		if (icn != null)
			setIcon(icn);

		// set Text
		setText(fsv.getSystemDisplayName(f));

		// set ToolTipText
		setToolTipText("<HTML>"
				+ "Name: "
				+ f.getName()
				+ "<br>"
				+ ((!f.isDirectory()) ? "Size: "
						+ getHumanPresentableSize(f.length()) + "<br>" : "")
				+ "Last Modified: " + new Date(f.lastModified()) + "</HTML>");

		return this;
	}

	/**
	 * Renders a node of type nl.tue.gale.AMt.TAMtFile
	 * 
	 * @param node
	 *            the node to render. UserObject of this node is of type
	 *            {@link TAMtFile}
	 */
	private Component getAMtFileRendererComponent(DefaultMutableTreeNode node) {
		TAMtFile af = (TAMtFile) node.getUserObject();

		// set Icon
		setIcon(getIcon(af));

		// set Text
		setText(af.getName());

		// set ToolTipText
		String s = "";
		if (af.isApp())
			s = "AHA! Application";
		else if (!af.isDirectory()) {
			String name = af.getName();
			if (name.endsWith(".aha"))
				s = "Concept Editor File";
			if (name.endsWith(".gaf"))
				s = "Graph Author File";
			if (name.endsWith(".frm"))
				s = "Form Editor File";
			if (name.endsWith(".test"))
				s = "Test Editor File";
		}

		setToolTipText("<HTML>"
				+ ((!s.equals("")) ? "Type: " + s + "<br>" : "")
				+ "Name: "
				+ af.getName()
				+ "<br>"
				+ ((!af.isDirectory()) ? "Size: "
						+ getHumanPresentableSize(af.getSize()) + "<br>" : "")
				+ ((!af.isApp()) ? "Last Modified: " + new Date(af.getDate())
						: "") + "</HTML>");

		return this;
	}

	/**
	 * Retrieves the system icon for a certain file.
	 * 
	 * @param f
	 *            the file to retrieve the icon for
	 * @return the icon for the requested file
	 */
	public Icon getIcon(File f) {
		return fsv.getSystemIcon(f);
	}

	/**
	 * Retrieves the icon for a TAMtFile object. Icon returned is based on the
	 * type of file. Applications and files ending with ".aha", ".gaf" and
	 * ".frm" are assigned special icons, where other file or folders are given
	 * DefaultTreeCellRenderer icons.
	 * 
	 * @param af
	 *            the file to retrieve the icon for
	 * @return the icon for the requested file
	 */
	public Icon getIcon(TAMtFile af) {
		if (af.isDirectory()) {// dir or application
			if (af.isApp())
				return appicon;
			else
				return foldericon;
		} else {// file
			String name = af.getName();
			if (name.endsWith(".aha"))
				return ahafileicon;
			else if (name.endsWith(".gaf"))
				return gaffileicon;
			else if (name.endsWith(".frm"))
				return frmfileicon;
			else
				return fileicon;
		}
	}

	/**
	 * Creates a human readable string representing the size of a file.
	 * 
	 * @param size
	 *            the size of a file
	 * @return the human readable string representing <code>size</code>
	 */
	private String getHumanPresentableSize(long asize) {

		String result = "";
		String[] names = { "bytes", "kB", "MB", "GB", "TB" };

		double d = new Long(asize).doubleValue();
		int i = 0;

		while (d > 1024 || i >= names.length) {
			d /= 1024;
			i++;
		}

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

		result += "" + nf.format(d);

		// add sizename
		result += " " + names[Math.min(i, names.length - 1)];

		return result;
	}

	/**
	 * Sets all images used in this renderer
	 */
	private void setImages() {
		System.out.println("--- file: " + AMtc.PICPATH + "file.png");
		fileicon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "file.png"));
		foldericon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "folder.png"));
		appicon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "app.png"));
		ahafileicon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "ahafile.png"));
		gaffileicon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "gaffile.png"));
		frmfileicon = new javax.swing.ImageIcon(getClass().getResource(
				AMtc.PICPATH + "frmfile.png"));
	}

};