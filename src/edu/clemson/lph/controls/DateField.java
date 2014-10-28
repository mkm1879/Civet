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
import java.awt.Frame;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JTextField;

import edu.clemson.lph.dialogs.*;

public class DateField extends JTextField {
	private static final long serialVersionUID = 1L;
	public static final int NO_FUTURE = 1;
	public static final int ALLOW_FUTURE = 2;
	public static final int ASK_FUTURE = 3;
	public static final int SANE = 0;
	public static final int ASKED_YES = 1;
	public static final int ASKED_NO = 2;
	public static final int REJECT = 3;
	private int iFutureStatus = ASK_FUTURE;
	private boolean bAccepted = false;
	private static String sDateFormatOut = "MMM dd, yyyy";
	// Duplicate first element is intentional so if aDateFormats[0] is replaced we don't lose this original default value
	private static String[] aDateFormats = { "yyyy-MM-dd",  "yyyy-MM-dd", 
		"MMM dd, yyyy", "dd-MMM-yyyy", "dd-MMM-yy", "dd MMM yy", "dd MMM yyy", "d MMM yy",
		"d MMM yyyy", "MM/dd/yyyy", "MM/dd/yy", "MM/d/yy",
		"M-dd-yy", "M-dd-yyyy",	"M/dd/yy", "M/dd/yyyy", "M/d/yy", "M/d/yyyy", 
		"MM-dd-yyyy", "MM-dd-yy", "MM-d-yy", "M-d-yyyy", "M-d-yy", "MMddyy", "Mdyy", "MMMMM dd, yyyy",  };
	private static int iMaxDateWidth = 12;
	public static String getDateFormatOut() { return sDateFormatOut; }
	public static String getDateFormat() { return aDateFormats[0]; }
	public static void setDateFormat( String sFormat ) { 
		aDateFormats[0] = sFormat; 
		sDateFormatOut = sFormat;
	}
	public static String[] getDateFormats() { return aDateFormats; }
	public static void setDateFormats( String[] aDateFormats ) { DateField.aDateFormats = aDateFormats; }

	public DateField() {
		super(iMaxDateWidth);
		init();
	}
	
	public DateField(String sText) {
		super(sText,iMaxDateWidth);
		init();
	}
	
	public void setFutureStatus( int iFutureStatus ) {
		this.iFutureStatus = iFutureStatus;
	}
	
	
	private void init() {
		addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String sDate = getText();
				if( sDate.trim().length() > 0 ) {
					// ASK_FUTURE will prompt about future dates
					java.util.Date dDate = textToDate(sDate, iFutureStatus);
					if( dDate != null ) { 
						bAccepted = true;
						setText(dateToText(dDate));
					}
					else {
						bAccepted = false;
						DateField.this.requestFocus();
					}
				}
			}
		});
	}
	
	public boolean isAcceptedDate() { return bAccepted; }
	
	public void setDate( java.util.Date dDate ) {
		setText( dateToText( dDate ) );
	}
	
	public java.util.Date getDate() {
		return textToDate( getText() );
	}

	  /**
	   * Format date using prefered (display) format.  Different formats for
	   * different java Date types all work the same.
	   * @param dValue Date
	   * @return String
	   */
	  public static String dateToText( java.util.Date dValue ) {
	    if( dValue == null ) return null;
	    SimpleDateFormat df = new SimpleDateFormat( getDateFormatOut() );
	    return df.format(dValue);
	  }

	  /**
	   * Format date using prefered (display) format.  Different formats for
	   * different java Date types all work the same.
	   * @param dValue Date
	   * @return String
	   */
	  public static String dateToText( java.util.Date dValue, String sFormat ) {
	    if( dValue == null ) return null;
	    SimpleDateFormat df = new SimpleDateFormat( sFormat );
	    return df.format(dValue);
	  }

	  /**
	   * Format date using prefered (display) format.  Different formats for
	   * different java Date types all work the same.
	   * @param dValue Timestamp
	   * @return String
	   */
	  public static String dateToText( java.sql.Timestamp dValue ) {
	    if( dValue == null ) return null;
	    SimpleDateFormat df = new SimpleDateFormat( getDateFormatOut() );
	    return df.format(dValue);
	  }

	  /**
	   * Parse a date string to a sql Date for entry into database using
	   * the list of format patterns provided in setDateFormats().
	   * @param sValue String
	   * @return Date
	   */
	  public static java.util.Date textToDate( String sValue ) {
		  return textToDate( sValue, ALLOW_FUTURE );
	  }
	  
	  /**
	   * Parse a date string to a sql Date for entry into database using
	   * the list of format patterns provided in setDateFormats().
	   * @param sValue String
	   * @return Date
	   */
	  public static java.util.Date textToDate( String sValue, String sFormat ) {
		  return textToDate( sValue, sFormat, ALLOW_FUTURE );
	  }
	  
	  public static String textToText( String sValue ) {
		  return textToText( sValue, null);
	  }
	  
  /**
   * Force format of date INPUT
   * @param sValue String in format specified in sFormat
   * @param sFormat String format of date string
   * @return date formatted according to default sDateFormatOut.
   */
  public static String textToText( String sValue, String sFormat ) {
	  return dateToText( textToDate( sValue, sFormat ) );
  }
  
/**
* Force format of date INPUT
* @param sValue String in format specified in sFormat
* @param sFormat String format of date string
* @return date formatted according to default sDateFormatOut.
*/
public static String textToText( String sValue,int iFutureMode ) {
  return dateToText( textToDate( sValue, iFutureMode ) );
}

	  /**
	   * Parse a date string to a sql Date for entry into database using
	   * the list of format patterns provided in setDateFormats().
	   * @param sValue String
	   * @param iAllowFutureMode one of three int constants ALLOW_FUTURE, NO_FUTURE, ASK_FUTURE
	   * @return Date
	   */
	  public static java.util.Date textToDate( String sValue, int iAllowFutureMode ) {
		  return textToDate( sValue, null, iAllowFutureMode );
	  }
		  
	  public static java.util.Date textToDate( String sValue, String sFormat, int iAllowFutureMode ) {
		  if( sValue == null || sValue.trim().length() == 0 ) return null;
		  boolean bAsked = false;
		  java.util.Date dRet = null;
		  SimpleDateFormat df = new SimpleDateFormat( aDateFormats[ 0 ] );
		  if( sFormat != null ) {
			  try {
				  df.applyPattern( sFormat );
				  dRet = new java.util.Date( (df.parse(sValue)).getTime());
				  if( isSane( dRet, sValue, sFormat, iAllowFutureMode, true ) == REJECT) {
					  dRet = null;
				  }
			  }
			  catch (ParseException ex) {
				  // Do nothing.  This is just a pattern that doesn't fit so move on.
			  }
			  
		  }
		  else {
			  int iPass = 1;
			  int iFirstDash =  sValue.indexOf('-');
			  int iFirstSlash =  sValue.indexOf('/');
			  int iLastDash =  sValue.lastIndexOf('-');
			  int iLastSlash =  sValue.lastIndexOf('/');
			  while( dRet == null && iPass <= 2 ) {
				  // Try all the patterns in our list one at a time starting with the prefered (display) format
				  for( int iIndex = 0; dRet == null && iIndex < aDateFormats.length; iIndex++ ) {
					  try {
						  String sNextFormat = aDateFormats[ iIndex ];
						  // Don't allow a yyyy format to try to parse a yy year or it will end up in the first century
						  // Subtract one to prevent skipping when using single digit day or month
						  if( sValue.length() < sNextFormat.length()-1 ) continue;
						  if( iFirstDash > 0 && sNextFormat.indexOf('-') != iFirstDash ) continue;
						  if( iFirstSlash > 0 && sNextFormat.indexOf('/') != iFirstSlash ) continue;
						  if( iLastDash > 0 && sNextFormat.lastIndexOf('-') != iLastDash ) continue;
						  if( iLastSlash > 0 && sNextFormat.lastIndexOf('/') != iLastSlash ) continue;
						  df.applyPattern( aDateFormats[iIndex] );
						  dRet = new java.util.Date( (df.parse(sValue)).getTime());
						  int iSane = isSane( dRet, sValue, aDateFormats[iIndex], iAllowFutureMode, (iPass > 1 && !bAsked) );
						  if( iSane == ASKED_NO ) bAsked = true;
						  // Completely valid date, use it.  Accepted 
						  if( iSane == SANE || iSane == ASKED_YES ) {
							  break;
						  }
						  else {
							  dRet = null;
						  }
					  }
					  catch (ParseException ex) {
						  // Do nothing.  This is just a pattern that doesn't fit so move on.
					  }
				  }
				  iPass++;
			  }
		  }
		  // Formatted Dates are stored
		  if( dRet == null ) {
			  if( "today".equalsIgnoreCase(sValue) ) {
				  Calendar cal2 = new GregorianCalendar();
				  dRet = new java.util.Date( cal2.getTime().getTime() );
			  }
			  else if( "tomorrow".equalsIgnoreCase(sValue) ) {
				  Calendar cal2 = new GregorianCalendar();
				  cal2.add( Calendar.DAY_OF_YEAR, 1);
				  dRet = new java.util.Date( cal2.getTime().getTime() );
			  }
			  else if( "yesterday".equalsIgnoreCase(sValue) ) {
				  Calendar cal2 = new GregorianCalendar();
				  cal2.add( Calendar.DAY_OF_YEAR, -1);
				  dRet = new java.util.Date( cal2.getTime().getTime() );
			  }
		  }
		  // Today or Tomorrow stored
		  // If none of these, give up.
		  if( dRet == null && !bAsked ) {
			  MessageDialog.showMessage( (JFrame)null, "SC Prem ID: Date Format Error",
					  		"Cannot parse \"" + sValue + "\" as a date." );
			  return null;
		  }
		  return dRet;
	  }
	  
	  private static int isSane( java.util.Date dRet, String sValue, String sFormat, int iAllowFutureMode, boolean bConfirm) {
		  int iRet = SANE;
		  SimpleDateFormat df = new SimpleDateFormat( aDateFormats[ 0 ] );
		  // Get a two digit year start date twenty years in the past.
		  Calendar calPast = new GregorianCalendar();
		  calPast.add( Calendar.YEAR, -20 );
		  df.set2DigitYearStart(calPast.getTime());
		  // Reject way past values
		  if( dRet.getTime() < calPast.getTime().getTime() ) {
			  if( bConfirm ) {
				  MessageDialog.showMessage( (JFrame)null, "SC Prem ID: Date Format Error",
					  		"\"" + sValue + "\" parses as a date way in the past using format " + sFormat );
				  iRet = ASKED_NO;
			  }
			  else 
				  iRet = REJECT;
		  }
		  // Otherwise examine future values
		  Calendar calNow = new GregorianCalendar(); // now
		  Calendar calFuture = new GregorianCalendar();
		  calFuture.add(Calendar.YEAR, 20);
		  // If future not allowed, reject later than now
		  if( dRet.getTime() > calNow.getTime().getTime() && iAllowFutureMode == NO_FUTURE ) {
			  if( bConfirm ) {
				  MessageDialog.showMessage( (JFrame)null, "SC Prem ID: Date Format Error",
			  				"Future dates not allowed here. Parsing " + sValue + " using format " + sFormat );
			  iRet = ASKED_NO;
		  }
		  else 
			  iRet = REJECT;
		  }
		  // In any case reject way in the future
		  else if( dRet.getTime() > calFuture.getTime().getTime() ) {
			  if( bConfirm ) {
				  MessageDialog.showMessage( (JFrame)null, "SC Prem ID: Date Format Error",
					  			"\"" + sValue + "\" parses as a date way in the future using format " + sFormat );
				  iRet = ASKED_NO;
			  }
			  else 
				  iRet = REJECT;
		  }
		  // If asked allow future dates
		  else if( dRet.getTime() > calNow.getTime().getTime() && iAllowFutureMode == ASK_FUTURE ) {
			  if( bConfirm ) {
				  boolean bOK = false;
				  bOK = YesNoDialog.ask( (Frame)null, "SC Prem ID: Future Date",
						  "Do you mean enter a date in the future? \"" + sValue + " using format " + sFormat +"\n" );
				  if( !bOK ) {
					  iRet = ASKED_NO;
				  }
				  else {
					  iRet = ASKED_YES;
				  }
			  }
			  else
				  iRet = REJECT;
		  }
		  return iRet;
	  }
	  
	  public static void main( String args[] ) {
		  System.out.println( DateField.textToText("2014-03-26", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("3/26/14", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("3/26/2014", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("4/26/2014", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("3/26/2008", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("3-26-14", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("03-26-2014", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("Mar 26, 2014", DateField.NO_FUTURE) );
		  System.out.println( DateField.textToText("Zon 26, 2014", DateField.NO_FUTURE) );
	  }
	  
	  public boolean isFuture() {
		  boolean bRet = false;
		  if( getDate() == null ) return false;
		  // Otherwise examine future values
		  Calendar calNow = new GregorianCalendar(); // now
		  Calendar calFuture = new GregorianCalendar();
		  calFuture.add(Calendar.YEAR, 20);
		  // If future not allowed, reject later than now
		  if( getDate().getTime() > calNow.getTime().getTime() )
			  bRet = true;
		  return bRet;
	  }

}
