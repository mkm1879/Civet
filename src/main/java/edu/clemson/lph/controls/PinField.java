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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.Document;

import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.PremCheckSum;

import java.awt.Container;
import java.awt.event.FocusEvent;


public class PinField extends JTextField{
	private static final long serialVersionUID = 1L;

	public PinField() {
		init();
	}

	public PinField(int p0) {
		super(p0);
		init();
	}

	public PinField(String p0) {
		super(p0);
		init();
	}

	public PinField(String p0, int p1) {
		super(p0, p1);
		init();
	}

	public PinField(Document p0, String p1, int p2) {
		super(p0, p1, p2);
		init();
	}

	public void setText( String sPin ) {
		if( sPin == null || sPin.trim().length() == 0 ) {
			super.setText(sPin);
			return;
		}
		String sUpperPin = sPin.toUpperCase();
		boolean bValid = false;
		try {
			if( PremCheckSum.isValid(sUpperPin) ) {
				bValid = true;
			}
		} catch (Exception es) {
			//
		}
		if( !bValid ) {
			JDialog dParent = getDialogParent();
			if( dParent != null )
				MessageDialog.showMessage(getDialogParent(), 
						"Premises ID Error", sUpperPin + " is not a valid Premises ID", MessageDialog.OK_ONLY);
			else
				MessageDialog.showMessage(getFrameParent(), 
						"Premises ID Error", sUpperPin + " is not a valid Premises ID", MessageDialog.OK_ONLY);
		}
		super.setText(sUpperPin);
	}

	private void init() {
		addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String sPin = getText();
				if( sPin == null || sPin.trim().length() == 0 )
					return;
				String sUpperPin = sPin.toUpperCase();
				PinField.super.setText(sUpperPin);
				boolean bValid = false;
				try {
					if( PremCheckSum.isValid(sUpperPin) ) {
						bValid = true;
					}
				} catch (Exception ex) {
				}
				if( !bValid ) {
					JDialog dParent = getDialogParent();
					if( dParent != null )
						MessageDialog.showMessage(getDialogParent(), 
								"Premises ID Error", sUpperPin + " is not a valid Premises ID", MessageDialog.OK_ONLY);
					else
						MessageDialog.showMessage(getFrameParent(), 
								"Premises ID Error", sUpperPin + " is not a valid Premises ID", MessageDialog.OK_ONLY);
				}
			}
		});
	}

	  public JFrame getFrameParent() {
	    Container parent = this.getParent();
	    JFrame fParent = null;
	    while (! (parent instanceof JFrame) && ! (parent instanceof JDialog)) {
	      if (parent == null)return fParent; // I give up.  Where am I?
	      parent = parent.getParent();
	    }
	    if (parent instanceof JFrame) fParent = (JFrame) parent;
	    return fParent;
	  }

	  public JDialog getDialogParent() {
	    Container parent = this.getParent();
	    JDialog dParent = null;
	    while (! (parent instanceof JFrame) && ! (parent instanceof JDialog)) {
	      if (parent == null)return dParent; // I give up.  Where am I?
	      parent = parent.getParent();
	    }
	    if (parent instanceof JDialog) dParent = (JDialog) parent;
	    return dParent;
	  }

} // end class DBPhoneField

