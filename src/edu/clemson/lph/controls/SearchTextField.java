package edu.clemson.lph.controls;
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
import javax.swing.JTextField;

import java.awt.event.*;
import java.util.*;

import edu.clemson.lph.dialogs.SearchDialog;

import java.awt.*;


@SuppressWarnings("serial")
public class SearchTextField extends JTextField {
//	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<SearchTextFieldListener> lListeners = null;
	private boolean bInSearch = false;
//	private boolean bChanged = false;
	private int deltaX = 0;
	private int deltaY = 0;
	/** Any dialog that returns type String from getSelectedKey() **/
	private SearchDialog<String> searchDialog = null;

	public SearchTextField() {
		init();
	}

	public SearchTextField(String p0) {
		super(p0);
		init();
	}

	private void init() {
		try {
			jbInit();
			// Temporary test code
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				this_keyPressed(e);
			}
			public void keyTyped(KeyEvent e) {
				//        e.consume();
			}
		});
		if( searchDialog != null ) {
			this.setToolTipText("CTRL-F To FIND and set value");
		}
		else {
			super.setText("");
			this.setToolTipText("");
		}
		
	}

	public boolean isInSearch() { return bInSearch; }
	
	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	public void setSearchDialog( SearchDialog<String> dlg ) {
		this.searchDialog = dlg;
	}
	
	public SearchDialog<String> getSearchDialog() {
		return searchDialog;
	}

	public void addListener( SearchTextFieldListener listener ) {
		if( lListeners == null ) lListeners = new ArrayList<SearchTextFieldListener>();
		lListeners.add( listener );
	}

	public String getDisplayText() {
		return super.getText();
	}


	void this_keyPressed(KeyEvent e) {
		// Could just look for numerical value of code == 70 but that might be less readable.
		if( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && "F".equals(KeyEvent.getKeyText(e.getKeyCode())) ) {
			// Search is just to complicated to generically outsource to the model.  
			// We assume a live db connection here.
			if( searchDialog != null ) {
				searchDialog.setDeltas(deltaX, deltaY);
				searchDialog.clear();
				searchDialog.setVisible(true);
				if (searchDialog.exitOK()) {
					String sKey = searchDialog.getSelectedKey();
					this.setText(sKey);
					// Don't bother to lookup again.
//					bChanged = false;
				}
				bInSearch = false;
			}
		}
		else if( e.getKeyCode() == KeyEvent.VK_DELETE ) {
			setText( null );
//			bChanged = true;
		}
//		else
//			bChanged = true;
	}

	public Window getWindowParent() {
		Container parent = this.getParent();
		Window wParent = null;
		while (! (parent instanceof Window) ) {
			if (parent == null) return wParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		wParent = (Window) parent;
		return wParent;
	}

}// End Class DBSearchTextField

