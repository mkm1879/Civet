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

//import sun.misc.Regexp;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	  
	  public static void main( String[] args ) {
		  Validator v = new Validator("ecviDraftRelaxed.xsd");
		  if( v.isValidXMLFile("TestFile.cvi") )
			  System.out.println("Valid");
		  else {
			  System.out.println( v.getLastError() );
		  }
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
	  public String getLastError() { return formatDatatypeError(formatPatternError(formatElementError(formatEnumerationError(sLastError)))); }
	  
	  private String formatEnumerationError( String sError ) {
		  String sRet = null;
		  String sRegex = "Value '(.*)' is not facet-valid with respect to enumeration '\\[(.*)\\]";
		  Pattern pat = Pattern.compile(sRegex);
		  Matcher mat = pat.matcher(sError);
		  if( mat.find() ) {
			  int iStart = mat.start(1);
			  int iEnd = mat.end(1);
			  sRet = "Schema Validation Error:\nInvalid value: \"";
			  sRet += sError.substring(iStart,iEnd);
			  sRet += "\" not in list [" ;
			  iStart = mat.start(2);
			  iEnd = mat.end(2);
			  sRet += sError.substring(iStart,iEnd);
			  sRet += "].";
		  }
		  else 
			  sRet = sError;
		  return sRet;
	  }
	  
	  private String formatElementError( String sError ) {
		  String sRet = null;
		  String sRegex = "Invalid content was found starting with element '(.*)'. One of '\\{\"http://www.usaha.org/xmlns/ecvi\":(.*)\\}' is expected.";

		  Pattern pat = Pattern.compile(sRegex);
		  Matcher mat = pat.matcher(sError);
		  if( mat.find() ) {
			  int iStart = mat.start(1);
			  int iEnd = mat.end(1);
			  sRet = "Schema Validation Error:\nInvalid or missing element: \"";
			  sRet += sError.substring(iStart,iEnd);
			  sRet += "\" found before [" ;
			  iStart = mat.start(2);
			  iEnd = mat.end(2);
			  sRet += sError.substring(iStart,iEnd).replace("\"http://www.usaha.org/xmlns/ecvi\":", "");
			  sRet += "].";
		  }
		  else 
			  sRet = sError;
		  return sRet;
	  }
	  	  
	  private String formatPatternError( String sError ) {
		  String sRet = null;
		  String sRegex = "pattern-valid: Value '(.*)' is not facet-valid with respect to pattern '(.*)' for type '#AnonType_NumberPhoneNumType'.";

		  Pattern pat = Pattern.compile(sRegex);
		  Matcher mat = pat.matcher(sError);
		  if( mat.find() ) {
			  int iStart = mat.start(1);
			  int iEnd = mat.end(1);
			  sRet = "Schema Validation Error:\nInvalid data: \"";
			  sRet += sError.substring(iStart,iEnd);
			  sRet += "\" does not match the required pattern \"" ;
			  iStart = mat.start(2);
			  iEnd = mat.end(2);
			  sRet += sError.substring(iStart,iEnd).replace("\"http://www.usaha.org/xmlns/ecvi\":", "");
			  sRet += "\".";
		  }
		  else 
			  sRet = sError;
		  return sRet;
	  }
	  
	  private String formatDatatypeError( String sError ) {
		  String sRet = null;
		  String sRegex = "datatype-valid.1.2.1: '(.*)' is not a valid value for '(.*)'.";

		  Pattern pat = Pattern.compile(sRegex);
		  Matcher mat = pat.matcher(sError);
		  if( mat.find() ) {
			  int iStart = mat.start(1);
			  int iEnd = mat.end(1);
			  sRet = "Schema Validation Error:\nInvalid data: \"";
			  sRet += sError.substring(iStart,iEnd);
			  sRet += "\" does not match the required data type \"" ;
			  iStart = mat.start(2);
			  iEnd = mat.end(2);
			  sRet += sError.substring(iStart,iEnd).replace("\"http://www.usaha.org/xmlns/ecvi\":", "");
			  sRet += "\".";
		  }
		  else 
			  sRet = sError;
		  return sRet;
	  }

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
	      throw new RuntimeException(ex.getMessage());
	    }
	    catch ( ParserConfigurationException pex ) {
	      pex.printStackTrace();
	      throw new RuntimeException(pex.getMessage());
	    }
	    catch (IllegalArgumentException x) {
	      System.err.println( "Parser not supported" );
	      throw new RuntimeException(x.getMessage());
	    }
	  }

	  /**
	   * check one file for validity
	   * @param File fFile
	   * @return boolean
	   */
	  public boolean isValidXMLFile( String sFile ) {
		  boolean bRet = false;
		  FileInputStream fis = null;
		  try {
			  fis = new FileInputStream( sFile );
			  bRet = isValidStream( fis );
		  }
		  catch( FileNotFoundException fne ) {
			  sLastError = sFile + " not found";
			  bRet = false;
		  }
		  finally {
			  if( fis != null )
				try {
					fis.close();
				} catch (IOException e) {
				}
		  }
		  return bRet;
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
	  
	  public boolean isValidXMLBytes( byte[] xmlBytes ) {
		  ByteArrayInputStream bis = new ByteArrayInputStream( xmlBytes );
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
