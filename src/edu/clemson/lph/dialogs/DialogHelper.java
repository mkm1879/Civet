package edu.clemson.lph.dialogs;
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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

public class DialogHelper {

	
	  public static void center( Window window ) {
		  center( window, 0, 0 );
	  }
		 
	  public static void center( Window window, int deltaX, int deltaY ) {
		    //Center the window
			boolean bSmall = false;
		    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		    Dimension frameSize = window.getSize();
		    if( frameSize.height > screenSize.height ) {
		    	frameSize.height = screenSize.height;
		    	bSmall = true;
		    }
		    if( frameSize.width > screenSize.width ) {
		    	frameSize.width = screenSize.width;
		    	bSmall = true;
		    }
		    if( bSmall ) {
		    	window.setLocation( (screenSize.width - frameSize.width) / 2, 0);
		    }
		    else {
		    	window.setLocation( deltaX + (screenSize.width - frameSize.width) / 2,
		    			deltaY + (screenSize.height - frameSize.height) / 2);
		    }
		  }

}
