package edu.clemson.lph.civet.lookup;
/*
Copyright 2015 Michael K Martin

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

public class CertificateNbrLookup {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static HashSet<String> setCertNbrs = null;

	/**
	 * Constructor will get slow as cache of existing numbers grows so call during 
	 * a load thread in prep from lookups in the main thread.
	 */
	public CertificateNbrLookup() {
		if( setCertNbrs == null ) {
			readCertNbrs();
		}
	}
	
	/** 
	 * Use to test existence of Certificate Number without adding such as live during 
	 * CivetEdit process.
	 * @param sCertNbr
	 * @return true if the number is a duplicate, false if unique or blank.
	 */
	public static boolean certficateNbrExists( String sCertNbr ) {
		if( sCertNbr == null || sCertNbr.trim().length() == 0 )
			return false;
		if( setCertNbrs == null ) {
			readCertNbrs();
		}
		return setCertNbrs.contains(sCertNbr);
	}
	
	/**
	 * This method checks that a certficate does not already exist with the number and adds it.
	 * Once per save cycle call this method.
	 * @param sCertNbr
	 * @return True if we added the number, no duplicate, or it was blank
	 */
	public static boolean addCertificateNbr( String sCertNbr ) {
		boolean bRet = false;
		if( sCertNbr == null || sCertNbr.trim().length() == 0 )
			return true;
		if( setCertNbrs == null ) {
			readCertNbrs();
		}
		if(!setCertNbrs.add(sCertNbr) ) {
			return false;
		}
		else 
			bRet = true;
		PrintWriter out = null;
	    try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("CertNbrs.txt", true)));
			out.println(sCertNbr);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if( out != null ) {
				out.flush();
				out.close();
			}
		}
		return bRet;
	}
	
	private static void readCertNbrs() {
		if( setCertNbrs == null ) {
			setCertNbrs = new HashSet<String>();
		}
		File fCertNbrs = new File( "CertNbrs.txt" );
		// TODO Add robust checking for stupid things like folder named "CertNbrs.txt"
		if( !fCertNbrs.exists() ) {
			return;
		}
		BufferedReader brCertNbrs = null;
		try {
			brCertNbrs = new BufferedReader( new FileReader( fCertNbrs ));
			String sNext = brCertNbrs.readLine();
			while( sNext != null ) {
				if(!setCertNbrs.add(sNext))
					logger.error(new Exception("Duplicate CertNbr " + sNext + " in file CertNbrs.txt" ));
				sNext = brCertNbrs.readLine();
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if( brCertNbrs != null ) {
				try {
					brCertNbrs.close();
				} catch (IOException e2) {
					logger.error(e2);
				}
			}
		}
	}

	/**
	 * Main just for temporary unit testing.
	 * @param args
	 */
	public static void main(String[] args) {
		String[] names = {"fred", "john", "ted","alice","bob","robert"};
		CertificateNbrLookup lu = new CertificateNbrLookup();
		for( String name : names ) {
			if( lu.certficateNbrExists(name))
				System.err.println(name +" exists");
			if( !lu.addCertificateNbr(name) ) 
				System.err.println(name +" exists already");
		}

	}

}
