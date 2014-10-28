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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class CSVPrinter {
	private char cSepChar = ',';
	private char cQuoteChar = '"';
	
	private StringBuffer sb = null;
	private PrintWriter printer = null;
	
	public CSVPrinter(File file) throws FileNotFoundException {
		printer = new PrintWriter(file);
		sb = new StringBuffer();
	}
	public CSVPrinter(OutputStream out) {
		printer = new PrintWriter(out);
		sb = new StringBuffer();
	}
	public CSVPrinter(String fileName) throws FileNotFoundException {
		printer = new PrintWriter(fileName);
		sb = new StringBuffer();
	}
	public CSVPrinter(Writer out) {
		printer = new PrintWriter(out);
		sb = new StringBuffer();
	}
	
	/**
	 * Override default sep char , 
	 * Must call before any output to get logical results.
	 * @param c
	 */
	public void setSepChar( char c ) {
		cSepChar = c;
	}
	
	/**
	 * Override default quote char "
	 * Must call before any output to get logical results.
	 * @param c
	 */
	public void setQuoteChar( char c ) {
		cQuoteChar = c;
	}
	
	/**
	 * Output a string adding quote chars if it contains sep chars.
	 * @param sStringIn
	 */
	public void print( String sStringIn ) {
		if( sb.length() > 0 ) {
			sb.append(cSepChar);
		}
		if( sStringIn != null ) {
			boolean bStr = sStringIn.indexOf(cSepChar) >= 0; 
			if( bStr ) sb.append(cQuoteChar);
			sb.append(sStringIn);
			if( bStr ) sb.append(cQuoteChar);
		}
	}
	
	/**
	 * Output a full line from a list of strings
	 * @param lStrings
	 */
	public void println( List<String> lStrings ) {
		for( String s : lStrings ) {
			if( sb.length() > 0 ) {
				sb.append(cSepChar);
			}
			boolean bStr = s.indexOf(cSepChar) >= 0; 
			if( bStr ) sb.append(cQuoteChar);
			sb.append(s);
			if( bStr ) sb.append(cQuoteChar);
		}
		println();
	}
	
	/**
	 * Output a full line from an array of strings
	 * @param aStrings
	 */
	public void println( String[] aStrings ) {
		for( String s : aStrings ) {
			if( sb.length() > 0 ) {
				sb.append(cSepChar);
			}
			boolean bStr = s.indexOf(cSepChar) >= 0; 
			if( bStr ) sb.append(cQuoteChar);
			sb.append(s);
			if( bStr ) sb.append(cQuoteChar);
		}
		println();
	}
	
	/**
	 * Output Integer as string
	 * @param i
	 */
	public void print( Integer i ) {
		print(Integer.toString(i));
	}
	
	/** 
	 * Output Double as string
	 * @param d
	 */
	public void print( Double d ) {
		print(Double.toString(d));
	}

	/**
	 * Terminate a line and send to output
	 */
	public void println() {
		printer.println(sb.toString());
		sb.setLength(0);
	}
	
	/**
	 * Send any remaining data to last row and flush output
	 */
	public void flush() {
		if( sb.length() > 0 )
			println();
		printer.flush();
	}
	
	/**
	 * Flush if necessary and then close output
	 */
	public void close() {
		flush();
		printer.close();
	}

}
