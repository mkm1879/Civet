package edu.clemson.lph.utils;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.Civet;

public class CSVWriter {
	@SuppressWarnings("unused")
      private static Logger logger = Logger.getLogger();
	private char cSepChar = ',';
	private char cQuoteChar = '"';
	private ArrayList<String> aRows;
	private int iCols = -1;
	private SimpleDateFormat dFmt = new SimpleDateFormat("MM/dd/yyyy");

	public CSVWriter() {
		aRows = new ArrayList<String>();
	}
		
	public void setSepChar( char c ) {
		cSepChar = c;
	}
	
	public void setQuoteChar( char c ) {
		cQuoteChar = c;
	}
	
	public void setDateFormat( String sFmt ) {
		dFmt = new SimpleDateFormat( sFmt );
	}

	public void setHeader( String colNames[] ) throws DataFormatException {
		ArrayList<String> aColNames = new ArrayList<String>( Arrays.asList(colNames) );
		setHeader( aColNames );
	}
		
	public void setHeader( ArrayList<String> aColNames ) throws DataFormatException {
		if( iCols <= 0 ) iCols = aColNames.size();
		if( iCols != aColNames.size() ) {
			throw new DataFormatException( "Data row size mismatch" );
		}
		StringBuffer sb = new StringBuffer();
		boolean bFirst = true;
		for( String sName : aColNames ) {
			if( !bFirst )
				sb.append(cSepChar);
			else 
				bFirst = false;
			sb.append( formatField(sName) );
		}
		aRows.add(0, sb.toString());
	}
	
	public void addRow( Object oData[] ) throws DataFormatException {
		ArrayList<Object> aData = new ArrayList<Object>( Arrays.asList(oData) );
		addRow( aData );
	}
		
	public void addRow( ArrayList<Object> aData ) throws DataFormatException {
		if( iCols <= 0 ) iCols = aData.size();
		if( iCols != aData.size() ) {
			throw new DataFormatException( "Data row size mismatch" );
		}
		StringBuffer sb = new StringBuffer();
		boolean bFirst = true;
		for( Object oData : aData ) {
			if( !bFirst )
				sb.append(cSepChar);
			else 
				bFirst = false;
			sb.append( formatField(oData) );
		}
		aRows.add(sb.toString());
	}
	
	public int write( String sFileOutName ) throws FileNotFoundException {
		if( !sFileOutName.toLowerCase().endsWith(".csv") )
			sFileOutName = sFileOutName + ".csv";
		return write( new File( sFileOutName ) );
	}
	
	public int write( File fFileOut ) throws FileNotFoundException {
		int iRet = 0;
		FileOutputStream fos = new FileOutputStream( fFileOut );
		iRet = write( fos );
		try {
			fos.close();
		} catch (IOException e) {
		}
		return iRet;
	}
	
	private int write( OutputStream strOut ) {
		int iWritten = 0;
		PrintWriter pw = new PrintWriter( strOut );
		for( String sRow : aRows ) {
			pw.println( sRow );
			iWritten++;
		}
		pw.flush();
		pw.close();
		return iWritten;
	}
	
	/**
	 * Quote strings and format numbers and dates.
	 * @param oData
	 * @return
	 */
	private String formatField( Object oData ) {
		String sRet = null;
		if( oData == null )
			sRet = "";
		else if( oData instanceof String ) 
			sRet = cQuoteChar + ((String)oData) + cQuoteChar;
		else if( oData instanceof Integer )
			sRet = ((Integer)oData).toString();
		else if( oData instanceof Double )
			sRet = ((Double)oData).toString();
		else if( oData instanceof Float )
			sRet = ((Float)oData).toString();
		else if( oData instanceof java.util.Date )
			sRet = dFmt.format((java.util.Date)oData);
		else
			sRet = oData.toString();
		return sRet;
	}



}
