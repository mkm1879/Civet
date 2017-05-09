package edu.clemson.lph.civet.xml;
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
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;

/**
 * This class wraps an XML DOM document and provides various, mostly XPath-based, 
 * convenience methods.
 * @author mmarti5
 *
 */
public class XMLDocHelper {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private Document doc;
	private XPathFactory factory = null;

	public XMLDocHelper( Document doc ) {
		this.doc = doc;
		factory = XPathFactory.newInstance();
	}

	public String getElementTextByPath( String sPath ) {
		return getElementTextByPath(doc.getDocumentElement(), sPath );
	}
		
	public String getElementTextByPath( Node nNode,  String sPath ) {
		String sRet = "";
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return "";
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					NodeList children = nodes.item(i).getChildNodes();
					if( children == null || children.getLength() == 0 ) return "";
					for (int j = 0; j < nodes.getLength(); ++j) {
						if( children.item(j).getNodeType() == Node.TEXT_NODE ) {
							sRet = children.item(j).getNodeValue();
							if( sRet == null ) sRet = "";
							return sRet;
						}
					}
				}
			}
		} catch (XPathExpressionException e) {
			logger.error("Error evaluating xpath " + sPath);;
		}
		return sRet;
	}
	
	
	public void removeElementByPath( String sPath ) {
		Element e = getElementByPath(doc.getDocumentElement(), sPath );
		if( e != null ) {
			Node nParent = e.getParentNode();
			nParent.removeChild(e);
		}
	}

	
	public Element getElementByPath( String sPath ) {
		return getElementByPath(doc.getDocumentElement(), sPath );
	}
	
	public void removeElementByPath( Node nNode,  String sPath ) {
		Element e = null;
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return;
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					e = (Element)nodes.item(i);
				}
			}
		} catch (XPathExpressionException ex) {
			logger.error("Error evaluating xpath " + sPath, ex);
		}
		Node nParent = e.getParentNode();
		nParent.removeChild(e);
	}

		
	public Element getElementByPath( Node nNode,  String sPath ) {
		Element eRet = null;
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return null;
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					eRet = (Element)nodes.item(i);
				}
			}
		} catch (XPathExpressionException e) {
			logger.error("Error evaluating xpath " + sPath, e);
		}
		return eRet;
	}
	
	public void updateElementByPath( String sPath, String sValue ) {
		Element e = null;
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, doc.getDocumentElement(), XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return;
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					e = (Element)nodes.item(i);
					e.setTextContent(sValue);
				}
			}
		} catch (XPathExpressionException ex) {
			logger.error("Error evaluating xpath " + sPath, ex);
		}
	}
	
	public void updateAttributeByPath( String sPath, String sAttribute, String sValue ) {
		Element e = null;
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, doc.getDocumentElement(), XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return;
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					e = (Element)nodes.item(i);
					e.setAttribute(sAttribute, sValue);
				}
			}
		} catch (XPathExpressionException ex) {
			logger.error("Error evaluating xpath " + sPath, ex);
		}
	}
	
	public void updateAttribute( Node n, String sAttribute, String sValue ) {
		Element e = null;
		try {
			NodeList nodes = n.getChildNodes();
			if( nodes == null || nodes.getLength() == 0 ) return;
			for (int i = 0; i < nodes.getLength(); ++i) {
				if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					e = (Element)nodes.item(i);
					e.setAttribute(sAttribute, sValue);
				}
			}
		} catch (Exception ex) {
			logger.error("Error evaluating updating attribute", ex);
		}
	}

	
	public Element getElementByPathAndAttribute( String sPath, String sAttributeName, String sAttributeValue ) {
		return getElementByPathAndAttribute(doc.getDocumentElement(), sPath, sAttributeName, sAttributeValue );
	}
		
	public Element getElementByPathAndAttribute( Node nNode,  String sPath, String sAttributeName, String sAttributeValue ) {
		Element eRet = null;
		try {
			XPath xPath = factory.newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
			if( nodes == null || nodes.getLength() == 0 ) return null;
			for (int i = 0; i < nodes.getLength(); ++i) {
				Node n = nodes.item(i);
				if( n.getNodeType() == Node.ELEMENT_NODE ) {
					Element e = (Element)n;
					if( sAttributeValue.equals(e.getAttribute(sAttributeName) ) ) {
						return e;
					}
					
				}
			}
		} catch (XPathExpressionException e) {
			logger.error("Error evaluating xpath " + sPath);;
		}
		return eRet;
	}
	
	
	
	public Document getDocument() {
		return doc;
	}

	public String getAttributeByPath( String sPath, String sAttr ) {
		return getAttributeByPath( doc.getDocumentElement(), sPath, sAttr);
	}
		
	public String getAttributeByPath( Node nNode, String sPath, String sAttr ) {
		String sRet = null;
		if( sPath == null || sPath.trim().length() == 0 || sPath.equals(".") ) {
			Element e = (Element)nNode;
			sRet = e.getAttribute(sAttr);
		}
		else {
			try {
				XPath xPath = factory.newXPath();
				NodeList nodes = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
				if( nodes == null || nodes.getLength() == 0 ) return "";
				for (int i = 0; i < nodes.getLength(); ++i) {
					if( nodes.item(i).getNodeType() == Node.ELEMENT_NODE ) {
						Element e = (Element)nodes.item(i);
						sRet = e.getAttribute(sAttr);
						if( sRet == null ) sRet = "";
						break;
					}
				}
			} catch (XPathExpressionException e) {
				logger.error("Error evaluating xpath " + sPath + "[" + sAttr + "]");;
			}
		}
		return sRet;
	}
	
	public NodeList getNodeListByPath( String sPath ) {
		return getNodeListByPath( doc.getDocumentElement(), sPath );
	}
		
	public NodeList getNodeListByPath( Node nNode, String sPath ) {
		NodeList nRet = null;
		try {
			XPath xPath = factory.newXPath();
			nRet = (NodeList)xPath.evaluate(sPath, nNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			logger.error("Error evaluating xpath " + sPath);;
		}
		return nRet;
	}
	
	public String getXMLString() {
		try {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.transform(new DOMSource(doc),
			      new StreamResult(buffer));
			return buffer.toString();
		} catch (TransformerException e) {
			logger.error("Transform failure", e);
		}
		return null;
	}
	
	public String getXMLString(boolean bOmitDeclaration) {
		return getXMLString(bOmitDeclaration, null );
	}
		
	public String getXMLString(boolean bOmitDeclaration, String sEncoding ) {
		String sOmit = (bOmitDeclaration ? "yes" : "no");
		try {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		if( sEncoding != null )
			transformer.setOutputProperty(OutputKeys.ENCODING, sEncoding);
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			transformer.transform(new DOMSource(doc),
			      new StreamResult(buffer));
			return buffer.toString();
		} catch (TransformerException e) {
			logger.error("Transform failure", e);
		}
		return null;
	}

}
