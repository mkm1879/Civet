package edu.clemson.lph.controls;
import javax.swing.JDialog;
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
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import edu.clemson.lph.dialogs.MessageDialog;

import java.awt.event.FocusEvent;

public class PhoneField extends JTextField{
	private static final long serialVersionUID = 1L;
	private boolean bTenDigits = false;

	public PhoneField() {
		init();
	}
	
	public PhoneField( boolean bTenDigits ) {
		super();
		this.bTenDigits = bTenDigits;
		init();
	}

	public PhoneField(int p0) {
		super(p0);
		init();
	}

	public PhoneField(String p0) {
		super(p0);
		init();
	}

	public PhoneField(String p0, int p1) {
		super(p0, p1);
		init();
	}

	public PhoneField(Document p0, String p1, int p2) {
		super(p0, p1, p2);
		init();
	}

	public void setText( String sText ) {
		String sFormatText = formatPhone(sText);
		super.setText(sFormatText);
	}

	private void init() {
		addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String sPhone = getText();
				int iLenPhoneDigits = formatDigitsOnly(sPhone).trim().length();
				if( sPhone.trim().length() > 0 ) {
					if( bTenDigits && iLenPhoneDigits != 10 ) {
						JDialog parent = (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, PhoneField.this);
						MessageDialog.showMessage(parent, "Civet Error: Phone format", "CVI standard requires ten digit phone");
						PhoneField.this.requestFocus();
						return;
					}
					setText(formatPhone(sPhone));
				}
			}
		});
	}

	public String getText() {
		return formatDigitsOnly( super.getText() );
	}
	
	public String getFormattedText() {
		return formatPhone( super.getText() );
	}

	public static String formatDigitsOnly( String sPhone ) {
		if( sPhone == null ) return null;
		if( sPhone.indexOf("@") > -1 ) return sPhone;
		StringBuffer sbPhone = new StringBuffer();
		int i = 0;
		int j = 0;
		char cNext;

		for( i = 0; i < sPhone.length() && j < 15; i++ ) {
			cNext = sPhone.charAt(i);
			if( Character.isDigit(cNext) ) {
				sbPhone.append(cNext);
				j++;
			}
		}
		return sbPhone.toString();
	}

	public static String formatPhone( String sPhone ) {
		if( sPhone == null ) return null;
		if( sPhone.indexOf("@") > -1 ) return sPhone;
		String sPhoneDigits = formatDigitsOnly( sPhone );
		StringBuffer sbPhone = new StringBuffer();
		int iLenPhoneDigits = sPhoneDigits.trim().length();
		for( int i = 0; i < iLenPhoneDigits; i++ ) {
			char cNext = sPhoneDigits.charAt(i);
			if( i == 0 && sPhoneDigits.length() > 7 )
				sbPhone.append( "(" + cNext );
			else if( i == 3 && sPhoneDigits.length() > 7 )
				sbPhone.append( ")" + cNext );
			else if( (i == 6 && sPhoneDigits.length() > 7 ) || (i == 3 && sPhoneDigits.length() <= 7 ) )
				sbPhone.append( "-" + cNext );
			else if( i == 10 )
				sbPhone.append( " x" + cNext );
			else
				sbPhone.append( cNext );
		}
		return sbPhone.toString();
	}

} // end class DBPhoneField

