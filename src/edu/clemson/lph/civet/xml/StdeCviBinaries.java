/**
 * Copyright 2018 Michael K Martin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.clemson.lph.civet.xml;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.clemson.lph.civet.Civet;

/**
 * Just to get the complexity of Binaries and their references isolated.
 */
public class StdeCviBinaries {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private XMLDocHelper helper;
	private int iSeq;  // Use for new IDs
	private ArrayList<String> aRefs;  // List all used and new IDs
	// Avoid constructing the same wrapper repeatedly.
	private CviMetaDataXml mMetaData = null;
	
	/**
	 * Create this child class to hold binaries and keep track of IDs and IDRefs.
	 * @param helper The parent helper contains references to the XML document
	 * containing these Binaries.
	 */
	public StdeCviBinaries(XMLDocHelper helper) {
		this.helper = helper;
		iSeq = 0;
		aRefs = listIds();
	}
	
	private String getNextId( String sPrefix ) {
		String sRet = "";
		for( int iNext = iSeq + 1; iNext < Integer.MAX_VALUE  ; iNext++ ) {
			sRet = sPrefix + Integer.toString(iNext);
			if( !(aRefs.contains(sRet)) ) {
				iSeq = iNext;  // This wastes int values for different prefixes, but they are cheap.
				break;
			}
		}
		return sRet;
	}
	
	private ArrayList<String> listIds() {
		ArrayList<String> aRet = new ArrayList<String>();
		aRet.addAll(0, helper.listAttributesByPath("/eCVI/Accessions/Accession", "id") );
		aRet.addAll(0, helper.listAttributesByPath("/eCVI/Binary", "ID") );
		return aRet;
	}
	
	// Create Adds, Gets, and Lists as needed.
	
	public Element getAttachment( String sFileName ) {
		Element eRet = null;
		for( Element e : helper.getElementsByName("Attachment") ) {
			if( e != null ) {
				String sThisFile = e.getAttribute("FileName");
				if( sFileName.equals(sThisFile) ) {
					eRet = e;
					break;
				}
			}
		}
		return eRet;
	}
	
	public Element getBinary( String sId ) {
		Element eRet = null;
		for( Element e : helper.getElementsByName("Binary") ) {
			if( e != null ) {
				String sThisId = e.getAttribute("ID");
				if( sId.equals(sThisId) ) {
					eRet = e;
					break;
				}
			}
		}
		return eRet;
	}
	
	public byte[] getPDFAttachmentBytes() {
		byte[] pdfBytes = null;
		Element attach = null;
		for( Element e : helper.getElementsByName("Attachment") ) {
			if( e != null ) {
				String sDocType = e.getAttribute("DocType");
				if( "PDF CVI".equals(sDocType) ) {
					attach = e;
					break;
				}
			}
		}
		String sId = attach.getAttribute("AttachmentRef");
		if( sId != null && sId.trim().length() > 0 ) {
			Element binary = getBinary( sId );
			if( binary == null ) return pdfBytes;
			Element payload = helper.getChildElementByName(binary, "Payload");
			if( payload == null ) return pdfBytes;
			String sBase64 = payload.getTextContent();
			pdfBytes = Base64.decodeBase64(sBase64);
		}
		return pdfBytes;
	}
	
	public String getPDFAttachmentFilename() {
		String sRet = null;
		Element attach = null;
		for( Element e : helper.getElementsByName("Attachment") ) {
			if( e != null ) {
				String sDocType = e.getAttribute("DocType");
				if( "PDF CVI".equals(sDocType) ) {
					attach = e;
					break;
				}
			}
		}
		sRet = attach.getAttribute("Filename");
		return sRet;
	}
	
	public void addPDFAttachment( byte[] pdfBytes, String sFileName ) {
		String sPDF64 = new String(Base64.encodeBase64(pdfBytes));
		String sID = getNextId("A");
		try {
			String sAfter = StdeCviXmlBuilder.getAfter("MiscAttribute");
			Element attach = helper.insertElementBefore("Attachment", sAfter);
			attach.setAttribute("DocType", "PDF CVI");
			attach.setAttribute("AttachmentRef", sID);
			attach.setAttribute("Filename", sFileName);
			Element binaries = helper.getOrInsertElementBefore("Binaries", null);
			Element binary = helper.appendChild(binaries, "Binary");
			binary.setAttribute("MimeType", "application/pdf");
			binary.setAttribute("ID", sID);
			Element payload = helper.appendChild(binary, "Payload");
			attach.appendChild(payload);
			payload.setTextContent(sPDF64);
		} catch ( Exception e) {
			logger.error("Should not see this error for unsupported encoding", e);
		}
	}
	
	public void addOrUpdateMetadata( byte[] pdfBytes ) {
		Element attach = getAttachment("CviMetadata.xml");
		if( attach != null ) 
			updateMetadataAttachment( pdfBytes );
		else
			addMetadataAttachment( pdfBytes );
	}
	
	private void addMetadataAttachment( byte[] pdfBytes ) {
		String sPDF64 = new String(Base64.encodeBase64(pdfBytes));
		String sID = getNextId("A");
		try {
			String sAfter = StdeCviXmlBuilder.getAfter("MiscAttribute");
			Element attach = helper.insertElementBefore("Attachment", sAfter);
			attach.setAttribute("DocType", "Other");
			attach.setAttribute("AttachmentRef", sID);
			attach.setAttribute("Filename", "CviMetadata.xml");
			Element binaries = helper.getOrInsertElementBefore("Binaries", null);
			Element binary = helper.appendChild(binaries, "Binary");
			binary.setAttribute("MimeType", "text/xml");
			binary.setAttribute("ID", sID);
			Element payload = helper.appendChild(binary, "Payload");
			attach.appendChild(payload);
			payload.setTextContent(sPDF64);
		} catch ( Exception e) {
			logger.error("Should not see this error for unsupported encoding", e);
		}
	}
	
	private void updateMetadataAttachment( byte[] pdfBytes ) {
		String sPDF64 = new String(Base64.encodeBase64(pdfBytes));
		Element attach = getAttachment("CviMetadata.xml");
		String sId = attach.getAttribute("AttachmentRef");
		Element binary = getBinary( sId );
		if( binary == null ) {
			logger.error("updateMetadataAttachment called on nonexistent metadata");
			return;
		}
		Element payload = helper.getChildElementByName(binary, "Payload");
		if( payload == null ) {
			payload = helper.appendChild(binary, "Payload");
		}
		payload.setTextContent(sPDF64);
		
	}
	
	public CviMetaDataXml getMetaData() {
		if( mMetaData == null ) {
		Element attach = getAttachment("CviMetadata.xml");
		String sId = attach.getAttribute("AttachmentRef");
		if( sId != null && sId.trim().length() > 0 ) {
			Element binary = getBinary( sId );
			if( binary == null ) return null;
			Element payload = helper.getChildElementByName(binary, "Payload");
			if( payload == null ) return null;
			String sBase64 = payload.getTextContent();
			mMetaData = new CviMetaDataXml(sBase64);
		}
		}
		return mMetaData;
	}
	

}
