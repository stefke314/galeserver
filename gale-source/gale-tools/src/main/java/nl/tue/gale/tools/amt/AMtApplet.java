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
 * AMtApplet.java
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Entry class for applet version of AMt. Starting this applet will result in
 * the creation of a JFrame containing the AMt.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class AMtApplet extends JApplet {

	private static JFrame frmMain; // the main frame
	private AMtClientGUI gui; // the user interface object

	/**
	 * Default Constructor. Creates the main frame.
	 */
	public AMtApplet() {
		frmMain = new JFrame(AMtc.APPTITLE);

		// make the close button work
		frmMain.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		// center and resize window
		int w = AMtc.FRAMEWIDTH, h = AMtc.FRAMEHEIGHT;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frmMain.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2
				- h / 2);
		frmMain.setSize(w, h);
	}

	public void createGUI() {
		URL url = getCodeBase();
		String path = url.getPath();
		String temp = path.substring(1, path.length());
		int index = temp.indexOf("/");
		index++;
		String contextpath = path.substring(0, index);

		try {
			AMtc.SERVERURL = url;
			AMtc.SERVLETPATH = new URL(url.getProtocol() + "://"
					+ url.getHost() + ":" + url.getPort() + contextpath + "/"
					+ AMtc.SERVLETNAME).toString();

		} catch (Exception e) {
			System.out.println("AMtApplet.init(): Could not creat servletURL");
		}

		gui = new AMtClientGUI(frmMain, getParameter("sessionid"));
		frmMain.getContentPane().add(gui);

		JMenuBar mb = gui.getMenuBar();
		frmMain.setJMenuBar(mb);

		frmMain.pack();
		frmMain.setVisible(true);
		// make application exit work

		// get file menu
		JMenu mn = gui.getMenuBar().getMenu(0);
		// get exit menuitem
		JMenuItem mni = mn.getItem(mn.getItemCount() - 1);
		mni.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exit();
			}
		});

		// make sure that frmMain stays on top
		// Browser will always give the applet the focus after start(), so
		// catch this first focus event and give focus to frmMain
		addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent evt) {
				if (evt.getID() == evt.FOCUS_GAINED) {
					System.out.println("switching focus to frmMain");
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {// necessary GUI-updates
							frmMain.setVisible(true);
							frmMain.requestFocus();
							setFocusable(false);
						}
					});
				}
			}
		});

		setVisible(false);
		setFocusable(true);
	}

	/**
	 * Called by the browser or applet viewer to inform this applet that it has
	 * been loaded into the system. It is always called before the first time
	 * that the start method is called. Displays the main frame.
	 */
	public void init() {
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createGUI();
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't successfully complete");
			e.printStackTrace();
		}
	}

	/**
	 * Called by the browser or applet viewer to inform this applet that it
	 * should stop its execution. It is called when the Web page that contains
	 * this applet has been replaced by another page, and also just before the
	 * applet is to be destroyed.
	 */
	public void stop() {
		exit();
	}

	/**
	 * Exits the application (disposes frmMain)
	 */
	private void exit() {
		gui.exitApp();
		frmMain.setVisible(false);
		frmMain.dispose();
	}

};