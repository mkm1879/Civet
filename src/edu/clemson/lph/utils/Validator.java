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
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.*;

import javax.xml.parsers.SAXParser;

/**
 * Validator is a very basic brute-force XML validation using the standard SAX parser.
 * I do not know why the member factory and parser seem to need to be recreated for each pass.
 * This could be improved I'm sure.
 * @author mmarti5
 *
 */
public class Validator extends org.xml.sax.helpers.DefaultHandler {
	  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	  private String schemaSource = null;
	  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	  private SAXParserFactory factory = null;
	  private SAXParser saxParser = null;
	  
	  private String sLastError = null;

	  public void warning( SAXParseException ex ) throws SAXParseException {
	    throw(ex);
	  }

	  public void error( SAXParseException ex ) throws SAXParseException {
	    throw(ex);
	  }

	  public void fatalError( SAXParseException ex ) throws SAXParseException {
	    throw(ex);
	  }

	  /**
	   * Initialize the validator with the appropriate schema.
	   * @param sSchemaFile
	   */
	  public Validator( String sSchemaFile ) {
		  schemaSource = sSchemaFile;
		  initializeFactory();
	  }
	  
	  /**
	   * Access the last error message if the previous validation returned false.
	   * @return String with last error or null if last was successful.
	   */
	  public String getLastError() { return sLastError; }

	  private void initializeFactory() {
	    factory = SAXParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    factory.setValidating(true);
	    try {
	      saxParser = factory.newSAXParser();
	      saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
	      saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
	    }
	    catch (SAXException ex) {
	      ex.printStackTrace();
	      System.exit(1);
	    }
	    catch ( ParserConfigurationException pex ) {
	      pex.printStackTrace();
	      System.exit(1);
	    }
	    catch (IllegalArgumentException x) {
	      System.err.println( "Parser not supported" );
	      System.exit(1);
	    }
	  }

	  /**
	   * check one file for validity
	   * @param File fFile
	   * @return boolean
	   */
	  public boolean isValidXMLFile( String sFile ) {
		  try {
			  FileInputStream fis = new FileInputStream( sFile );
			  return isValidStream( fis );
		  }
		  catch( FileNotFoundException fne ) {
			  sLastError = sFile + " not found";
		  }
		  return false;
	  }
	  
	  /**
	   * check one String XML for validity
	   * @param sMsg String with XML message
	   * @return boolean true if valid
	   */
	  public boolean isValidXMLString( String sMsg ) {
		  ByteArrayInputStream bis = new ByteArrayInputStream( sMsg.getBytes() );
		  return isValidStream( bis );
	  }

	  /**
	   * Pass in the XML to be validated as an InputStream which may be from an open file
	   * a string, etc.
	   * @param is
	   * @return boolean true if valid
	   */
	  public boolean isValidStream( InputStream is ) {
		  boolean bRet = false;
		  sLastError = null;
		  try {
			  initializeFactory();  // I don't know why this needs to be intialized every time!
			  saxParser.parse( is, this );
//			  System.out.println("Validation successful");
			  bRet = true;
			  sLastError = null;
		  }
		  catch (SAXParseException spe) {
			  sLastError = spe.getLineNumber() + "~" + spe.getMessage();
		  }
		  catch (SAXException sxe) {
			  // Error generated during parsing
			  if (sxe.getException() != null)
				  sLastError = sxe.getException().getMessage();
			  else
				  sLastError = sxe.getMessage();
		  }
		  catch (IOException ioe) {
			  sLastError = ioe.getMessage();
		  }
		  finally {
			  try {
				  is.close();
			  } catch (IOException e) {
				  System.err.println( "Failed to close input stream");
				  e.printStackTrace();
			  }
		  }
		  return bRet;
	  }

}// End class validator
