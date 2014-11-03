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
 * AMtc.java
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

import java.net.URL;

import nl.tue.gale.tools.AHAStatic;

/**
 * Contains all constant declarations for the AMt Application.
 * 
 * @author T.J. Dekker
 * @version 1.0.0
 */
public class AMtc {

	// *** General AMt Constants ***//

	public static final String VERSION = "1.0";
	public static final String APPTITLE = "AHA! Application Management Tool";

	public static final int PANELWIDTH = 720;
	public static final int PANELHEIGHT = 520;

	public static final int FRAMEWIDTH = PANELWIDTH + 8;
	public static final int FRAMEHEIGHT = PANELHEIGHT + 54;

	public static final String PICPATH = "/nl/tue/gale/tools/amt/pics/";

	public static final String AHATEMPLATEPATH = AHAStatic.AUTHORFILESPATH
			+ "template.aha";
	public static final String GAFTEMPLATEPATH = AHAStatic.AUTHORFILESPATH
			+ "template.gaf";

	// *** Server URLs: to be set when starting AMt (get from codebase) ***//

	public static final String SERVLETNAME = "AMtServer";
	public static URL SERVERURL = null; // url to the AHA! server
	public static String SERVLETPATH = null; // path to the AMtServlet servlet

	// *** Buffer sizes (in bytes) for file transfer ***//

	public static final int CLIENT_BUFFER_SIZE = 128000;
	public static final int SERVER_BUFFER_SIZE = 128000;

	// *** Sleep times for upload and downloads. The sleeptime is used when
	// waiting
	// for data from the server. Low sleeptimes increase CPU-load, high
	// sleeptimes
	// slow down the transfer. ***//

	public static final int UPLOAD_SLEEP_TIME = 200;
	public static final int DOWNLOAD_SLEEP_TIME = 200;

	// *** Codes for operations (i.e. used in AMtClientGUI TransferThread) ***//

	public static final int OP_UPLOAD = 0;
	public static final int OP_DOWNLOAD = 1;
	public static final int OP_COPY_LOCAL = 2;
	public static final int OP_COPY_REMOTE = 3;

	// *** Constants for the overwrite Dialog buttons ***//

	public static final String[] OWBUTTONS = { "Yes to All", "Yes", "No",
			"No to All", "Cancel" };
	public static final int YES_TO_ALL = 0;
	public static final int YES = 1;
	public static final int NO = 2;
	public static final int NO_TO_ALL = 3;
	public static final int CANCEL = 4;

	// *** Constants for treemodes of remote tree panel in AMtClientGUI ***//

	public static final int APP_FILES_MODE = 0; // lists all application folders
												// of an author
	public static final int AUTHOR_FILES_MODE = 1; // lists all files in the
													// authorfiles folder of an
													// author

	// *** Constants for error codes ***//

	public static final int NO_ERRORS = -3;
	public static final int ERROR_NOT_IMPLEMENTED = -2;
	public static final int ERROR_USER_ABORTED = -1;
	public static final int ERROR = 0;
	public static final int ERROR_ILLEGAL_CHARS_IN_FILENAME = 1;
	public static final int ERROR_FOLDER_ALREADY_EXISTS = 2;
	public static final int ERROR_COULD_NOT_CREATE_DIR = 3;
	public static final int ERROR_COULD_NOT_CREATE_FILE = 4;
	public static final int ERROR_COULD_NOT_DELETE_FILE = 6;
	public static final int ERROR_INVALID_PATH = 7;
	public static final int ERROR_LOGIN = 8;
	public static final int ERROR_NO_SUCH_REMOTE_DIR = 9;
	public static final int ERROR_CAN_NOT_UPLOAD_FILES_TO_ROOT = 10;
	public static final int ERROR_NO_SUCH_LOCAL_DIR = 11;
	public static final int ERROR_COULD_NOT_DELETE_DIR = 12;
	public static final int ERROR_NOT_CONNECTED = 13;
	public static final int ERROR_APP_ALREADY_EXISTS = 14;
	public static final int ERROR_NO_SUCH_REMOTE_APP = 15;
	public static final int ERROR_COULD_NOT_OVERWRITE_FILE = 16;
	public static final int ERROR_COPY_TARGET_IS_SOURCE = 17;
	public static final int ERROR_FILE_ALREADY_EXISTS = 18;
	public static final int ERROR_COULD_NOT_RENAME_FILE = 19;
	public static final int ERROR_COULD_NOT_CREATE_APP_DIR = 20;
	public static final int ERROR_COULD_NOT_RENAME_APP_DIR = 21;
	public static final int ERROR_PASSWORDS_DO_NOT_MATCH = 22;
	public static final int ERROR_INVALID_APP_NAME = 23;

	/**
	 * Retrieves the human presentable error message for an error code.
	 * 
	 * @param ec
	 *            The error code
	 * @return The human presentable error message of ec or "Unknown error code"
	 *         if the error code was unknown.
	 */
	public static String getErrorMsg(int ec) {
		String s = "";// "An error occured:\n";
		switch (ec) {
		case NO_ERRORS:
			return s + "There were no errors.";
		case ERROR_NOT_IMPLEMENTED:
			return s + "This function has not been implemented.";
		case ERROR_USER_ABORTED:
			return s + "User aborted operation.";
		case ERROR:
			return s + "An unknown error occured.";
		case ERROR_ILLEGAL_CHARS_IN_FILENAME:
			return s + "The following characters are not allowed in file "
					+ "names:\n" + "/ \\ : * ? \" < > |";
		case ERROR_FOLDER_ALREADY_EXISTS:
			return s + "The folder already exists.";
		case ERROR_COULD_NOT_CREATE_DIR:
			return s + "Could not create folder.";
		case ERROR_COULD_NOT_CREATE_FILE:
			return s + "Could not create file.";
		case ERROR_INVALID_PATH:
			return s + "Invalid Path.";
		case ERROR_COULD_NOT_DELETE_FILE:
			return s + "There were errors during the deletion process.\n"
					+ "Some files may not have been deleted.";
		case ERROR_NO_SUCH_LOCAL_DIR:
			return s + "Local folder does not exist.";
		case ERROR_NO_SUCH_REMOTE_DIR:
			return s + "Remote folder does not exist.";
		case ERROR_CAN_NOT_UPLOAD_FILES_TO_ROOT:
			return s
					+ "Can not upload files to remote root.\nPlease select an "
					+ "application to upload to.";
		case ERROR_LOGIN:
			return s + "Wrong authorname or password.\nAccess denied.";
		case ERROR_COULD_NOT_DELETE_DIR:
			return s + "Could not delete folder.";
		case ERROR_NOT_CONNECTED:
			return s + "Could not connect to remote server.";
		case ERROR_APP_ALREADY_EXISTS:
			return s + "The application is already registered for an author.";
		case ERROR_NO_SUCH_REMOTE_APP:
			return s + "Application does not exist on server.";
		case ERROR_COULD_NOT_OVERWRITE_FILE:
			return s
					+ "Could not overwrite file.\nCheck if the file is in use.";
		case ERROR_FILE_ALREADY_EXISTS:
			return s + "The File already exists.";
		case ERROR_COULD_NOT_RENAME_FILE:
			return s + "Could not rename file.";
		case ERROR_COPY_TARGET_IS_SOURCE:
			return s
					+ "Could not copy. The source and destination files are the same.";
		case ERROR_COULD_NOT_CREATE_APP_DIR:
			return s + "Could not create application directory. The directory "
					+ "could have been created by another author.";
		case ERROR_COULD_NOT_RENAME_APP_DIR:
			return s + "Could not rename application directory.";
		case ERROR_PASSWORDS_DO_NOT_MATCH:
			return s + "The passwords do not match.";
		case ERROR_INVALID_APP_NAME:
			return s
					+ "Invalid application name. Application name must be an identifier.";
		default:
			return s + "Unknown error code: " + ec;
		}
	}

	// *** Message string constants (i.e. used for dialog boxes) ***//

	public static final String MESSAGE_ABOUTBOX = APPTITLE + "\nVersion "
			+ VERSION + "\nCreated by TJ Dekker 2004.";
	public static final String MESSAGE_ACCESS_DENIED = "Access Denied.";
	public static final String MESSAGE_DEL_CONFIRMATION = "Are you sure you want to delete all selected\n files and folders?";
	public static final String MESSAGE_HEADER_CONFIRM = "Confirm";
	public static final String MESSAGE_HEADER_ERROR = "Error";
	public static final String MESSAGE_HEADER_INFO = "Info";
	public static final String MESSAGE_HEADER_INPUT = "Input";
	public static final String MESSAGE_ADD_AUTHOR_PROMPT = "Enter the loginname of the new author.\n"
			+ "This can be an already existing user or a new user.";
	public static final String MESSAGE_NEW_FOLDER_PROMPT = "Enter the name for the new folder.";
	public static final String MESSAGE_NEW_APP_PROMPT = "Enter the name for the new application.";
	public static final String MESSAGE_UPLOAD_ERROR = "There were errors in uploading selected files.\n"
			+ "Not all files may have been uploaded correctly.";
	public static final String MESSAGE_DOWNLOAD_ERROR = "There were errors in"
			+ " downloading selected files.\n"
			+ "Not all files may have been downloaded correctly.";
	public static final String MESSAGE_SAVE_SUCCESSFUL = "Save successful.";

	/**
	 * Retrieves a message that can be displayed to the user prompting the user
	 * if he or she would like to overwrite a file.
	 * 
	 * @param s
	 *            The name of the file to overwrite.
	 * @return The message to be displayed.
	 */
	public static String getMsgFileOverwrite(String s) {
		return "The file already exists: " + s + ".\n"
				+ "Would you like to replace the existing file?";
	}

	/**
	 * Retrieves a message that can be displayed to the user prompting the user
	 * if he or she would like to overwrite a directory and all its contents.
	 * 
	 * @param s
	 *            The name of the directory to overwrite.
	 * @return The message to be displayed.
	 */
	public static String getMsgDirOverwrite(String s) {
		return "The folder already exists: " + s + ".\n"
				+ "Would you like to replace the existing folder?";
	}
}