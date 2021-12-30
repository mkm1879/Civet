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
import javax.swing.text.Document;

import java.awt.event.*;

import javax.swing.JDialog;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.dialogs.MessageDialog;


@SuppressWarnings("serial")
public class DBNumericField extends JTextField{
      private static Logger logger = Logger.getLogger();
	private int iType = java.sql.Types.INTEGER;  // Someday handle other types
	private long lMin = Long.MIN_VALUE;
	private long lMax = Long.MAX_VALUE;
	

	public DBNumericField() {
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public DBNumericField(int p0) {
		super(p0);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public DBNumericField(String p0) {
		super(p0);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public DBNumericField(String p0, int p1) {
		super(p0, p1);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public DBNumericField(Document p0, String p1, int p2) {
		super(p0, p1, p2);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setSQLType( int iType ) {
		switch( iType ){
		case java.sql.Types.BIGINT:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			this.iType = iType;
			break;
		default:
			logger.error( "Unknown numeric type value = " + iType );
		}
	}

	public int getSQLType() { return iType; }

	public void setText( String sText ) {
		if( sText != null && sText.trim().length() > 0 && !isNumber( sText ) ) {
			logger.error("Cannot parse " + sText + " as a number");
		}
		else {
			super.setText(sText);
		}
	}

	public String getText() {
		String sText = super.getText();
		sText = stripCommas(sText.trim());
		return sText;
	}
	
	public void setRange( long lMin, long lMax ) {
		this.lMin = lMin;
		this.lMax = lMax;
	}

	boolean isNumber( String sText ) {
		// Ignore empty or leading minus sign
		if( sText == null || sText.trim().length() == 0 || sText.equals("-") ) return true;
		sText = stripCommas( sText.trim() );
		boolean bRet = false;
		switch( iType ){
		case java.sql.Types.DECIMAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
			try {
				Double.parseDouble(sText);
				bRet = true;
			}
			catch( NumberFormatException ex ) {
				bRet = false;
			}
			break;
		case java.sql.Types.BIGINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
			try {
				Integer.parseInt(sText);
				bRet = true;
			}
			catch( NumberFormatException ex ) {
				bRet = false;
			}
			break;
		default:
			logger.error( "Unknown numeric type value = " + iType );
		}
		return bRet;
	}

	private void jbInit() throws Exception {
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String sText = getText();
				if( sText == null || sText.trim().length() == 0 )
					return;
				if( !isNumber(sText) ) {
					java.awt.Container parent = getParent();
					while( !(parent instanceof JDialog) && parent != null ) parent = parent.getParent();
					MessageDialog.showMessage( (JDialog)parent, "Number Format Error", "Cannot read " + sText + " as a number" );
				}
				Long lValue = Long.parseLong(sText);
				if( lValue < lMin || lValue > lMax ) {
					java.awt.Container parent = getParent();
					while( !(parent instanceof JDialog) && parent != null ) parent = parent.getParent();
					MessageDialog.showMessage( (JDialog)parent, "Number Range Error", sText + " is " + ((lValue < lMin) ? "small" : "large") +
							"\nNormal range is " + lMin + " to " + lMax );
				}
				
			}
		});
		this.addKeyListener( new KeyAdapter() {
			public void keyTyped(java.awt.event.KeyEvent e) {
				String sText = getText();
				int iCode = e.getKeyCode();
				char cNext = e.getKeyChar();
				if( cNext != KeyEvent.VK_BACK_SPACE && iCode != KeyEvent.VK_DELETE && iCode != KeyEvent.VK_SHIFT ) {
					String sNext = sText + cNext;
					if (!isNumber(sNext)) {
						java.awt.Toolkit.getDefaultToolkit().beep();
						e.consume();
					}
				}
			}
		});
	}

	private String stripCommas( String sInput ) {
		if( sInput == null || sInput.indexOf(",") < 0 ) return sInput;
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < sInput.length(); i++ ) {
			char cNext = sInput.charAt(i);
			if( cNext != ',' ) sb.append(cNext);
		}
		return sb.toString();
	}

} // end class DBNumericField

