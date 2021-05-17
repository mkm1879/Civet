package edu.clemson.lph.civet.addons;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FilenameFilter;

/**
 * <p>Title: SCPremID</p>
 * <p>Description: SC Premises ID Database</p>
 * <p>Copyright: Copyright (c) 2004-2005</p>
 * <p>Company: Clemson Livestock Poultry Health</p>
 * @author Michael K Martin
 * @version 0.002 Alpha
 */

public class PDFFilter extends javax.swing.filechooser.FileFilter implements FilenameFilter {

  public PDFFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
        return true;
    }
    String sFileName = f.getName();
    if( sFileName.toUpperCase().endsWith("PDF") ) {
      return true;
    }
    else {
      return false;
    }
  }

    //The description of this filter
    public String getDescription() {
        return "Just PDF Files";
    }

	@Override
	public boolean accept(File arg0, String arg1) {
	    if( arg1.toUpperCase().endsWith("PDF") ) {
	        return true;
	    }
	    else {
	    	return false;
	    }
	}
}

