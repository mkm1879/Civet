package edu.clemson.lph.civet.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.FileUtils;

public class V2Transform {
      private static Logger logger = Logger.getLogger();
    private static Transformer transformer;
	
	static  {
		try { 
		File fXSLT = getTransformFile();
	    FileReader xsltReader = new FileReader( fXSLT );
		TransformerFactory tFactory = TransformerFactory.newInstance();
			transformer = tFactory.newTransformer(new StreamSource(xsltReader));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		} catch (FileNotFoundException e) {
			logger.error("Could not find XSLT: " + getTransformFile(), e);
		} catch (TransformerException e) {
			logger.error("Failed to transform XML with XSLT: " + getTransformFile(),e);
		}
	}

	public static String convertToV1( String sVersion2XML ) {
		String sRet = null;
		try {
		    StringReader sourceReader = new StringReader( sVersion2XML );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				sRet = new String( baosDest.toByteArray(), "UTF-8" );
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + getTransformFile(), e);
			} catch (UnsupportedEncodingException e) {
				logger.error("Should not see this unsupported encoding", e);
			} 
 		return sRet;
	}
	
	private static File getTransformFile() {
		File fTransform = new File( "./eCVI2_to_eCVI1.xsl");
		return fTransform;
	}
	
	public static void main( String args[] ) {
		try {
			String sV2 = FileUtils.readTextFile(new File("TestIn.xml"));
			String sV1 = convertToV1(sV2);
			System.out.println(sV1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

}
