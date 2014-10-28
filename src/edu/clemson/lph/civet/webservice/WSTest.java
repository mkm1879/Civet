package edu.clemson.lph.civet.webservice;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfDictionary;
import org.w3c.dom.Document;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.CoKsXML;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.utils.XMLUtility;


/**
 * Using simple HTTP POST version of web services.  These are in two varieties.  One takes a series of string parameters.  The 
 * other takes a single XML document defined for the specific service.  
 * To generate stub and proxies use:
 * E:\EclipseJava\Civet\Generated>set AXIS2_HOME=E:\JavaLib\axis2-1.6.2
 * E:\EclipseJava\Civet\Generated>E:\JavaLib\axis2-1.6.2\bin\wsdl2java -S src -uri CivetExternalMessages.wsdl
 * Must edit the WSDL file extensively.  To replace base64binary parameters with string and returned xml:any with string.
 * Defining Body as string will avoid large numbers of errors.
 * (The second is not currently working.  Will document here what it took to make it work.)
 * @author mmarti5
 *
 */

public class WSTest {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.ERROR);
	}

	public WSTest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main( String args[] ) {
		CivetWebServices service = new CivetWebServices();
//		String currentFilePath = "E:\\Documents\\CivetRobotComplete\\eCVI ver 3.1Dr.BobSigned.pdf";
//		String sXML = null;
//		try {
//			sXML = getCviXML(currentFilePath);
//			String sResult;
//			sResult = service.sendCviXML(sXML);
//			System.out.println(sResult);
//		} catch( PdfException e) {
//			logger.error("Failed to read PDF file: " + currentFilePath, e);
//		} catch( Exception e ) {
//			logger.error("Failed to send xml\n" + sXML, e);
//		}
		Document dom;
		try {
			dom = service.getCivetLookupDocument(UsaHerdsWebServiceLookup.LOOKUP_SPECIES);
			System.out.println(XMLUtility.printElements(dom));
		} catch (WebServiceException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
//		Document dom1 = service.getCivetVets( null, null, "3034350333", null, false);
//		dom1 = service.getCivetVets( null, null, null, null, true);
//		System.out.println(XMLUtility.printElements(dom1));
//         String sStatePremID, String sFedPremID, 
//		   String sAddress1, String sCity, String sStateCode, String sZipCode, 
//		   String sCounty, String sCountry, String sPhone, String sClassType
//		Document dom2 = service.getCivetPremises(null, null, null, null, "CO", "80524*", null, null, null, null);
//		java.util.Set<String> hNames = XMLUtility.getUniqueElementNames( dom2 );
//		for( String sName : hNames ) {
//			System.out.println( sName );
//		}
//		System.out.println(XMLUtility.printElements(dom2));
	}
	
	@SuppressWarnings("unused")
	private static String getCviXML( String sFileName ) throws PdfException {
		String sRet = null;
		PdfDecoder pdfDecoder = new PdfDecoder();
		pdfDecoder.openPdfFile(sFileName);
		AcroRenderer rend = pdfDecoder.getFormRenderer();
		// Extract and transform from CO/KS form
		if( rend.isXFA() ) {
			byte[] xmlBytes = rend.getXMLContentAsBytes(PdfDictionary.XFA_DATASET);
			CoKsXML coks = new CoKsXML( xmlBytes );
			StdeCviXml std = coks.getStdeCviXml();
			sRet = std.getXMLString();
		}

		return sRet;
	}

}