package edu.clemson.lph.civet.extractor;

import java.io.File;

import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.utils.FileUtils;

public class CVIExtractor {

	private CVIExtractor() {
	}

	public static void main(String[] args) {
		if( args.length < 1 ) {
			System.err.println( "Usage: java -jar Extractor.jar File.cvi" );
			System.exit(1);
		}
		String sFile = args[0];
		String sPdfFile = sFile + ".pdf";
		String sMetaFile = sFile + ".meta.txt";
		File fIn = new File(sFile);
		StdeCviXmlModel stdXml = new StdeCviXmlModel( fIn );
		byte pdfBytes[] = stdXml.getPDFAttachmentBytes();
		FileUtils.writeBinaryFile(pdfBytes, sPdfFile);
		CviMetaDataXml meta = stdXml.getMetaData();
		String sXMeta = meta.getXmlString();
		FileUtils.writeTextFile(sXMeta, sMetaFile);
	}

}
