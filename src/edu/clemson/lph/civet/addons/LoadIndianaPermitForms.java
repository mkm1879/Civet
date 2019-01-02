package edu.clemson.lph.civet.addons;

import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.webservice.CivetWebServiceFactory;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.webservice.CivetWebServicesNew;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlV1;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.db.ThreadListener;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

public class LoadIndianaPermitForms implements AddOn, ThreadListener {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_PERMIT;
	private static final String sProgMsg = "Loading Permit Form ";
	private static final OpenOption[] CREATE_OR_APPEND = new OpenOption[] { StandardOpenOption.APPEND, StandardOpenOption.CREATE };
	private static final String XSLT_FILE = "./IN_Permit_to_Standard.xsl";
	private static final String EGG_PERMITS_FILE = "./EggPermits.txt";
	private File files[];

	public LoadIndianaPermitForms() {
	}

	@Override
	public void onThreadComplete(Thread thread) {
		// TODO Auto-generated method stub
		for( File f : files ) {
			// move to Complete folder
			File fXml = new File(f.getAbsolutePath() + ".xml");
			f.renameTo( new File("./Complete/" + f.getName()) );
			fXml.renameTo( new File("./Complete/" + fXml.getName()) );
		}
	}

	@Override
	public String getMenuText() {
		return "Upload Indiana Permit Forms";
	}

	@Override
	public void execute(Window parent) {
		// Select File(s)
	    JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new File("."));
	    fc.setDialogTitle("Open Indiana Permit PDF File");
	    fc.setFileFilter( new PDFFilter() );
	    fc.setMultiSelectionEnabled(true);
	    int returnVal = fc.showOpenDialog(parent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      files = fc.getSelectedFiles();
	    }
	    else {
	    	return;
	    }
		// Run worker thread
		ProgressDialog prog = new ProgressDialog(parent, "Civet", sProgMsg);
		prog.setAuto(true);
		prog.setVisible(true);
		TWorkPermitForms tWork = new TWorkPermitForms( prog, files, parent );
		tWork.start();
	}

	class TWorkPermitForms extends Thread {
		String sFilePath;
		ProgressDialog prog;
		Window parent;
		CivetWebServices service;
		private File files[];
		
		public TWorkPermitForms( ProgressDialog prog, File files[], Window parent ) {
			this.prog = prog;
			this.files = files;
			this.parent = parent;
			service = CivetWebServiceFactory.getService();
		}
		
		public void run() {
			try {
				for( File f : files ) {
					String sAcrobatXml = toStdXMLString(f);
					FileUtils.writeTextFile(sAcrobatXml, f.getAbsolutePath() + ".xml");
					processFile(new File(f.getAbsolutePath() + ".xml"));
				}
			} catch (Exception e) {
				logger.error(e);
			}
			finally {
				exitThread(true);
			}
		}
		
		private void exitThread( boolean bSuccess ) {
			final boolean bDone = bSuccess;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					if( bDone )
						onThreadComplete(TWorkPermitForms.this);
				}
			});
		}
		
		public String toStdXMLString(File f) throws Exception {
			byte pdfBytes[] = FileUtils.readBinaryFile(f);
			Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
			Element eProduct = edu.clemson.lph.utils.XMLUtility.findFirstChildElementByName(xmlNode, "BirdType");
			String sProduct = eProduct.getTextContent();
			String sRet = null;
			String sXSLT = XSLT_FILE;
			String sAcrobatXML = nodeToString(xmlNode, false);
			try {
			    FileReader xsltReader = new FileReader( sXSLT );
			    StringReader sourceReader = new StringReader( sAcrobatXML );
			    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer;
				transformer = tFactory.newTransformer(new StreamSource(xsltReader));
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				sRet = new String( baosDest.toByteArray(), "UTF-8" );
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
			} catch (UnsupportedEncodingException e) {
				logger.error("Should not see this unsupported encoding", e);
			} catch (FileNotFoundException e) {
				logger.error("Could not find XSLT: " + sXSLT, e);
			}
			StdeCviXmlV1 xStd = new StdeCviXmlV1( sRet );
			CviMetaDataXml metaData = new CviMetaDataXml();
			metaData.setCertificateNbr(xStd.getCertificateNumber());
			metaData.setBureauReceiptDate(xStd.getBureauReceiptDate() );
			String sNote = "Movement Permit Form Import";
			if( sProduct != null && sProduct.toUpperCase().startsWith("EGG")) {
				sNote = "Egg " + sNote;
				FileUtils.writeTextFile(xStd.getCertificateNumber(), EGG_PERMITS_FILE, true);
			}
			metaData.setErrorNote(sNote);
			metaData.setCVINumberSource(sCVINbrSource);
			StdeCviXmlModel xmlBuilder = new StdeCviXmlModel(xStd);
			xmlBuilder.addMetadataAttachement(metaData);
			byte bytes[] = FileUtils.readBinaryFile(f);
			xmlBuilder.setPDFAttachment(bytes, f.getName());
			return xmlBuilder.getXMLString();
		}
		
		private String nodeToString(Node node, boolean bOmitDeclaration) {
			String sOmit = bOmitDeclaration ? "yes" : "no";
			StringWriter sw = new StringWriter();
			try {
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.transform(new DOMSource(node), new StreamResult(sw));
			} catch (TransformerException te) {
				logger.error("nodeToString Transformer Exception", te);
			}
			return sw.toString();
		}	
		
		private void processFile(File fThis) {
			try {
				String sXML = FileUtils.readTextFile(fThis);
				String sCertNbr = getCertNbr( sXML );
				// Check but don't add yet.
				if( CertificateNbrLookup.certficateNbrExists(sCertNbr) ) {
					MessageDialog.messageLater(parent, "Civet Error", "Certificate Number " + sCertNbr + " already exists.\n" +
							"Resolve conflict and try again.");
					return;
				}
				// Everything happens here!
				String sRet = service.sendCviXML(sXML);
				final String sReturn = sRet;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						logger.info("Return code from Civet WS: " + sReturn);
					}
				});		
				// If successfully sent, record the number in CertNbrs.txt
				if( sRet != null && !sRet.toLowerCase().contains("error")  && sRet.contains(service.getSuccessMessage() ) ) {
					if( !CertificateNbrLookup.addCertificateNbr(sCertNbr) ) {
						MessageDialog.messageLater(parent, "Civet Error", "Certificate Number " + sCertNbr + " Added twice.\n" +
								"Please report to developer.");
						return;
					}
				}
				else {
					throw new Exception( "Error from web service\n" + sRet);
				}
			} catch (final Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						logger.error("Error in uploading file to USAHERDS", e);
						if( e.getMessage().contains("There was an exception running the extensions") ) {
							MessageDialog.showMessage(null, "Civet Error", "Error Uploading.\nCheck the size of your scanned PDFs");
						}
					}
				});		
			} 
		}
		
		private String getCertNbr( String sXML ) {
			if( sXML == null || sXML.trim().length() == 0 ) return null;
			int iStart = sXML.indexOf("CviNumber=") + 11;
			int iEnd = sXML.substring(iStart).indexOf('\"');
			return sXML.substring(iStart, iStart+iEnd);
		}
	}
}
